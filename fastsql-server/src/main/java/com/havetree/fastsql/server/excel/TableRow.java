package com.havetree.fastsql.server.excel;

import com.havetree.fastsql.plugin.sql.DatabaseField;
import com.havetree.fastsql.plugin.sql.RawSqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class TableRow implements IExcelRow
{
    private final static String TIME_FORMAT_ST = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private final static String TIME_FORMAT_NORMAL = "yyyy-MM-dd HH:mm:ss";
    private final static String TIME_FORMAT_DAY = "yyyy-MM-dd";

    @Autowired
    private RawSqlService rawSqlService;

    private Map<String, Map<String, Class<?>>> mapTableFields = new ConcurrentHashMap<>();

    @Override
    public String onFirstOneColumn(String data) {
        return data;
    }

    @Override
    public Map<String, Integer> onMaybeColumnTitle(Map<String, Integer> map) {
        Map<String, Integer> mapRet = new HashMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet())
        {
            int index = entry.getKey().indexOf("（");
            if (index < 0)
            {
                index = entry.getKey().indexOf("(");
            }

            if (index < 0)
            {
                mapRet.put(entry.getKey(), entry.getValue());
                continue;
            }

            int end = entry.getKey().indexOf("）");
            if (end < 0)
            {
                end = entry.getKey().indexOf(")");
            }

            String key = entry.getKey().substring(index + 1, end);
            mapRet.put(key.trim(), entry.getValue());
        }

        return mapRet;
    }

    @Override
    public void onRow(String dbSource, Map<String, String> mapRow) {
        System.out.println("db: " + dbSource + " row: " + mapRow.toString());
        int index = dbSource.lastIndexOf('.');
        if (index < 0)
        {
            return;
        }

        String tableName = dbSource.substring(index + 1);
        String realDbSource = dbSource.substring(0, index);
        Map<String, String> mapKeysTemp = new HashMap<>();
        Map<String, String> mapFieldsTemp = new HashMap<>();
        for (Map.Entry<String, String> entry : mapRow.entrySet())
        {
            String [] keys = entry.getKey().split("=");
            if (keys.length != 2)
            {
                mapFieldsTemp.put(entry.getKey(), entry.getValue());
                continue;
            }

            if (!keys[1].trim().toLowerCase().equals("key"))
            {
                continue;
            }

            mapKeysTemp.put(keys[0].trim(), entry.getValue());
        }

        Map<String, Class<?>> mapTypes = getTableFieldType(realDbSource, tableName);
        Map<String, Object> mapKeys = clearTableField(mapKeysTemp, mapTypes);
        Map<String, Object> mapFields = clearTableField(mapFieldsTemp, mapTypes);

        String sqlUpdate = "";
        if (checkExist(realDbSource, tableName, mapKeys))
        {
            sqlUpdate = "update " + tableName + " set ";
            int i = 0;
            Object [] param = new Object[mapFields.size() + mapKeys.size()];

            for (Map.Entry<String, Object> entry : mapFields.entrySet())
            {
                if (i == 0)
                {
                    sqlUpdate += entry.getKey() + " =? ";
                }
                else
                {
                    sqlUpdate += ", " + entry.getKey() + " =? ";
                }

                param[i++] = entry.getValue();
            }

            sqlUpdate += " where ";
            for (Map.Entry<String, Object> entry : mapKeys.entrySet())
            {
                if (i== mapFields.size())
                {
                    sqlUpdate += entry.getKey() + "=? ";
                }
                else
                {
                    sqlUpdate += " and " + entry.getKey() + "=? ";
                }

                param[i++] = entry.getValue();
            }
        }
        else
        {
            sqlUpdate = "insert into " + tableName + "(";
            int i = 0;
            Object [] param = new Object[mapFields.size() + mapKeys.size()];
            String valueSql = "values (";

            for (Map.Entry<String, Object> entry : mapFields.entrySet())
            {
                if (i == 0)
                {
                    sqlUpdate += entry.getKey();
                    valueSql += "?";
                }
                else
                {
                    sqlUpdate += ", " + entry.getKey();
                    valueSql += ", ?";
                }

                param[i++] = entry.getValue();
            }

            sqlUpdate += ") " + valueSql + ") ";
        }
    }

    private boolean checkExist(String dbSource, String tableName, Map<String, Object> mapKeys)
    {
        String sqlFind = "";
        if (!mapKeys.isEmpty())
        {
            sqlFind = "select * from " + tableName + " where ";
            int i = 0;
            Object [] param = new Object[mapKeys.size()];
            for (Map.Entry<String, Object> entry : mapKeys.entrySet())
            {
                if (i==0)
                {
                    sqlFind += entry.getKey() + "=? ";
                }
                else
                {
                    sqlFind += " and " + entry.getKey() + "=? ";
                }

                param[i++] = entry.getValue();
            }

            sqlFind += " limit 1";
        }

        return false;
    }

    private Map<String, Object> clearTableField(Map<String, String> mapIn, Map<String, Class<?>> mapTypes)
    {
        Map<String, Object> mapRet = new HashMap<>();
        for (Map.Entry<String, String> entry : mapIn.entrySet())
        {
            Class<?> type = mapTypes.get(entry.getKey());
            if (type == null)
            {
                continue;
            }

            if (type.equals(String.class))
            {
                mapRet.put(entry.getKey(), entry.getValue());
                continue;
            }

            if (type.equals(Long.class))
            {
                Long vv = null;
                int find = entry.getValue().indexOf('.');
                if (find > 0)
                {
                    vv = Long.parseLong(entry.getValue().substring(0, find));
                }
                else
                {
                    vv = Long.parseLong(entry.getValue());
                }
                mapRet.put(entry.getKey(), vv);
                continue;
            }

            if (type.equals(Integer.class))
            {
                Integer vv = null;
                int find = entry.getValue().indexOf('.');
                if (find > 0)
                {
                    vv = Integer.parseInt(entry.getValue().substring(0, find));
                }
                else
                {
                    vv = Integer.parseInt(entry.getValue());
                }
                mapRet.put(entry.getKey(), vv);
                continue;
            }

            if (type.equals(Double.class))
            {
                Double vv = Double.parseDouble(entry.getValue());
                mapRet.put(entry.getKey(), vv);
                continue;
            }

            if (type.equals(Date.class))
            {
                Date vv = null;
                try
                {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT_NORMAL);
                    vv = simpleDateFormat.parse(entry.getValue());
                }
                catch (Throwable e)
                {
                    try
                    {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT_ST);
                        vv = simpleDateFormat.parse(entry.getValue());
                    }
                    catch (Throwable e1)
                    {
                        try
                        {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT_DAY);
                            vv = simpleDateFormat.parse(entry.getValue());
                        }
                        catch (Throwable e2)
                        {
                            throw new RuntimeException(e2);
                        }
                    }
                }
                mapRet.put(entry.getKey(), vv);
                continue;
            }

            throw new RuntimeException("un implement type: " + type);
        }

        return  mapRet;
    }

    private Map<String, Class<?>> getTableFieldType(String dbSource, String table)
    {
        Map<String, Class<?>> mapFieldType = mapTableFields.get(table);
        if (mapFieldType != null)
        {
            return  mapFieldType;
        }

        String sql = "select * from " + table + " limit 1";

        Map<String, Map<String, List<DatabaseField>>>  mapRet = rawSqlService.getDatabaseFieldsWithSql(Map.class, dbSource, sql);

        mapFieldType = new HashMap<>();
        for (Map.Entry<String, List<DatabaseField>> entry : mapRet.entrySet().iterator().next().getValue().entrySet())
        {
            List<DatabaseField> lstField = entry.getValue();
            System.out.println(lstField.size());
        }


        mapFieldType.put("id", Long.class);
        mapFieldType.put("email", String.class);
        mapFieldType.put("real_name", String.class);

        return mapFieldType;
    }
}

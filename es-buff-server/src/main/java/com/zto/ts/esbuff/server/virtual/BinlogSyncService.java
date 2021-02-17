package com.zto.ts.esbuff.server.virtual;

import com.alibaba.fastjson.JSONObject;
import com.zto.titans.common.util.JsonUtil;
import com.zto.titans.common.util.StringUtil;
import com.zto.ts.esbuff.api.en.AnchorType;
import com.zto.ts.esbuff.dao.mapper.one.EsBuffVirtualSqlMapper;
import com.zto.ts.esbuff.dao.pojo.po.EsBuffVirtualSqlPo;
import com.zto.ts.esbuff.plugin.sql.DatabaseField;
import com.zto.ts.esbuff.plugin.sql.RawSqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BinlogSyncService
{
    @Autowired
    private EsBuffVirtualSqlMapper mapper;

    @Resource(name = "elasticsearchTemplate")
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private RawSqlService rawSqlService;

    public void onBinLog(String dbSource, String dbName, String tableName, String dml, Map<String, Object> org, Map<String, Object> after)
    {
        if (StringUtil.isEmpty(dbSource) || StringUtil.isEmpty(dbName) || StringUtil.isEmpty(tableName) ||
                after == null || after.isEmpty()
        )
        {
            throw new RuntimeException("invalid parameters");
        }

        if (dml.equals("select"))
        {
            return;
        }

        List<EsBuffVirtualSqlPo> items = filterVirtualSqls(dbSource, dbName, tableName);
        if (items == null || items.isEmpty())
        {
            return;
        }

        for (EsBuffVirtualSqlPo one : items)
        {
            doDML(one, dml, tableName, org, after);
        }
    }

    private void doDML(EsBuffVirtualSqlPo sqlPo, String dml, String tableName, Map<String, Object> before, Map<String, Object> after)
    {
        List<DatabaseField> lstFields = getTableFields(sqlPo.getDbFields(), tableName);
        if (lstFields == null || lstFields.isEmpty())
        {
            return;
        }

        Map<String, AnchorType> mapAnchor = VirtualSqlUtil.getAnchor(sqlPo.getAnchor());
        DatabaseField primary = getPrimaryKey(mapAnchor, lstFields);

        if (dml.equals("insert"))
        {
            // select fields
            selectAndInsert(sqlPo, lstFields, primary, after);
        }
        else if (dml.equals("update"))
        {
            // update es
        }
        else
        {
            // delete es
        }
    }

    private void selectAndInsert(EsBuffVirtualSqlPo sqlPo, List<DatabaseField> lstFields, DatabaseField primary, Map<String, Object> after)
    {
        Object primaryValue = after.get(primary.getFieldName());
        StringBuilder sql = new StringBuilder(sqlPo.getRunSql()).append(" having ").append(primary.getFieldNick()).append("=?");

        String dbName = sqlPo.getDbName();
        String tableSpace = dbName;
        int index = dbName.indexOf('.');
        if (index >= 0)
        {
            tableSpace = dbName.substring(index + 1);
            dbName = dbName.substring(0, index);
        }

        List<Map> items = rawSqlService.list(Map.class, sqlPo.getDbSource(), dbName, sql.toString(), primaryValue);

    }

    private DatabaseField getPrimaryKey(Map<String, AnchorType> mapAnchor, List<DatabaseField> lstFields)
    {
        Map<String, DatabaseField> mapFields = new HashMap<>();
        for (DatabaseField field : lstFields)
        {
            mapFields.put(field.getFieldNick(), field);
        }

        for (Map.Entry<String, AnchorType> entry : mapAnchor.entrySet())
        {
            if (entry.getValue() == AnchorType.UPDATE_TIME)
            {
                continue;
            }

            DatabaseField field = mapFields.get(entry.getKey());
            if (field != null)
            {
                return  field;
            }
        }

        return  null;
    }

    private List<EsBuffVirtualSqlPo> filterVirtualSqls(String dbSource, String dbName, String tables)
    {
        List<EsBuffVirtualSqlPo> lstRet = new ArrayList<>();
        EsBuffVirtualSqlPo find = new EsBuffVirtualSqlPo();
        find.setDbSource(dbSource);
        List<EsBuffVirtualSqlPo> items = mapper.list(find);
        if (items == null || items.isEmpty())
        {
            return lstRet;
        }

        for (EsBuffVirtualSqlPo one : items)
        {
            if (StringUtil.isEmpty(one.getDbName()) || one.getDbName().indexOf(dbName) < 0)
            {
                continue;
            }

            String dbFields = one.getDbFields();
            if (!isExistTable(dbFields, tables))
            {
                continue;
            }

            lstRet.add(one);
        }

        return lstRet;
    }

    private boolean isExistTable(String json, String table)
    {
        JSONObject jMap = JSONObject.parseObject(json);
        if (!(jMap instanceof  Map))
        {
            throw new RuntimeException("json object must be map");
        }

        Map<String, Object> map = jMap;
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            Object mapDbTables = entry.getValue();
            if (!(mapDbTables instanceof Map))
            {
                throw new RuntimeException("json object must be map");
            }

            Map<String, Object> mapTable = (Map<String, Object>) mapDbTables;
            if (mapTable.containsKey(table))
            {
                return  true;
            }
        }

        return  false;
    }

    private List<DatabaseField> getTableFields(String json, String table)
    {
        JSONObject jMap = JSONObject.parseObject(json);
        if (!(jMap instanceof  Map))
        {
            throw new RuntimeException("json object must be map");
        }

        Map<String, Object> map = jMap;
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            Object mapDbTables = entry.getValue();
            if (!(mapDbTables instanceof Map))
            {
                throw new RuntimeException("json object must be map");
            }

            Map<String, Object> mapTable = (Map<String, Object>) mapDbTables;
            Object objFields = mapTable.get(table);
            if (objFields == null)
            {
                continue;
            }

            String jsonFields = JsonUtil.toJSON(objFields);
            return JsonUtil.parseArray(jsonFields, DatabaseField.class);
        }

        return  new ArrayList<>();
    }
}

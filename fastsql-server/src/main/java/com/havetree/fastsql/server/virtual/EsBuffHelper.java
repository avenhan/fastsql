package com.havetree.fastsql.server.virtual;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.zto.titans.common.event.DynamicConfigChangeSpringEvent;
import com.zto.titans.common.util.StringUtil;
import com.zto.ts.esbuff.api.IVirtualSqlApi;
import com.zto.ts.esbuff.api.Service.EsBuffAware;
import com.zto.ts.esbuff.api.anno.EsBuffField;
import com.zto.ts.esbuff.api.anno.EsBuffIndex;
import com.zto.ts.esbuff.api.dto.ValueCondition;
import com.zto.ts.esbuff.api.dto.VirtualRealFields;
import com.zto.ts.esbuff.api.dto.VirtualSqlDto;
import com.zto.ts.esbuff.api.en.AnchorType;
import com.zto.ts.esbuff.api.en.TypeEnum;
import com.zto.ts.esbuff.api.obj.EsTypeField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EsBuffHelper implements ApplicationListener<DynamicConfigChangeSpringEvent>
{
    @Autowired
    private IVirtualSqlApi virtualSqlApi;

    // @Value("${aven.esbuff.index.map}")
    private String virtualNames;

    private Map<String, String> mapTypeVirtualName = new ConcurrentHashMap<>();
    private Map<Class<?>, Map<String, EsTypeField>> mapField = new ConcurrentHashMap<>();
    private Map<Class<?>, List<VirtualRealFields>> mapRealFields = new ConcurrentHashMap<>();

    public boolean addSql(Class<?> type, String dbSource, String sql, Map<String, AnchorType> mapAnchor)
    {
        if (type == null
                || StringUtil.isEmpty(dbSource)
                || StringUtil.isEmpty(sql)
                || mapAnchor == null || mapAnchor.isEmpty()
        )
        {
            throw new RuntimeException("invalid paramter");
        }

        if (mapTypeVirtualName == null || mapTypeVirtualName.isEmpty())
        {
            updateVirtualMap(virtualNames);
        }

        String virtualName = mapTypeVirtualName.get(type.getName());
        if (StringUtil.isEmpty(virtualName))
        {
            EsBuffIndex index = type.getAnnotation(EsBuffIndex.class);
            if (index == null || StringUtil.isEmpty(index.value()))
            {
                return false;
            }

            virtualName = index.value();
        }

        Map<String, EsTypeField> mapTypes = getTypeFields(type);
        Map<String, TypeEnum> mapFields = new HashMap<>();
        for (Map.Entry<String, EsTypeField> entry : mapTypes.entrySet())
        {
            EsTypeField esTypeField = entry.getValue();
            if (esTypeField.getField().isAnnotationPresent(EsBuffField.class))
            {
                continue;
            }

            mapFields.put(entry.getKey(), esTypeField.getTypeEnum());
        }

        int index = dbSource.indexOf('/');
        if (index < 0)
        {
            throw new RuntimeException("invalid dbsource, must be ip:port/dbName ");
        }
        String dbName = dbSource.substring(index + 1);
        dbSource = dbSource.substring(0, index);

        VirtualSqlDto virtualSqlDto = new VirtualSqlDto();
        virtualSqlDto.setVirtualName(virtualName);
        virtualSqlDto.setSql(sql);
        virtualSqlDto.setFields(mapFields);
        virtualSqlDto.setAnchors(mapAnchor);
        virtualSqlDto.setDbSource(dbSource);
        virtualSqlDto.setDbName(dbName);
        return virtualSqlApi.addSql(virtualSqlDto);
    }

    public <T> List<T> search(Class<T> type, List<ValueCondition> condition, Integer page, Integer pageSize)
    {
        return search(type, condition, page, pageSize, null, null);
    }

    public <T> List<T> search(Class<T> type, List<ValueCondition> condition, Integer page, Integer pageSize, String sortField, String sort)
    {
        List<T> lstRet = new ArrayList<>();
        if (mapTypeVirtualName == null || mapTypeVirtualName.isEmpty())
        {
            updateVirtualMap(virtualNames);
        }

        String virtualName = mapTypeVirtualName.get(type.getName());
        if (StringUtil.isEmpty(virtualName))
        {
            EsBuffIndex index = type.getAnnotation(EsBuffIndex.class);
            if (index == null || StringUtil.isEmpty(index.value()))
            {
                return lstRet;
            }

            virtualName = index.value();
        }

        List<VirtualRealFields> lstRealFields = getRealFields(type);
        List<Map<String, Object>> lstMap = virtualSqlApi.search(lstRealFields, virtualName, condition, page, pageSize, sortField, sort);
        for (Map<String, Object> map : lstMap)
        {
            lstRet.add(create(type, map));
        }

        return  lstRet;
    }

    public long count(Class<?> type, List<ValueCondition> condition)
    {
        if (mapTypeVirtualName == null || mapTypeVirtualName.isEmpty())
        {
            updateVirtualMap(virtualNames);
        }

        String virtualName = mapTypeVirtualName.get(type.getName());
        if (StringUtil.isEmpty(virtualName))
        {
            EsBuffIndex index = type.getAnnotation(EsBuffIndex.class);
            if (index == null || StringUtil.isEmpty(index.value()))
            {
                return 0;
            }

            virtualName = index.value();
        }

        List<VirtualRealFields> lstRealFields = getRealFields(type);
        return virtualSqlApi.count(virtualName, condition);
    }

    private <T> T create(Class<T> type, Map<String, Object> map)
    {
        try
        {
            T t = type.newInstance();
            Map<String, EsTypeField> mapTypes = getTypeFields(type);
            for (Map.Entry<String, EsTypeField> entry : mapTypes.entrySet())
            {
                EsTypeField field = entry.getValue();
                Object value = map.get(entry.getKey());
                if (value == null)
                {
                    continue;
                }

                field.getField().setAccessible(true);
                field.getField().set(t, value);
            }

            return  t;
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    private Map<String, EsTypeField> getTypeFields(Class<?> type)
    {
        Map<String, EsTypeField> mapFields = mapField.get(type);
        if (mapFields != null)
        {
            return mapFields;
        }

        mapFields = new HashMap<String, EsTypeField>();
        Field [] fields = type.getDeclaredFields();
        for (Field field : fields)
        {
            String fieldName = toFieldName(field.getName());
            EsTypeField esTypeField = new EsTypeField();
            esTypeField.setFieldName(fieldName);
            esTypeField.setField(field);

            TypeEnum typeEnum = null;
            if (field.getType().equals(Integer.class) || field.getType().equals(int.class))
            {
                typeEnum = TypeEnum.INT;
            }
            else if (field.getType().equals(Long.class) || field.getType().equals(long.class))
            {
                typeEnum = TypeEnum.LONG;
            }
            else if (field.getType().equals(String.class))
            {
                typeEnum = TypeEnum.STRING;
            }
            else if (field.getType().equals(Double.class) || field.getType().equals(double.class))
            {
                typeEnum = TypeEnum.DOUBLE;
            }
            else if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class))
            {
                typeEnum = TypeEnum.BOOLEAN;
            }
            else if (field.getType().equals(BigDecimal.class))
            {
                typeEnum = TypeEnum.DECIMAL;
            }
            else if (field.getType().equals(Date.class))
            {
                typeEnum = TypeEnum.DATE;
            }
            else
            {
                throw new RuntimeException("invalid field type: " + field.getType().getName());
            }

            esTypeField.setTypeEnum(typeEnum);
            mapFields.put(fieldName, esTypeField);
        }

        mapField.put(type, mapFields);
        return  mapFields;
    }

    public static String toFieldName(String value)
    {
        if (StringUtil.isEmpty(value))
        {
            return "";
        }

        StringBuilder ret = new StringBuilder();
        char[] lstChar = value.toCharArray();
        for (char c : lstChar)
        {
            if (Character.isLowerCase(c) || Character.isDigit(c))
            {
                ret.append(c);
            }
            else
            {
                if (ret.length() > 0)
                {
                    ret.append("_");
                }
                ret.append(Character.toLowerCase(c));
            }
        }

        return ret.toString();
    }

    @Override
    public void onApplicationEvent(DynamicConfigChangeSpringEvent dynamicConfigChangeSpringEvent) {
        String json = EsBuffAware.getProperty("aven.esbuff.index.map");
        updateVirtualMap(json);
    }

    private void updateVirtualMap(String json)
    {
        if (json == null || json.isEmpty())
        {
            return;
        }

        JSONObject jMap = JSONObject.parseObject(json);
        if (!(jMap instanceof  Map))
        {
            throw new RuntimeException("json object must be map");
        }

        Map<String, Object> map = jMap;

        Map<String, String> mapValue = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            String value = (String) entry.getValue();
            mapValue.put(entry.getKey(), value);
        }

        if (!mapValue.isEmpty())
        {
            mapTypeVirtualName.clear();
            mapTypeVirtualName.putAll(mapValue);
        }
    }

    private List<VirtualRealFields> getRealFields(Class<?> type)
    {
        List<VirtualRealFields> lstRet = mapRealFields.get(type);
        if (lstRet != null)
        {
            return lstRet;
        }

        Map<String, VirtualRealFields> mapVirtualRealField = new HashMap<>();
        List<VirtualRealFields> lstVirtualFields = new ArrayList<>();

        Field [] fields = type.getDeclaredFields();
        for (Field field : fields)
        {
            if (!field.isAnnotationPresent(EsBuffField.class))
            {
                continue;
            }

            String fieldName = toFieldName(field.getName());
            EsBuffField esBuffField = field.getAnnotation(EsBuffField.class);
            String table = esBuffField.table();
            String id = esBuffField.id();
            if (StringUtil.isEmpty(id) || StringUtil.isEmpty(table))
            {
                throw new RuntimeException("@EsBuffField must has table and esbuff id");
            }

            VirtualRealFields virtualRealFields = mapVirtualRealField.get(table);
            if (virtualRealFields == null)
            {
                virtualRealFields = new VirtualRealFields();
                virtualRealFields.setTable(table);
                virtualRealFields.setId(id);
                virtualRealFields.setFields(new ArrayList<>());

                mapVirtualRealField.put(table, virtualRealFields);
                lstVirtualFields.add(virtualRealFields);
            }

            virtualRealFields.addField(fieldName);
        }

        mapRealFields.put(type, lstVirtualFields);
        return lstVirtualFields;
    }

}


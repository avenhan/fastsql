package com.havetree.fastsql.plugin;

import com.havetree.fastsql.plugin.sql.RawSqlContext;
import com.zto.titans.common.util.JsonUtil;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

// @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class })

@Intercepts({ 
    @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }),
    @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }) })
public class RawSqlPluginQuery implements Interceptor
{
    public static final String  KEY_REPLACE_SQL = "select * from zto_es_buff_config(TEMP_韩)";
    
    private final static Logger LOGGER          = LoggerFactory.getLogger(RawSqlPluginQuery.class);
    
    @SuppressWarnings("rawtypes")
    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {

        Object[] args = invocation.getArgs();
        if (args[0] instanceof Connection)
        {
            return doPrepare(invocation);
        }
        
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (args.length > 1)
        {
            parameter = invocation.getArgs()[1];
        }
        
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        if (!KEY_REPLACE_SQL.equals(boundSql.getSql()) || args.length < 4)
        {
            Object ret = invocation.proceed();
            return ret;
        }
        
        MetaObject boundSqlMetaObject = SystemMetaObject.forObject(boundSql);
        String replaceSql = RawSqlContext.get().getSql();
        if (Objects.isNull(replaceSql)) {
            Object ret = invocation.proceed();
            return ret;
        }
        boundSqlMetaObject.setValue("sql", replaceSql);
        
        Executor executor = (Executor) invocation.getTarget();
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        
        CacheKey key = executor.createCacheKey(mappedStatement, parameter, rowBounds, boundSql);
        try
        {
            RawSqlContext.get().setList(null);
            Object ret = executor.query(mappedStatement, parameter, rowBounds, resultHandler, key, boundSql);
            if (ret == null || !(ret instanceof List))
            {
                return ret;
            }
            
            List list = (List)ret;
            if (list.isEmpty())
            {
                return ret;
            }
            
            Class<?> retType = RawSqlContext.get().getType();
            if (retType.equals(Map.class) || isInterfaceOf(list.get(0), retType))
            {
                return ret;
            }
            
            Map<String, Field> mapFields = RawSqlContext.get().getFields();
            List<Object> lstRet = new ArrayList<Object>();
            for (Object one : list)
            {
                if (!(one instanceof Map))
                {
                    lstRet.add(one);
                    continue;
                }
                
                Map<?, ?> map = (Map<?, ?>) one;
                lstRet.add(createObjectFromMap(retType, map, mapFields));
            }
            
            return ret;
        }
        catch (Throwable e)
        {
            LOGGER.info(e.getMessage() + " sql: " + replaceSql, e);
            List<?> list = RawSqlContext.get().getList();
            if (list == null)
            {
                throw new RuntimeException(e);
            }
            
            if (list.isEmpty())
            {
                return list;
            }
            
            Class<?> retType = RawSqlContext.get().getType();
            Object objOne = list.get(0);
            if (!isInterfaceOf(objOne, retType))
            {
                throw new RuntimeException(e);
            }
            
            return list;
        }
    }
    
    private boolean isInterfaceOf(Object obj, Class<?> type)
    {
        if (obj.getClass().equals(type))
        {
            return true;
        }
        
        Class<?> [] ites = obj.getClass().getInterfaces();
        for (Class<?> it : ites)
        {
            if (it.equals(type))
            {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Object plugin(Object target)
    {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties)
    {
    }
    
    private Object doPrepare(Invocation invocation) throws Throwable
    {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();
        String replaceSql = RawSqlContext.get().getSql();
        if (replaceSql==null || !replaceSql.equals(boundSql.getSql()))
        {
            Object ret = invocation.proceed();
            return ret;
        }
        
        Connection con = (Connection) invocation.getArgs()[0];
        MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        
        java.sql.PreparedStatement ps = con.prepareStatement(sql);
        
        // 获取参数处理器来处理参数
        Object[] args = RawSqlContext.get().getArgs();
        if (args != null)
        {
            for (int i = 0; i < args.length; i++)
            {
                Object parameterObj = args[i];
                ps.setObject(i + 1, parameterObj);
            }
        }
        
        ParameterHandler ph = (ParameterHandler) metaObject.getValue("delegate.parameterHandler");
        ph.setParameters(ps);
        metaObject.setValue("delegate.boundSql.sql", replaceSql);
        
        Map<String, Field> mapFields = RawSqlContext.get().getFields();
        List<Object> lstRet = new ArrayList<Object>();
        Class<?> retType = RawSqlContext.get().getType();
        ResultSet rs = ps.executeQuery();
        Set<String> setFields = toFieldSet(rs);
        while (rs.next())
        {
            if (!retType.equals(Map.class))
            {
                lstRet.add(createObject(retType, rs, mapFields));
                continue;
            }
            
            lstRet.add(toMap(setFields, rs));
        }
        
        RawSqlContext.get().setList(lstRet);
        
        Object ret = invocation.proceed();
        return ret;
    }
    
    private Map<String, Object> toMap(Set<String> fields, ResultSet rs)
    {
        try
        {
            Map<String, Object> mapRet = new HashMap<String, Object>();
            for (String field : fields)
            {
                mapRet.put(field, rs.getString(field));
            }
            
            return mapRet;
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private java.util.Set<String> toFieldSet(ResultSet rs)
    {
        try
        {
            java.util.Set<String> lstRet = new HashSet<String>();
            java.sql.ResultSetMetaData rsData = rs.getMetaData();
            int count = rsData.getColumnCount();
            for (int i = 0; i < count; i++)
            {
                lstRet.add(rsData.getColumnName(i + 1));
            }
            
            return lstRet;
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private <T> T createObject(Class<T> type, ResultSet rs, Map<String, Field> mapFields)
    {
        try
        {
            T t = type.newInstance();
            
            for (Map.Entry<String, Field> entry : mapFields.entrySet())
            {
                Object objField = null;
                Class<?> paramType = entry.getValue().getType();
                if (paramType.equals(Integer.class) || paramType.equals(int.class))
                {
                    objField = rs.getInt(entry.getKey());
                }
                else if (paramType.equals(Long.class) || paramType.equals(long.class))
                {
                    objField = rs.getLong(entry.getKey());
                }
                else if (paramType.equals(Double.class) || paramType.equals(double.class))
                {
                    objField = rs.getDouble(entry.getKey());
                }
                else if (paramType.equals(Float.class) || paramType.equals(float.class))
                {
                    objField = rs.getFloat(entry.getKey());
                }
                else if (paramType.equals(Short.class) || paramType.equals(short.class))
                {
                    objField = rs.getFloat(entry.getKey());
                }
                else if (paramType.equals(Date.class))
                {
                    objField = rs.getDate(entry.getKey());
                }
                else if (paramType.equals(Byte.class) || paramType.equals(byte.class))
                {
                    objField = rs.getByte(entry.getKey());
                }
                else if (paramType.equals(BigDecimal.class))
                {
                    objField = rs.getBigDecimal(entry.getKey());
                }
                else if (paramType.equals(String.class))
                {
                    objField = rs.getString(entry.getKey());
                }
                else
                {
                    throw new RuntimeException(
                            "invalid raw sql enable type: " + type.getName() + " field name: " + entry.getKey() + " type: " + paramType.getName());
                }
                
                entry.getValue().setAccessible(true);
                entry.getValue().set(t, objField);
            }
            
            return t;
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private <T> T createObjectFromMap(Class<T> type, Map<?, ?> rs, Map<String, Field> mapFields)
    {
        try
        {
            T t = type.newInstance();
            
            for (Map.Entry<String, Field> entry : mapFields.entrySet())
            {
                Object objField = rs.get(entry.getKey());
                if (objField == null)
                {
                    continue;
                }
                
                Class<?> paramType = entry.getValue().getType();
                if (objField.getClass().equals(paramType))
                {
                    entry.getValue().setAccessible(true);
                    entry.getValue().set(t, objField);
                    continue;
                }
                
                objField = JsonUtil.parse(JsonUtil.toJSON(objField), paramType);
                entry.getValue().setAccessible(true);
                entry.getValue().set(t, objField);
            }
            
            return t;
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }
}
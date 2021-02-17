package com.havetree.fastsql.plugin.sql;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.havetree.fastsql.plugin.mapper.BuffConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zto.titans.common.util.JsonUtil;
import com.havetree.fastsql.plugin.RawSqlAware;

@Service
public class RawSqlService
{
    private BuffConfigMapper mapper;
    
    @Autowired
    private RawSqlAware rawSqlAware;
    
    public Map<String, Set<String>> listDatabases()
    {
        return rawSqlAware.listDatabase();
    }

    public String getDatabaseType(String url)
    {
        return rawSqlAware.getDatabaseType(url);
    }
    
    public <T> T get(Class<T> type, String sql, Object... args)
    {
        List<T> items = list(type, sql, args);
        if (items == null || items.isEmpty())
        {
            return null;
        }
        
        return items.get(0);
    }
    
    public <T> List<T> list(Class<T> type, String sql, Object... args)
    {
        DataSource dataSource = rawSqlAware.getDefaultDataSource();
        return list(type, dataSource, sql, args);
    }
    
    public <T> List<T> list(Class<T> type, String dataSource, String sql, Object... args)
    {
        DataSource ds = rawSqlAware.getDataSource(dataSource);
        return list(type, ds, sql, args);
    }
    
    public <T> List<T> list(Class<T> type, String ip, String dbName, String sql, Object... args)
    {
        DataSource dataSource = rawSqlAware.getDataSource(ip, dbName);
        return list(type, dataSource, sql, args);
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> list(Class<T> type, DataSource dataSource, String sql, Object... args)
    {
        if (dataSource == null)
        {
            throw new RuntimeException("there is no datasource, you should config datasource in config file");
        }
        
        Connection connection = null;
        java.sql.PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            
            if (args != null)
            {
                for (int i = 0; i < args.length; i++)
                {
                    Object parameterObj = args[i];
                    if (parameterObj == null)
                    {
                        ps.setObject(i + 1, parameterObj);
                        continue;
                    }

                    if (parameterObj instanceof Date)
                    {
                        java.util.Date var = (Date) parameterObj;
                        java.sql.Date sqlDate = new java.sql.Date(var.getTime());
                        ps.setDate(i + 1, sqlDate);
                    }
                    else
                    {
                        ps.setObject(i + 1, parameterObj);
                    }
                }
            }
            
            RawSqlContext.get().setSql(sql);
            RawSqlContext.get().setArgs(args);
            RawSqlContext.get().setType(type);
            
            Map<String, Field> mapFields = RawSqlContext.get().getFields();
            List<Object> lstRet = new ArrayList<Object>();
            Class<?> retType = RawSqlContext.get().getType();
            rs = ps.executeQuery();
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
            
            
            return (List<T>) lstRet;
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                    rs = null;
                }
                
                if (ps != null)
                {
                    ps.close();
                    ps = null;
                }
                
                if (connection != null)
                {
                    connection.close();
                    connection = null;
                }
            }
            catch (SQLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally 
            {
                if (connection != null)
                {
                    try
                    {
                        connection.close();
                    }
                    catch (SQLException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public <T> Map<String, Map<String, List<DatabaseField>>> getDatabaseFieldsWithSql(Class<T> type, String ip, String dbName, String sql, Object... args)
    {
        DataSource dataSource = rawSqlAware.getDataSource(ip, dbName);
        return getDatabaseFieldsWithSql(type, dataSource, sql, args);
    }

    public<T> Map<String, Map<String, List<DatabaseField>>> getDatabaseFieldsWithSql(Class<T> type, String dataSource, String sql, Object... args)
    {
        DataSource ds = rawSqlAware.getDataSource(dataSource);
        return getDatabaseFieldsWithSql(type, ds, sql, args);
    }

    public <T> Map<String, Map<String, List<DatabaseField>>> getDatabaseFieldsWithSql(Class<T> type, DataSource dataSource, String sql, Object... args)
    {
        if (dataSource == null)
        {
            throw new RuntimeException("there is no datasource, you should config datasource in config file");
        }

        Connection connection = null;
        java.sql.PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);

            if (args != null)
            {
                for (int i = 0; i < args.length; i++)
                {
                    Object parameterObj = args[i];
                    if (parameterObj == null)
                    {
                        ps.setObject(i + 1, parameterObj);
                        continue;
                    }

                    if (parameterObj instanceof Date)
                    {
                        java.util.Date var = (Date) parameterObj;
                        java.sql.Date sqlDate = new java.sql.Date(var.getTime());
                        ps.setDate(i + 1, sqlDate);
                    }
                    else
                    {
                        ps.setObject(i + 1, parameterObj);
                    }
                }
            }

            RawSqlContext.get().setSql(sql);
            RawSqlContext.get().setArgs(args);
            RawSqlContext.get().setType(type);

            Map<String, Field> mapFields = RawSqlContext.get().getFields();
            Class<?> retType = RawSqlContext.get().getType();
            rs = ps.executeQuery();
            List<DatabaseField> lstRet = new ArrayList<>();

            Map<String, Map<String, List<DatabaseField>>> mapRet = new HashMap<String, Map<String, List<DatabaseField>>>();
            java.sql.ResultSetMetaData rsData = rs.getMetaData();
            int count = rsData.getColumnCount();
            for (int i = 0; i < count; i++)
            {
                String nickField = rsData.getColumnLabel(i + 1);
                String fieldName = rsData.getColumnName(i + 1);
                String tableName = rsData.getTableName(i +1);
                String dbName = rsData.getCatalogName(i + 1);
                String javaTypeName = rsData.getColumnClassName(i + 1);

                DatabaseField field = new DatabaseField();
                field.setDbName(dbName);
                field.setTableName(tableName);
                field.setFieldName(fieldName);
                field.setFieldNick(nickField);
                field.setFieldType(javaTypeName);
                field.setTableNick("");

                lstRet.add(field);

                Map<String, List<DatabaseField>> mapTableFields = mapRet.get(dbName);
                if (mapTableFields == null)
                {
                    mapTableFields = new HashMap<String, List<DatabaseField>>();
                    mapRet.put(dbName, mapTableFields);
                }

                List<DatabaseField> lstFields = mapTableFields.get(tableName);
                if (lstFields == null)
                {
                    lstFields = new ArrayList<DatabaseField>();
                    mapTableFields.put(tableName, lstFields);
                }

                lstFields.add(field);
            }

            return mapRet;
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                    rs = null;
                }

                if (ps != null)
                {
                    ps.close();
                    ps = null;
                }

                if (connection != null)
                {
                    connection.close();
                    connection = null;
                }
            }
            catch (SQLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally
            {
                if (connection != null)
                {
                    try
                    {
                        connection.close();
                    }
                    catch (SQLException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public int[] update(String sql, Object... args)
    {
        DataSource dataSource = rawSqlAware.getDefaultDataSource();
        return update(dataSource, sql, args);
    }

    public int[] update(String dataSource, String sql, Object... args)
    {
        DataSource ds = rawSqlAware.getDataSource(dataSource);
        return update(ds, sql, args);
    }

    public int[] update(String ip, String dbName, String sql, Object... args)
    {
        DataSource dataSource = rawSqlAware.getDataSource(ip, dbName);
        return update(dataSource, sql, args);
    }

    public int[]  update(DataSource dataSource, String sql, Object... args)
    {
        if (dataSource == null)
        {
            throw new RuntimeException("there is no datasource, you should config datasource in config file");
        }

        Connection connection = null;
        java.sql.PreparedStatement ps = null;
        try
        {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);

            if (args != null)
            {
                for (int i = 0; i < args.length; i++)
                {
                    Object parameterObj = args[i];
                    if (parameterObj == null)
                    {
                        ps.setNull(i + 1, java.sql.Types.VARCHAR);
                    }
                    else if (parameterObj instanceof Integer || parameterObj.equals(int.class))
                    {
                        ps.setObject(i + 1, parameterObj, java.sql.Types.INTEGER);
                    }
                    else if (parameterObj instanceof Long || parameterObj.equals(long.class))
                    {
                        ps.setObject(i + 1, parameterObj, java.sql.Types.BIGINT);
                    }
                    else if (parameterObj instanceof String)
                    {
                        ps.setObject(i + 1, parameterObj, java.sql.Types.VARCHAR);
                    }
                    else if (parameterObj instanceof Date)
                    {
                        java.util.Date var = (Date) parameterObj;
                        java.sql.Date sqlDate = new java.sql.Date(var.getTime());
                        ps.setDate(i + 1, sqlDate);
                    }
                    else
                    {
                        ps.setObject(i + 1, parameterObj);
                    }
                }
            }

            RawSqlContext.get().setSql(sql);
            RawSqlContext.get().setArgs(args);

            try
            {
                ps.addBatch();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }

            Map<String, Field> mapFields = RawSqlContext.get().getFields();
            Class<?> retType = RawSqlContext.get().getType();
            return ps.executeBatch();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if (ps != null)
                {
                    ps.close();
                    ps = null;
                }

                if (connection != null)
                {
                    connection.close();
                    connection = null;
                }
            }
            catch (SQLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally
            {
                if (connection != null)
                {
                    try
                    {
                        connection.close();
                    }
                    catch (SQLException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
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
                lstRet.add(rsData.getColumnLabel(i + 1));
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

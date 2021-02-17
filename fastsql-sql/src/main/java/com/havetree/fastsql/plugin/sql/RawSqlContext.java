package com.havetree.fastsql.plugin.sql;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RawSqlContext
{
    private String                            sql;
    private Object[]                          args;
    private Class<?>                          type;
    
    private Map<String, Field>                mapFields = new HashMap<String, Field>();
    
    private List<?> list;
    
    private static ThreadLocal<RawSqlContext> g_raw     = new ThreadLocal<RawSqlContext>()
                                                        {
                                                            @Override
                                                            protected RawSqlContext initialValue()
                                                            {
                                                                return null;
                                                            }
                                                        };
    
    public static RawSqlContext get()
    {
        RawSqlContext ret = g_raw.get();
        if (ret == null)
        {
            RawSqlContext trans = new RawSqlContext();
            g_raw.set(trans);
            ret = trans;
        }
        
        return ret;
    }
    
    public String getSql()
    {
        return sql;
    }
    
    public void setSql(String sql)
    {
        this.sql = sql;
    }
    
    public Object[] getArgs()
    {
        return args;
    }
    
    public void setArgs(Object[] args)
    {
        this.args = args;
    }
    
    public Class<?> getType()
    {
        return type;
    }
    
    public List<?> getList()
    {
        return list;
    }

    public void setList(List<?> list)
    {
        this.list = list;
    }

    public void setType(Class<?> type)
    {
        this.type = type;
        
        mapFields.clear();
        
        if (type.equals(Map.class))
        {
            return;
        }
        
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields)
        {
            mapFields.put(toField(field.getName()), field);
        }
    }
    
    public Map<String, Field> getFields()
    {
        return mapFields;
    }
    
    private String toField(String value)
    {
        if (value == null)
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
    
}

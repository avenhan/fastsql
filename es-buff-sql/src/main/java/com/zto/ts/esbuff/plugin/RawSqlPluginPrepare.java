package com.zto.ts.esbuff.plugin;

import com.zto.ts.esbuff.plugin.sql.RawSqlContext;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

@Intercepts({ 
        @Signature(type = Executor.class, method = "prepare", args = {Connection.class, Integer.class }) })
public class RawSqlPluginPrepare implements Interceptor
{
    public static final String  KEY_REPLACE_SQL = "select * from zto_es_buff_config(TEMP_韩)";
    
    private final static Logger LOGGER          = LoggerFactory.getLogger(RawSqlPluginPrepare.class);
    
    @SuppressWarnings("rawtypes")
    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        if (RawSqlContext.get() == null)
        {
            Object ret = invocation.proceed();
            return ret;
        }

        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();
        if (!KEY_REPLACE_SQL.equals(boundSql.getSql()))
        {
            Object ret = invocation.proceed();
            return ret;
        }
        
        Object[] args = invocation.getArgs();
        Object parameter = null;
        if (args.length > 1)
        {
            parameter = invocation.getArgs()[1];
        }
        
        Connection con = (Connection) invocation.getArgs()[0];
        MetaObject metaObject = MetaObject.forObject(statementHandler, 
                SystemMetaObject.DEFAULT_OBJECT_FACTORY, 
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        
        PreparedStatement prepareStatement = con.prepareStatement(sql);
        if (parameter != null)
        {
            Object[] params = (Object[]) parameter;
            for (int i = 0; i < params.length; i++)
            {
                Object parameterObj = params[i];
                prepareStatement.setObject(i + 1, parameterObj);
            }
        }
        
        Object ret = invocation.proceed();
        return ret;
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
}
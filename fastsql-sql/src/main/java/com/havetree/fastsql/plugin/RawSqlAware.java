package com.havetree.fastsql.plugin;

import com.zto.titans.common.event.DynamicConfigChangeSpringEvent;
import com.zto.titans.common.util.StringUtil;
import com.zto.titans.orm.configuration.DynamicDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RawSqlAware implements ApplicationListener<DynamicConfigChangeSpringEvent>, ApplicationContextAware, InstantiationAwareBeanPostProcessor
{
    private ApplicationContext context;
    
    private Map<String, DynamicDataSource> mapDataSource = new ConcurrentHashMap<>();
    private Map<String, Set<String>> mapDatabases = new ConcurrentHashMap<String, Set<String>>();
    private Map<String, String> mapDatabaseType = new ConcurrentHashMap<>();
    private AtomicBoolean init = new AtomicBoolean(false);
    
    @Override
    public void setApplicationContext(ApplicationContext arg0) throws BeansException
    {
        context = arg0;
        init.set(true);
        initDataSource();
    }
    
    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException
    {
        return true;
    }
    
    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException
    {
        return pvs;
    }
    
    public Map<String, Set<String>> listDatabase()
    {
        return mapDatabases;
    }

    public String getDatabaseType(String url)
    {
        if (StringUtil.isEmpty(url))
        {
            return  null;
        }

        if (url.indexOf(":") < 0)
        {
            url += ":3306";
        }

        return mapDatabaseType.get(url);
    }
    
    public DataSource getDefaultDataSource()
    {
        if (mapDataSource.isEmpty())
        {
            return null;
        }
        
        return mapDataSource.entrySet().iterator().next().getValue();
    }
    
    public DataSource getDataSource(String name)
    {
        DynamicDataSource dataSource = mapDataSource.get(name);
        if (dataSource == null)
        {
            initDataSource();
        }
        
        return mapDataSource.get(name);
    }
    
    public DataSource getDataSource(String ip, String db)
    {
        if (ip.indexOf(":") < 0)
        {
            ip += ":3306";
        }
        
        String key = ip + "/" + db;
        
        DynamicDataSource dataSource = mapDataSource.get(key);
        if (dataSource == null)
        {
            initDataSource();
        }
        
        return mapDataSource.get(key);
    }
    
    @Scheduled(fixedRate=5000)
    public void initDataSource()
    {
        if (init.get() == false)
        {
            return;
        }
        
        Map<String, DynamicDataSource> mapSource = context.getBeansOfType(DynamicDataSource.class);
        for (Map.Entry<String, DynamicDataSource> entry : mapSource.entrySet())
        {
            String name = entry.getKey().substring(0, entry.getKey().indexOf("DataSource"));
            DynamicDataSource dataSource = entry.getValue();

            List<String> lstDbInfo = getUrlAndDbName(dataSource);
            if (lstDbInfo == null ||  lstDbInfo.size() != 3)
            {
                continue;
            }
            
            String url = lstDbInfo.get(0);
            String dbName = lstDbInfo.get(1);
            String type = lstDbInfo.get(2);
            
            mapDataSource.put(name, entry.getValue());
            mapDataSource.put(url + "/" + dbName, entry.getValue());
            mapDatabaseType.put(url, type);
            
            Set<String> setDb = mapDatabases.get(url);
            if (setDb == null)
            {
                setDb = new HashSet<String>();
                mapDatabases.put(url, setDb);
            }
            
            setDb.add(dbName);
        }
        
        init.set(false);
    }

    @Override
    public void onApplicationEvent(DynamicConfigChangeSpringEvent event)
    {
        init.set(true);
    }
    
    List<String> getUrlAndDbName(DynamicDataSource dataSource)
    {
        List<String> lstRet = new ArrayList<String>();
        
        String url = dataSource.getDataSource().getJdbcUrl();
        if (url.indexOf("jdbc:mysql:") >= 0)
        {
            int index = url.indexOf('?');
            if (index >= 0)
            {
                url = url.substring(0, index);
            }

            index = url.lastIndexOf('/');
            String dbName = url.substring(index + 1);

            url = url.substring(0, index);

            index = url.lastIndexOf('/');
            if (index >= 0)
            {
                url = url.substring(index + 1);
            }
            
            lstRet.add(url);
            lstRet.add(dbName);
            lstRet.add("mysql");
        }
        else if (url.indexOf("jdbc:oracle:") >= 0)
        {
            int index = url.indexOf('?');
            if (index >= 0)
            {
                url = url.substring(0, index);
            }
            
            index = url.indexOf("@");
            if (index >= 0)
            {
                url = url.substring(index + 1);
            }
            
            index = url.lastIndexOf(':');
            String dbName = "";
            if (index >= 0)
            {
                dbName = url.substring(index + 1);
                url = url.substring(0, index);
            }
            
            lstRet.add(url);
            lstRet.add(dbName);
            lstRet.add("oracle");
        }
        
        return lstRet;
    }
}



package com.zto.ts.esbuff.plugin.config;

import java.util.List;

import javax.annotation.PostConstruct;

import com.zto.ts.esbuff.plugin.sql.RawSqlService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zto.ts.esbuff.plugin.RawSqlPluginQuery;

@Configuration
public class RawSqlAutoConfig
{
    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;
    
    @PostConstruct
    public void addMyInterceptor()
    {
        RawSqlPluginQuery e = new RawSqlPluginQuery();
        
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList)
        {
            sqlSessionFactory.getConfiguration().addInterceptor(e);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    RawSqlService RawSqlService() {
        return new RawSqlService();
    }
}

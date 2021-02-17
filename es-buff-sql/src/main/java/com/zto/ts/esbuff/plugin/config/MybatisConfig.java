package com.zto.ts.esbuff.plugin.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.zto.titans.orm.configuration.DynamicDataSource;

@Configuration
public class MybatisConfig
{
//    @Bean("dynamicDataSource")
//    public DataSource dynamicDataSource()
//    {
//        DynamicDataSource dynamicDataSource = new DynamicDataSource();
//        Map<Object, Object> dataSourceMap = new HashMap<Object, Object>();
//        // 将 master 数据源作为默认指定的数据源
//        dynamicDataSource.setDefaultDataSource();
//        // 将 master 和 slave 数据源作为指定的数据源
//        dynamicDataSource.setDataSources(dataSourceMap);
//        return dynamicDataSource;
//    }
//    
//    @Bean
//    public SqlSessionFactoryBean sqlSessionFactoryBean() throws Exception
//    {
//        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
//        // 配置数据源，此处配置为关键配置，如果没有将 dynamicDataSource作为数据源则不能实现切换
//        sessionFactory.setDataSource(dynamicDataSource());
//        sessionFactory.setTypeAliasesPackage("com.louis.**.model"); // 扫描Model
//        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        sessionFactory.setMapperLocations(resolver.getResources("classpath*:**/sqlmap/*.xml")); // 扫描映射文件
//        return sessionFactory;
//    }
//    
//    @Bean
//    public PlatformTransactionManager transactionManager()
//    {
//        // 配置事务管理, 使用事务时在方法头部添加@Transactional注解即可
//        return new DataSourceTransactionManager(dynamicDataSource());
//    }
}

package com.zto.ts.esbuff.server;

import com.zto.titans.common.annotation.EnableFramework;
import com.zto.titans.config.annotation.EnableConfig;
import com.zto.titans.es.annotation.EnableElasticSearch;
import com.zto.titans.logging.annotation.EnableDynamicLog;
import com.zto.titans.mq.annotation.EnableMQ;
import com.zto.titans.orm.annotation.EnableMyBatis;
import com.zto.titans.soa.dubbo.annotation.EnableDubbo;
import com.zto.titans.soa.http.annotation.EnableHttp;
import com.zto.ts.esbuff.api.anno.EnableEsBuff;
import com.zto.ts.esbuff.plugin.anno.EnableRawSql;
import com.zto.ts.esbuff.server.common.config.TsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


/**
 * @EnableFramework开启Framework,类似SpringBoot的SpringBootApplication注解，封装了部分兼容性问题
 * @EnableWeb开启Web模块集成了SSO 参数校验等功能
 * @EnableHttp开启Http模块,使用接口的方式调用Http接口
 * @EnableDubbo开启Dubbo模块，自动检测Dubbo的Service注解及Reference注解
 * @EnableMyBatis开启了Mybatis模块
 * @EnableDynamicLog开启动态日志模块及规范的日志文件及目录 注意:请一定要填写应用的APP_ID,应用框架依赖此ID,生成规则请参考WIKI
 * 填写目录:src/main/resources/META-INF/app.properties
 */
@EnableFramework
@EnableDubbo
@EnableMyBatis
@EnableDynamicLog
@EnableRawSql
//@EnableMQ
@EnableElasticSearch
// @EnableConfig({"application","5pWw5o2u5Lit5b+D6L+Q57u06YOo.HZPL132028_monitordb"})
//@EnableConfig({"application","5pyr56uv56CU5Y+R6YOo.panda"})
//@EnableConfigurationProperties(value= TsConfig.class )
@EnableHttp
//@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

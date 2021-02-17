package com.havetree.fastsql.schedule;


import com.havetree.fastsql.schedule.common.conf.TsConfig;
import com.zto.titans.common.annotation.EnableFramework;
import com.zto.titans.common.startup.Main;
import com.zto.titans.config.annotation.EnableConfig;
import com.zto.titans.job.annotation.EnableSchedule;
import com.zto.titans.logging.annotation.EnableDynamicLog;
import com.zto.titans.soa.dubbo.annotation.EnableDubbo;
import com.zto.titans.web.annotation.EnableWeb;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @EnableFramework开启Framework,类似SpringBoot的SpringBootApplication注解，封装了部分兼容性问题
 * @EnableSchedule开启了调度模块
 * @EnableMQ开启MQ模块
 * @EnableHttp开启Http模块,使用接口的方式调用Http接口
 * @EnableDubbo开启Dubbo模块，自动检测Dubbo的Service注解及Reference注解
 * @EnableMyBatis开启了Mybatis模块
 * @EnableDynamicLog开启动态日志模块及规范的日志文件及目录 注意:请一定要填写应用的APP_ID,应用框架依赖此ID,生成规则请参考WIKI
 * 填写目录:src/main/resources/META-INF/app.properties
 */
@EnableFramework
@EnableSchedule
@EnableScheduling
@EnableWeb
@EnableDubbo
@EnableDynamicLog
@EnableConfig
@EnableConfigurationProperties(value= TsConfig.class )
public class Application {
    public static void main(String[] args) {
        Main.run(Application.class, args);
    }
}


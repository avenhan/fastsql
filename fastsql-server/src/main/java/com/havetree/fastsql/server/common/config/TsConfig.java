package com.havetree.fastsql.server.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * 作者: 陈征
 * 描述: 用于读取apollo的配置
 * 日期: 2019/10/27 18:53
 */
@Data
@ConfigurationProperties(prefix = "ts")
public class TsConfig {

}

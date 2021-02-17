package com.zto.ts.esbuff.plugin.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Import;

import com.zto.ts.esbuff.plugin.RawPluginInit;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({RawPluginInit.class})
public @interface EnableRawSql 
{

}

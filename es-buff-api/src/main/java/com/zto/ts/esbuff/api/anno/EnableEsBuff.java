package com.zto.ts.esbuff.api.anno;

import com.zto.ts.esbuff.api.Service.EsBuff;
import com.zto.ts.esbuff.api.Service.EsBuffRegister;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EsBuffRegister.class})
@ComponentScans({ @ComponentScan(value = "com.zto.ts.esbuff.api")})
public @interface EnableEsBuff
{

}

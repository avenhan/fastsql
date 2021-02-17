package com.zto.ts.esbuff.api.anno;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EsBuffTime
{
    public String[] value();
}

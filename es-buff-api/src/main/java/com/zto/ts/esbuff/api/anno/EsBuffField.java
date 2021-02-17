package com.zto.ts.esbuff.api.anno;

import java.lang.annotation.*;

/**
 * 非ESbuff的字段赋值
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EsBuffField
{
    /**
     * 来源表的名称
     * @return
     */
    public String table();

    /**
     * esbuff中存的表唯一键名称
     * @return
     */
    public String id();
}

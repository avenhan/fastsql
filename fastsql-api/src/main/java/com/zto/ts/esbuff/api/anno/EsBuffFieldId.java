package com.zto.ts.esbuff.api.anno;

import java.lang.annotation.*;

/**
 * 非ESbuff的字段赋值
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EsBuffFieldId
{

}

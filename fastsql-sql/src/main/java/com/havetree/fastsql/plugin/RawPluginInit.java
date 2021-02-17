package com.havetree.fastsql.plugin;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;


public class RawPluginInit implements ImportSelector
{

    @Override
    public String[] selectImports(AnnotationMetadata arg0)
    {
        return new String[]{"com.zto.ts.esbuff.plugin.RawSqlAware"};
    }

    
}

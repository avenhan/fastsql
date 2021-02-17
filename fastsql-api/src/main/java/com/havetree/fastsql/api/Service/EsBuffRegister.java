package com.zto.ts.esbuff.api.Service;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class EsBuffRegister implements ImportSelector
{
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        return new String[]{"com.zto.ts.esbuff.api.Service.EsBuffAware"};
    }
}

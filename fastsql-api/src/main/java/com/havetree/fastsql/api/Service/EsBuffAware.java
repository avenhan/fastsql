package com.zto.ts.esbuff.api.Service;

import com.zto.titans.common.event.DynamicConfigChangeSpringEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.awt.*;

public class EsBuffAware implements ApplicationListener<DynamicConfigChangeSpringEvent>, EnvironmentAware
{
    private static  EsBuffAware m_pThis;
    private  Environment env;

    @Override
    public void onApplicationEvent(DynamicConfigChangeSpringEvent dynamicConfigChangeSpringEvent) {
        System.out.println("config changed...");
    }

    public static String getProperty(String key)
    {
        return m_pThis.env.getProperty(key);
    }

    @Override
    public void setEnvironment(Environment environment) {
        env = environment;
        m_pThis = this;
    }
}

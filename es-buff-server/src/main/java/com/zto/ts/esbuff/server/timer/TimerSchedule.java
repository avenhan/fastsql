package com.zto.ts.esbuff.server.timer;

import com.zto.titans.common.event.DynamicConfigChangeSpringEvent;
import com.zto.ts.esbuff.api.IVirtualSqlSyncApi;
import com.zto.ts.esbuff.api.Service.EsBuffAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TimerSchedule implements ApplicationListener<DynamicConfigChangeSpringEvent>, EnvironmentAware
{
    @Autowired
    private IVirtualSqlSyncApi virtualSqlSyncApi;

    @Value("${aven.esbuff.sync.timer}")
    private volatile long timerRun;

    private long lastRunTime = 0;
    private Environment env;

    @Scheduled(cron = "0 */1 * * * ?")
    public void onTimer()
    {
        if (timerRun == 0)
        {
            return;
        }

        if (System.currentTimeMillis() - lastRunTime < timerRun - 6000)
        {
            return;
        }

        System.out.println("on timer to sync: " + System.currentTimeMillis());
        log.info("on timer to sync: " + System.currentTimeMillis());
        lastRunTime = System.currentTimeMillis();
        virtualSqlSyncApi.sync();
    }

    @Override
    public void onApplicationEvent(DynamicConfigChangeSpringEvent dynamicConfigChangeSpringEvent) {
        String value = env.getProperty("aven.esbuff.sync.timer");
        if (value == null)
        {
            timerRun = 0;
        }

        try
        {
            Long timer = Long.parseLong(value);
            if (timer != null)
            {
                timerRun = timer.longValue();
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        env = environment;
    }
}

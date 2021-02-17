package com.zto.ts.esbuff.schedule.job;

import com.alibaba.dubbo.config.annotation.Reference;
import com.taobao.pamirs.schedule.IScheduleTaskDealSingle;
import com.taobao.pamirs.schedule.TaskItemDefine;
import com.zto.ts.esbuff.api.IVirtualSqlSyncApi;
import com.zto.ts.esbuff.api.dto.EsBuffSynConfigBo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component("SynVirtualSqlJob")
public class SyncVirtualSqlJob implements IScheduleTaskDealSingle<Object>
{
    @Reference
    private IVirtualSqlSyncApi virtualSqlSyncApi;

    @Override
    public boolean execute(Object esBuffSynConfigBo, String s) throws Exception {
        return true;
    }

    @Override
    public List<Object> selectTasks(String s, String s1, int i, List<TaskItemDefine> list, int i1) throws Exception
    {
        virtualSqlSyncApi.sync();
        return null;
    }

    @Override
    public Comparator<Object> getComparator() {
        return null;
    }
}

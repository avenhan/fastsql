package com.zto.ts.esbuff.schedule.job;


import com.alibaba.dubbo.config.annotation.Reference;
import com.taobao.pamirs.schedule.IScheduleTaskDealSingle;
import com.taobao.pamirs.schedule.TaskItemDefine;
import com.zto.ts.esbuff.api.IScheduleApi;
import com.zto.ts.esbuff.api.dto.EsBuffSynConfigBo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 作者：郑余
 * 描述：同步数据job
 * 时间： 2020/12/7 16:35
 * @param
 * @return
 */
@Slf4j
@Component("SynDataJob")
public class SynDataJob implements IScheduleTaskDealSingle<EsBuffSynConfigBo> {

    @Reference
    IScheduleApi scheduleApi;

    @Override
    public boolean execute(EsBuffSynConfigBo configBo, String s) throws Exception {
        scheduleApi.synData(configBo);
        return true;
    }

    @Override
    public List<EsBuffSynConfigBo> selectTasks(String s, String s1, int i, List<TaskItemDefine> list, int i1) throws Exception {
        return scheduleApi.getAllSynConfig();
    }

    @Override
    public Comparator<EsBuffSynConfigBo> getComparator() {
        return null;
    }
}
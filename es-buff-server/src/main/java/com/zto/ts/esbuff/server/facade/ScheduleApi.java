package com.zto.ts.esbuff.server.facade;

import com.alibaba.dubbo.config.annotation.Service;
import com.zto.titans.common.util.JsonUtil;
import com.zto.ts.esbuff.api.IScheduleApi;
import com.zto.ts.esbuff.api.dto.EsBuffSynConfigBo;
import com.zto.ts.esbuff.dao.pojo.po.EsBuffSynConfigPo;
import com.zto.ts.esbuff.server.entity.SynStatusEnum;
import com.zto.ts.esbuff.server.service.SynConfigService;
import com.zto.ts.esbuff.server.service.SynDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 作者：郑余
 * 描述：定时任务接口
 * 时间：2020/12/7 16:33
 */
@Slf4j
@Service
public class ScheduleApi implements IScheduleApi {

    @Resource
    private SynConfigService synConfigService;
    @Resource
    private SynDataService synDataService;

    @Override
    public void synData(EsBuffSynConfigBo configBo) {
        if (Objects.nonNull(configBo)) {
            EsBuffSynConfigPo configPo = new EsBuffSynConfigPo();
            BeanUtils.copyProperties(configBo, configPo);
            try {
                // 设置同步中
                EsBuffSynConfigPo update = new EsBuffSynConfigPo();
                update.setId(configPo.getId());
                update.setSynStatus((byte) SynStatusEnum.SYN.getValue());
                int result = synConfigService.setSynStatus(SynStatusEnum.IDLE.getValue(), update);
                if (result!=0) {
                    synDataService.scheduleSynData(configPo);
                }
            } catch (Exception e) {
                log.warn("synData fail:{}", JsonUtil.toJSON(configPo));
            }
        }
    }

    @Override
    public List<EsBuffSynConfigBo> getAllSynConfig() {
        List<EsBuffSynConfigBo> boList = new ArrayList<>();
        List<EsBuffSynConfigPo> configList = synConfigService.getAllSynConfig();
        if (!CollectionUtils.isEmpty(configList)) {
            configList.forEach(configPo -> {
                if (Objects.nonNull(configPo)) {
                    EsBuffSynConfigBo configBo = new EsBuffSynConfigBo();
                    BeanUtils.copyProperties(configPo, configBo);
                    boList.add(configBo);
                }
            });
        }
        return boList;
    }
}

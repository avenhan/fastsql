package com.havetree.fastsql.server.facade;

import com.alibaba.dubbo.config.annotation.Service;
import com.zto.titans.common.util.JsonUtil;
import com.zto.ts.esbuff.api.IScheduleApi;
import com.zto.ts.esbuff.api.dto.EsBuffSynConfigBo;
import com.havetree.fastsql.dao.pojo.po.EsBuffSynConfigPo;
import com.havetree.fastsql.server.entity.SynStatusEnum;
import com.havetree.fastsql.server.service.SynConfigService;
import com.havetree.fastsql.server.service.SynDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

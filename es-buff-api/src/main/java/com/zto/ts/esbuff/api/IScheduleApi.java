package com.zto.ts.esbuff.api;

import com.zto.ts.esbuff.api.dto.EsBuffSynConfigBo;

import java.util.List;

/**
 * 作者：郑余
 * 描述：定时任务接口
 * 时间：2020/12/7 16:31
 */
public interface IScheduleApi {

    void synData(EsBuffSynConfigBo configBo);

    List<EsBuffSynConfigBo> getAllSynConfig();
}

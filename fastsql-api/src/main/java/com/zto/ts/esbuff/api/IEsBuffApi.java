package com.zto.ts.esbuff.api;

import com.github.pagehelper.PageInfo;
import com.zto.titans.common.entity.Result;
import com.zto.ts.esbuff.api.dto.SearchConditionDto;
import com.zto.ts.esbuff.api.dto.SynConfigDto;

import java.util.List;

/**
 * 作者：郑余
 * 描述：对外接口
 * 时间：2020/11/29 18:00
 */
public interface IEsBuffApi {
    /**
     * 作者：郑余
     * 描述：检索数据接口
     * 时间： 2020/12/7 14:55
     * @param
     * @return
     */
    Result<PageInfo<String>> searchData(SearchConditionDto searchConditionDto);

    /**
     * 作者：郑余
     * 描述：表同步配置字段新增，reindex操作
     * 时间： 2020/12/7 14:56
     * 操作口令，表名，字段列
     * @param
     * @return
     */
    Result<Integer> buildSynConfig(SynConfigDto synConfigDto);
}

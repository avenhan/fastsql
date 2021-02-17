package com.zto.ts.esbuff.server.facade;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageInfo;
import com.zto.titans.common.entity.Result;
import com.zto.titans.common.util.StringUtil;
import com.zto.ts.esbuff.api.IEsBuffApi;
import com.zto.ts.esbuff.api.dto.SearchConditionDto;
import com.zto.ts.esbuff.api.dto.SynConfigDto;
import com.zto.ts.esbuff.server.service.SearchDataService;
import com.zto.ts.esbuff.server.service.SynConfigService;
import com.zto.ts.esbuff.server.service.SynDataService;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 作者：郑余
 * 描述：rpc接口实现
 * 时间：2020/12/7 14:57
 */
@Service
public class EsBuffApi implements IEsBuffApi {

    @Resource
    private SynConfigService synConfigService;
    @Resource
    private SynDataService synDataService;
    @Resource
    private SearchDataService searchDataService;

    @Override
    public Result<PageInfo<String>> searchData(SearchConditionDto searchConditionDto) {
        if(Objects.isNull(searchConditionDto) || StringUtil.isEmpty(searchConditionDto.getDbSource())
                || StringUtil.isEmpty(searchConditionDto.getDbName()) || StringUtil.isEmpty(searchConditionDto.getTbName())) {
            return Result.error("SYS0001","dbSource dbName tbName should not empty");
        }
        if (searchConditionDto.getDbType()==1&&!searchConditionDto.getDbName().contains(".")) {
            return Result.error("SYS0001","oracle need db and spaceName like: devdb.ZTM");
        }
        return Result.success(searchDataService.searchData(searchConditionDto));
    }

    @Override
    public Result<Integer> buildSynConfig(SynConfigDto synConfigDto) {
        if(Objects.isNull(synConfigDto) || StringUtil.isEmpty(synConfigDto.getDbSource())
            || StringUtil.isEmpty(synConfigDto.getDbName()) || StringUtil.isEmpty(synConfigDto.getTbName())
            || StringUtil.isEmpty(synConfigDto.getFieldMapperJson())) {
            return Result.error("SYS0001","dbSource dbName tbName should not empty");
        }
        if (synConfigDto.getDbType()==1&&!synConfigDto.getDbName().contains(".")) {
            return Result.error("SYS0001","oracle need db and spaceName like: devdb.ZTM");
        }
        return synDataService.reIndexData(synConfigDto);
    }
}

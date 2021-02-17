package com.zto.ts.esbuff.server.service;

import com.github.pagehelper.PageInfo;
import com.zto.ts.esbuff.api.dto.SearchConditionDto;
import com.zto.ts.esbuff.dao.pojo.po.EsBuffSynConfigPo;
import com.zto.ts.esbuff.plugin.sql.RawSqlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 作者：郑余
 * 描述：查询服务
 * 时间：2020/12/9 11:13
 */
@Slf4j
@Service
public class SearchDataService {

    @Resource
    SynConfigService synConfigService;
    @Resource
    ElasticSearchService elasticSearchService;
    @Resource
    RawSqlService rawSqlService;

    // 数据检索
    public PageInfo<String> searchData(SearchConditionDto searchConditionDto) {
        // check config
        PageInfo<String> res = new PageInfo<String>();
        EsBuffSynConfigPo query = new EsBuffSynConfigPo();
        query.setDbType(searchConditionDto.getDbType());
        query.setDbName(searchConditionDto.getDbName());
        query.setDbSource(searchConditionDto.getDbSource());
        query.setTbName(searchConditionDto.getTbName());
        EsBuffSynConfigPo configPo = synConfigService.getSynConfig(query);
        if (Objects.isNull(configPo)) {
            return res;
        }
        Map<String, String> fieldMap = synConfigService.getFieldMap(configPo);
        List<String> ids = elasticSearchService.searchDataByConditions(fieldMap, searchConditionDto);
        // TODO 回查关系库
        res.setList(ids);
        return res;
    }
}

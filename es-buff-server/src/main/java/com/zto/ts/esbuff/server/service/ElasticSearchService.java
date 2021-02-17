package com.zto.ts.esbuff.server.service;

import com.alibaba.fastjson.JSON;
import com.zto.titans.common.util.DateUtil;
import com.zto.titans.common.util.StringUtil;
import com.zto.ts.esbuff.api.dto.SearchConditionDto;
import com.zto.ts.esbuff.dao.pojo.po.EsBuffSynConfigPo;
import com.zto.ts.esbuff.server.entity.BuffDataIndex;
import com.zto.ts.esbuff.server.entity.EsFieldEnum;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.recycler.Recycler;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import scala.annotation.meta.field;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 作者：郑余
 * 描述：es操作
 * 时间：2020/12/7 15:47
 */
@Slf4j
@Service
public class ElasticSearchService {

    private final static String INDEX_NAME_BUFF_DATA = "ts_buff_data_index";
    private final static String INDEX_NAME_BUFF_DATA_TYPE = "_doc";
    private final static String ES_DATA_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private final static String DB_DATA_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final static String[] DID = new String[]{"did"};
    private final static String WILDCARD_QUERY = "*";
    private final static int ES_MAX_SKIP = 10000;

    @Resource(name = "elasticsearchTemplate")
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    // 保存索引数据
    public void saveBuffDataIndex(Map<String, String> dataIndex) {
        IndexQuery query = new IndexQueryBuilder().withIndexName(INDEX_NAME_BUFF_DATA)
                .withType(INDEX_NAME_BUFF_DATA_TYPE).withId(dataIndex.get(EsFieldEnum.ID.getName())).withObject(dataIndex).build();
        elasticsearchRestTemplate.index(query);
    }

    // 批量写入
    public void saveBuffDataIndexMapList(List<Map<String, String>> dataIndexList) {
        List<IndexQuery> indexQueries = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dataIndexList)) {
            dataIndexList.forEach(dataIndex -> {
                indexQueries.add(new IndexQueryBuilder().withIndexName(INDEX_NAME_BUFF_DATA)
                        .withType(INDEX_NAME_BUFF_DATA_TYPE).withId(dataIndex.get(EsFieldEnum.ID.getName())).withObject(dataIndex).build());
            });
        }
        if (!CollectionUtils.isEmpty(indexQueries)) {
            elasticsearchRestTemplate.bulkIndex(indexQueries);
        }
    }

    // 根据es主键id查询
    public BuffDataIndex getEventIndexById(String id) {
        BoolQueryBuilder bool = new BoolQueryBuilder();
        bool.filter(QueryBuilders.termQuery("id",id));
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withIndices(INDEX_NAME_BUFF_DATA)
                .withQuery(bool).build();
        List<BuffDataIndex> resList = elasticsearchRestTemplate.queryForList(searchQuery, BuffDataIndex.class);
        if (resList!=null&&resList.size()>0) {
            return resList.get(0);
        } else {
            return null;
        }
    }

    // 多字段组合检索
    List<String> searchDataByConditions(Map<String, String> fieldMap,  SearchConditionDto searchConditionDto) {
        List<String> ids = new ArrayList<>();
        int pageNum = searchConditionDto.getPageNum();
        int pageSize = searchConditionDto.getPageSize();
        BoolQueryBuilder bool = new BoolQueryBuilder();
        if (!CollectionUtils.isEmpty(searchConditionDto.getConditions())) {
            searchConditionDto.getConditions().forEach(valueCondition -> {
                if (valueCondition.getMatchOption()==0) {// 模糊检索
                    bool.filter(QueryBuilders.wildcardQuery(fieldMap.get(valueCondition.getFieldName()), WILDCARD_QUERY + valueCondition.getFiledValue() + WILDCARD_QUERY));
                } else if (valueCondition.getMatchOption()==1) {// 精确匹配
                    bool.filter(QueryBuilders.termQuery(fieldMap.get(valueCondition.getFieldName()), valueCondition.getFiledValue()));
                } else if (valueCondition.getMatchOption()==2) {// 范围查找
                    String fieldName = fieldMap.get(valueCondition.getFieldName());
                    if (StringUtil.isNotEmpty(fieldName)) {
                        if (checkIsDateField(fieldName)) {
                            bool.filter(QueryBuilders.rangeQuery(fieldName).gte(DateUtil.formatDateTime(DateUtil.parseDateTime(valueCondition.getFrom(), DB_DATA_TIME_FORMAT),ES_DATA_TIME_FORMAT))
                                    .lt(DateUtil.formatDateTime(DateUtil.parseDateTime(valueCondition.getTo(), DB_DATA_TIME_FORMAT),ES_DATA_TIME_FORMAT)));
                        } else {
                            bool.filter(QueryBuilders.rangeQuery(fieldName).gte(valueCondition.getFrom()).lt(valueCondition.getTo()));
                        }
                    }
                }
            });
        }

        SortBuilder sort = SortBuilders.fieldSort(EsFieldEnum.DTIME.getName()).order(SortOrder.ASC);
        if((pageNum*pageSize + pageSize)>ES_MAX_SKIP) {
            int maxPageNum = ES_MAX_SKIP/pageSize;
            pageNum = maxPageNum - 1;
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withIndices(INDEX_NAME_BUFF_DATA).withTypes(INDEX_NAME_BUFF_DATA_TYPE)
                .withSourceFilter(new FetchSourceFilter(DID, null))
                .withQuery(bool).withSort(sort).withPageable(pageable).build();// .withSourceFilter(new FetchSourceFilter(DID, null))
        List<BuffDataIndex> resList = elasticsearchRestTemplate.queryForList(searchQuery, BuffDataIndex.class);
        if (CollectionUtils.isEmpty(resList)) {
            return null;
        } else {
            resList.forEach(dataIndex -> {
                ids.add(dataIndex.getDid());
            });
        }
        return ids;
    }

    // 检查是否是日期字段
    private boolean checkIsDateField(String fieldName) {
        boolean resFlag = false;
        if (EsFieldEnum.DTIME.getName().equals(fieldName)||EsFieldEnum.FT0.getName().equals(fieldName)
                ||EsFieldEnum.FT1.getName().equals(fieldName)||EsFieldEnum.FT2.getName().equals(fieldName)
                ||EsFieldEnum.FT3.getName().equals(fieldName)||EsFieldEnum.FT4.getName().equals(fieldName)) {
            resFlag = true;
        }
        return resFlag;
    }
}

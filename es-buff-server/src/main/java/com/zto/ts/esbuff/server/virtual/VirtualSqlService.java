package com.zto.ts.esbuff.server.virtual;

import com.alibaba.dubbo.config.annotation.Service;
import com.zto.titans.common.util.DateUtil;
import com.zto.titans.common.util.JsonUtil;
import com.zto.titans.common.util.StringUtil;
import com.zto.ts.esbuff.api.IVirtualSqlApi;
import com.zto.ts.esbuff.api.dto.ValueCondition;
import com.zto.ts.esbuff.api.dto.VirtualRealFields;
import com.zto.ts.esbuff.api.dto.VirtualSqlDto;
import com.zto.ts.esbuff.api.en.AnchorType;
import com.zto.ts.esbuff.api.en.TypeEnum;
import com.zto.ts.esbuff.dao.mapper.one.EsBuffVirtualSqlMapper;
import com.zto.ts.esbuff.dao.pojo.po.EsBuffVirtualSqlPo;
import com.zto.ts.esbuff.plugin.sql.DatabaseField;
import com.zto.ts.esbuff.plugin.sql.RawSqlService;
import org.apache.http.HttpStatus;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Service
public class VirtualSqlService implements IVirtualSqlApi
{
    private final static String KEY_WILDCARD_QUERY = "*";
    private final static String INDEX_NAME_BUFF_DATA_TYPE = "_doc";
    private final static String[] DID = new String[]{"did"};
    private final static String KEY_ES_INDEX_ID = "_es_id";
    private final static String KEY_ES_IS_DELETE = "_es_is_deleted";

    private final static String ES_DATA_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private final static String DB_DATA_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final static int ES_MAX_SKIP = 10000;


    @Resource(name = "elasticsearchTemplate")
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private EsBuffVirtualSqlMapper esBuffVirtualSqlMapper;

    @Autowired
    private RawSqlService rawSqlService;

    @Override
    public boolean addSql(VirtualSqlDto rqst) {

        /**
         * 判斷入參
         */
        if (    StringUtil.isEmpty(rqst.getVirtualName()) ||
                StringUtil.isEmpty(rqst.getDbSource()) ||
                StringUtil.isEmpty(rqst.getDbName()) ||
                StringUtil.isEmpty(rqst.getSql()) ||
                rqst.getFields() == null || rqst.getFields().isEmpty() ||
                rqst.getAnchors() == null || rqst.getAnchors().isEmpty()
            )
        {
            throw new RuntimeException("invalid paramters");
        }

        if (rqst.getDbType() == null)
        {
            rqst.setDbType(0);
        }

        String dbType = rawSqlService.getDatabaseType(rqst.getDbSource());
        if (!StringUtil.isEmpty(dbType))
        {
            if (dbType.equals("mysql"))
            {
                rqst.setDbType(0);
            }
            else if (dbType.equals("oracle"))
            {
                rqst.setDbType(1);
            }
            else
            {
                throw  new RuntimeException("invalid database type: "+ rqst.getDbSource());
            }
        }

        if (rqst.getDbType().intValue() > 1)
        {
            throw new RuntimeException("invalid paramters");
        }

        rqst.setSql(rqst.getSql().trim());
        if (rqst.getSql().endsWith(";"))
        {
            rqst.setSql(rqst.getSql().substring(0, rqst.getSql().length() - 1));
        }

        // 查詢數據存在與否
        EsBuffVirtualSqlPo find = new EsBuffVirtualSqlPo();
        BeanUtils.copyProperties(rqst, find);
        find.setRunSql(null);
        find.setSqlMd5(VirtualSqlUtil.md5(rqst.getSql()));
        find.setFields(null);
        find.setUserId(null);
        find.setUserName(null);
        List<EsBuffVirtualSqlPo> items = esBuffVirtualSqlMapper.list(find);
        if (items != null && !items.isEmpty())
        {
            return  false;
        }

        //獲取字段信息並判斷表是否存在
        find.setRunSql(rqst.getSql());
        List list = listFields(find, rqst.getFields());
        Set<String> lstFields = (Set<String>)list.get(0);
        Map<String, Map<String, List<DatabaseField>>> mapReal = (Map<String, Map<String, List<DatabaseField>>>)list.get(1);

        //查詢是否有類似數據
        find = new EsBuffVirtualSqlPo();
        find.setVirtualName(rqst.getVirtualName());
        find.setDeleted(false);
        items = esBuffVirtualSqlMapper.list(find);
        if (items != null && !items.isEmpty())
        {
            // check fields
            String existJsonFields = items.get(0).getFields();
            checkFieldTypes(existJsonFields, rqst.getFields());
        }

        boolean hasUpdateTime = false;
        // 确认主键是否在sql字段内
        for (Map.Entry<String, AnchorType> entry : rqst.getAnchors().entrySet())
        {
            if (entry.getValue() == AnchorType.UPDATE_TIME)
            {
                hasUpdateTime = true;
                continue;
            }

            if (entry.getValue() != AnchorType.UNIQUE_KEY)
            {
                continue;
            }

            if (!lstFields.contains(entry.getKey()))
            {
                throw new RuntimeException("主键必须在sql的结果中展示出来");
            }
        }

        //保存數據
        find = new EsBuffVirtualSqlPo();
        BeanUtils.copyProperties(rqst, find);
        find.setFields(JsonUtil.toJSON(rqst.getFields()));
        find.setRunSql(rqst.getSql());
        find.setSqlMd5(VirtualSqlUtil.md5(rqst.getSql()));
        find.setAnchor(JsonUtil.toJSON(rqst.getAnchors()));
        find.setDbFields(JsonUtil.toJSON(mapReal));

        int ret = esBuffVirtualSqlMapper.insert(find);
        if (ret > 0)
        {
            return  true;
        }

        return false;
    }

    @Override
    public boolean updateSql(List<VirtualSqlDto> sqls) {
        return false;
    }

    @Override
    public List<Map<String, Object>> search(List<VirtualRealFields> lstRealFields, String virtualName, List<ValueCondition> conditions, Integer page, Integer pageSize, String sortField, String sort)
    {
        if (StringUtil.isEmpty(virtualName))
        {
            throw new RuntimeException("invalid virtual name");
        }

        EsBuffVirtualSqlPo validSql = getValidVirtualSql(virtualName);
        Map<String, TypeEnum> mapFields = VirtualSqlUtil.getFieldTypeFromJson(validSql.getFields());
        if (mapFields == null || mapFields.isEmpty())
        {
            throw new RuntimeException("解析字段類型錯誤");
        }

        BoolQueryBuilder bool = new BoolQueryBuilder();
        bool.filter(QueryBuilders.termQuery(KEY_ES_IS_DELETE, false));

        if (!CollectionUtils.isEmpty(conditions))
        {
            conditions.forEach(condition->{

                TypeEnum typeEnum = mapFields.get(condition.getFieldName());
                if (condition.getMatchOption()==0) {// 模糊检索
                    bool.filter(QueryBuilders.wildcardQuery(condition.getFieldName(), KEY_WILDCARD_QUERY + condition.getFiledValue() + KEY_WILDCARD_QUERY));
                } else if (condition.getMatchOption()==1) {// 精确匹配
                    bool.filter(QueryBuilders.termQuery(condition.getFieldName(), condition.getFiledValue()));
                } else if (condition.getMatchOption()==2) {// 范围查找
                    if (typeEnum == null)
                    {
                        bool.filter(QueryBuilders.rangeQuery(condition.getFieldName()).gte(condition.getFrom()).lt(condition.getTo()));
                    }
                    else if (typeEnum == TypeEnum.DATE)
                    {
                        bool.filter(QueryBuilders.rangeQuery(condition.getFieldName()).gte(DateUtil.formatDateTime(DateUtil.parseDateTime(condition.getFrom(), DB_DATA_TIME_FORMAT),ES_DATA_TIME_FORMAT))
                                .lt(DateUtil.formatDateTime(DateUtil.parseDateTime(condition.getTo(), DB_DATA_TIME_FORMAT),ES_DATA_TIME_FORMAT)));
                    }
                    else{
                        bool.filter(QueryBuilders.rangeQuery(condition.getFieldName()).gte(condition.getFrom()).lt(condition.getTo()));
                    }
                }
            });
        }

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if (pageSize > 0 && page > 0)
        {
            int pos = (page - 1) * pageSize;
            if (pos + pageSize > ES_MAX_SKIP)
            {
                int maxPageNum = ES_MAX_SKIP/ pageSize;
                page = maxPageNum - 1;
            }

            pos = (page - 1) * pageSize;

            sourceBuilder.from(pos);
            sourceBuilder.size(pageSize);
        }

        if (!StringUtil.isEmpty(sortField) && !StringUtil.isEmpty(sort))
        {
            FieldSortBuilder sortBuilder = SortBuilders.fieldSort(sortField);
            if (sort.equals("asc"))
            {
                sortBuilder.order(SortOrder.ASC);
            }
            else
            {
                sortBuilder.order(SortOrder.DESC);
            }

            sourceBuilder.sort(sortBuilder);
        }

        SearchRequest searchRequest = new SearchRequest(validSql.getVirtualName());
        searchRequest.source(sourceBuilder);
        sourceBuilder.query(bool);

        try
        {
            List<Map<String, Object>> lstMap = new ArrayList<>();
            SearchResponse response = elasticsearchRestTemplate.getClient().search(searchRequest, RequestOptions.DEFAULT);
            if (HttpStatus.SC_OK != response.status().getStatus())
            {
                throw new RuntimeException("invalid search result: " + response.status());
            }

            SearchHit[] hits = response.getHits().getHits();
            if (hits == null || hits.length < 1)
            {
                return lstMap;
            }

            for (SearchHit searchHit : hits)
            {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                lstMap.add(toValidDataMap(sourceAsMap, mapFields));
            }

            return lstMap;
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long count(String virtualName, List<ValueCondition> conditions)
    {
        if (StringUtil.isEmpty(virtualName))
        {
            throw new RuntimeException("invalid virtual name");
        }

        EsBuffVirtualSqlPo validSql = getValidVirtualSql(virtualName);
        Map<String, TypeEnum> mapFields = VirtualSqlUtil.getFieldTypeFromJson(validSql.getFields());
        if (mapFields == null || mapFields.isEmpty())
        {
            throw new RuntimeException("解析字段類型錯誤");
        }

        BoolQueryBuilder bool = new BoolQueryBuilder();
        bool.filter(QueryBuilders.termQuery(KEY_ES_IS_DELETE, false));

        if (!CollectionUtils.isEmpty(conditions))
        {
            conditions.forEach(condition->{

                TypeEnum typeEnum = mapFields.get(condition.getFieldName());
                if (condition.getMatchOption()==0) {// 模糊检索
                    bool.filter(QueryBuilders.wildcardQuery(condition.getFieldName(), KEY_WILDCARD_QUERY + condition.getFiledValue() + KEY_WILDCARD_QUERY));
                } else if (condition.getMatchOption()==1) {// 精确匹配
                    bool.filter(QueryBuilders.termQuery(condition.getFieldName(), condition.getFiledValue()));
                } else if (condition.getMatchOption()==2) {// 范围查找
                    if (typeEnum == null)
                    {
                        bool.filter(QueryBuilders.rangeQuery(condition.getFieldName()).gte(condition.getFrom()).lt(condition.getTo()));
                    }
                    else if (typeEnum == TypeEnum.DATE)
                    {
                        bool.filter(QueryBuilders.rangeQuery(condition.getFieldName()).gte(DateUtil.formatDateTime(DateUtil.parseDateTime(condition.getFrom(), DB_DATA_TIME_FORMAT),ES_DATA_TIME_FORMAT))
                                .lt(DateUtil.formatDateTime(DateUtil.parseDateTime(condition.getTo(), DB_DATA_TIME_FORMAT),ES_DATA_TIME_FORMAT)));
                    }
                    else{
                        bool.filter(QueryBuilders.rangeQuery(condition.getFieldName()).gte(condition.getFrom()).lt(condition.getTo()));
                    }
                }
            });
        }

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        CountRequest countRequest = new CountRequest(validSql.getVirtualName());
        countRequest.source(sourceBuilder);
        sourceBuilder.query(bool);

        try
        {
            CountResponse rspd = elasticsearchRestTemplate.getClient().count(countRequest, RequestOptions.DEFAULT);
            if (HttpStatus.SC_OK != rspd.status().getStatus())
            {
                throw new RuntimeException("invalid count result: " + rspd.status());
            }

            long count =  rspd.getCount();
            return  count;
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> toValidDataMap(Map<String, Object> mapIn, Map<String, TypeEnum> mapFields)
    {
        Map<String, Object> mapRet = new HashMap<>();

        for (Map.Entry<String, Object> entry : mapIn.entrySet())
        {
            TypeEnum typeEnum = mapFields.get(entry.getKey());
            if (typeEnum == null)
            {
                continue;
            }

            if (entry.getValue() == null)
            {
                mapRet.put(entry.getKey(), null);
                continue;
            }

            if (typeEnum != TypeEnum.STRING && entry.getValue().equals(""))
            {
                mapRet.put(entry.getKey(), null);
                continue;
            }

            Object value = null;
            if (typeEnum == TypeEnum.DATE)
            {
                value = getDateAsNormal(entry.getValue());
            }
            else if (typeEnum == TypeEnum.LONG)
            {
                value = Long.parseLong(entry.getValue().toString());
            }
            else if (typeEnum == TypeEnum.DOUBLE)
            {
                value = Double.parseDouble(entry.getValue().toString());
            }
            else if (typeEnum == TypeEnum.DECIMAL)
            {
                value = BigDecimal.valueOf(Double.parseDouble(entry.getValue().toString()));
            }
            else if (typeEnum == TypeEnum.INT)
            {
                value = Integer.parseInt(entry.getValue().toString());
            }
            else if (typeEnum == TypeEnum.BOOLEAN)
            {
                String vv = entry.getValue().toString();
                if (vv.equals("0"))
                {
                    value = false;
                }
                if (vv.equals("1"))
                {
                    value = true;
                }
                else
                {
                    try
                    {
                        value = Boolean.parseBoolean(vv);
                    }
                    catch (Throwable e)
                    {
                        value = true;
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                value = entry.getValue();
            }

            mapRet.put(entry.getKey(), value);
        }

        return  mapRet;
    }

    private Date getDateAsNormal(Object value)
    {
        Date time = null;
        if (value instanceof  Date)
        {
            time = (Date) value;
        }
        else
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ES_DATA_TIME_FORMAT);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            try
            {
                time = simpleDateFormat.parse(value.toString());
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        }

//        TimeZone zone = TimeZone.getDefault();
//        int offset = zone.getRawOffset();
//
//        long timeMs = time.getTime()+ offset;
//
//        return new Date(timeMs);

        return  time;
    }

    private List listFields(EsBuffVirtualSqlPo find, Map<String, TypeEnum> mapFields)
    {
        String dbName = find.getDbName();
        String tableSpace = dbName;
        int index = dbName.indexOf('.');
        if (index >= 0)
        {
            tableSpace = dbName.substring(index + 1);
            dbName = dbName.substring(0, index);
        }

        StringBuilder sql = new StringBuilder(find.getRunSql()).append(" limit 0, 1");
        if (find.getDbType() == 1)
        {
            sql = new StringBuilder(find.getRunSql()).append(" WHERE ROWNUM >0 && ROWNUM < 2");
        }

        Map<String, Map<String, List<DatabaseField>>> mapRealFields = rawSqlService.getDatabaseFieldsWithSql(Map.class, find.getDbSource(), dbName, sql.toString());

        List<Map> lstRet = rawSqlService.list(Map.class, find.getDbSource(), dbName, sql.toString());
        if (lstRet == null || lstRet.isEmpty())
        {
            throw new RuntimeException("初始數據庫裡的數據不可為空");
        }

        Map<String, Object> one = lstRet.get(0);
        if (one.size() != mapFields.size())
        {
            throw new RuntimeException("入參錯誤，字段數量跟數據庫中存在的有差異");
        }

        one.keySet().forEach(field->{
            if (!mapFields.containsKey(field))
            {
                throw new RuntimeException("入參錯誤，數據字段中不存在與傳入字段值");
            }
        });

        List<Object> lstRetAll = new ArrayList<>();
        lstRetAll.add(one.keySet());
        lstRetAll.add(mapRealFields);

        return lstRetAll;
    }

    private boolean checkEqaul(Set<String> fields, List<String> lstExist)
    {
        Set<String> setExist = new TreeSet<String>();
        for (String field: lstExist)
        {
            setExist.add(field);
        }

        Set<String> setFields = new TreeSet<String>();
        for (String field: fields)
        {
            setFields.add(field);
        }

        if (setExist.size() != setFields.size())
        {
            return  false;
        }

        for (String field: setFields )
        {
            if (!setExist.contains(field))
            {
                return  false;
            }
        }

        return  true;
    }

    public void checkFieldTypes(String json, Map<String, TypeEnum> mapFields)
    {
        Map<String, TypeEnum> mapExist = VirtualSqlUtil.getFieldTypeFromJson(json);
        if (mapExist == null || mapExist.size() != mapFields.size())
        {
            throw new RuntimeException("錯誤的數據字段，已有字段不匹配");
        }

        for (Map.Entry<String, TypeEnum> entry : mapFields.entrySet())
        {
            TypeEnum addType = mapFields.get(entry.getKey());
            if (addType == null || entry.getValue() == null || addType != entry.getValue())
            {
                throw new RuntimeException("錯誤的數據字段，已有字段不匹配");
            }
        }
    }

    private EsBuffVirtualSqlPo getValidVirtualSql(String virtualName)
    {
        EsBuffVirtualSqlPo find = new EsBuffVirtualSqlPo();
        find.setVirtualName(virtualName);
        List<EsBuffVirtualSqlPo> lstSql = esBuffVirtualSqlMapper.list(find);
        if (lstSql == null || lstSql.isEmpty())
        {
            throw new RuntimeException("虛擬表不存在");
        }

        EsBuffVirtualSqlPo valid = null;
        for (EsBuffVirtualSqlPo virtualSqlPo : lstSql)
        {
            if (virtualSqlPo.getStatus() > 1)
            {
                valid = virtualSqlPo;
                break;
            }
        }

        if (valid == null)
        {
            throw new RuntimeException("數據還未同步，請創建ES索引，並設置開始同步");
        }

        return  valid;
    }

}

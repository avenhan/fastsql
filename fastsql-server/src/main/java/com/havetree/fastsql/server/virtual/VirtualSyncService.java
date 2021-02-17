package com.havetree.fastsql.server.virtual;

import com.alibaba.dubbo.config.annotation.Service;
import com.zto.ts.esbuff.api.IVirtualSqlSyncApi;
import com.zto.ts.esbuff.api.en.AnchorType;
import com.zto.ts.esbuff.api.en.TypeEnum;
import com.havetree.fastsql.dao.mapper.one.EsBuffVirtualSqlMapper;
import com.havetree.fastsql.dao.pojo.po.EsBuffVirtualSqlPo;
import com.havetree.fastsql.plugin.sql.RawSqlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Service
public class VirtualSyncService implements IVirtualSqlSyncApi
{
    private final static String INDEX_NAME_BUFF_DATA_TYPE = "_doc";
    private final static String KEY_ES_INDEX_ID = "_es_id";
    private final static String KEY_ES_IS_DELETE = "_es_is_deleted";
    private final static String ES_DATA_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private final static String DB_DATA_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";


    private static final int KEY_TIME_LIMIT = 60000 * 5;
    private static  final int KEY_LIMIT = 1000;
    private static  final int KEY_TIME_SYNC_TIMEOUT = 60000 * 5;

    @Resource(name = "elasticsearchTemplate")
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private EsBuffVirtualSqlMapper esBuffVirtualSqlMapper;

    @Autowired
    private RawSqlService rawSqlService;

    static private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void sync() {
        doSync();
    }

    public void doSync()
    {
        EsBuffVirtualSqlPo find = new EsBuffVirtualSqlPo();
        find.setDeleted(false);

        if (threadPoolExecutor == null)
        {
            threadPoolExecutor =  new ThreadPoolExecutor(4, 4,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
        }

        List<EsBuffVirtualSqlPo> items = esBuffVirtualSqlMapper.list(find);
        items.forEach(one->{
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    syncOneSql(one);
                }
            });
        });
    }

    private void syncOneSql(EsBuffVirtualSqlPo sql)
    {
        if (sql.getStatus() < 1)
        {
            return;
        }

        //在同步的，并未超时的数据，不做
        if (sql.getStatus() == 2)
        {
            Date now = new Date();
            if (now.getTime() < sql.getUpdateTime().getTime() + KEY_TIME_SYNC_TIMEOUT)
            {
                return;
            }
        }

        Date startTime = sql.getSyncTime();
        if (sql.getSyncTime() == null)
        {
            startTime = getMinDataTime(sql);
        }

        syncTime(sql, startTime);
    }

    private Date getMinDataTime(EsBuffVirtualSqlPo sqlPo)
    {
//        Map<String, AnchorType> mapAnchor = VirtualSqlUtil.getAnchor(sqlPo.getAnchor());
//        StringBuilder sql = new StringBuilder("select ");
//
//        int index = 0;
//        for (Map.Entry<String, AnchorType> entry : mapAnchor.entrySet())
//        {
//            if (entry.getValue() == AnchorType.UPDATE_TIME)
//            {
//                if (index > 0)
//                {
//                    sql.append(", ");
//                }
//                sql.append(entry.getKey()).append(" as u").append(index);
//                index++;
//            }
//        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM
        try
        {
            Date startTime = simpleDateFormat.parse("2000-01-01 00:00:00");
            return startTime;
        }
        catch (Throwable e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private void syncTime(EsBuffVirtualSqlPo sql, Date startTime)
    {
        // 每5分鐘形成一個查詢列表
        long posTime = startTime.getTime();
        EsBuffVirtualSqlPo update = null;
        long updateTime = System.currentTimeMillis();
        boolean isBegin = false;
        int step = KEY_TIME_LIMIT;

        while (true)
        {
            Date now = new Date();
            long endTime = posTime + step;
            boolean isEnd = false;
            if (endTime > now.getTime())
            {
                endTime = now.getTime();
                isEnd = true;
            }

            if (isBegin == false)
            {
                // 设置同步中
                update = new EsBuffVirtualSqlPo();
                update.setId(sql.getId());
                update.setStatus(2);
                update.setUpdateTime(new Date());
                isBegin = true;
                esBuffVirtualSqlMapper.update(update);
            }

            int count = 0;
            count = syncTime(sql, new Date(posTime - 1000), new Date(endTime));

            if (count < KEY_LIMIT)
            {
                step += step;
            }
            else
            {
                step = KEY_TIME_LIMIT;
            }

            if (step < KEY_TIME_LIMIT)
            {
                step = KEY_TIME_LIMIT;
            }

            update = new EsBuffVirtualSqlPo();
            update.setId(sql.getId());
            update.setSyncTime(new Date(endTime));
            update.setSynced(true);
            update.setUpdateTime(new Date());
            if (System.currentTimeMillis() - updateTime > 1000)
            {
                updateTime = System.currentTimeMillis();
                esBuffVirtualSqlMapper.update(update);
            }

            if (isEnd)
            {
                break;
            }

            posTime = endTime;
        }

        if (update != null)
        {
            // 设置同步完成
            update.setStatus(3);
            esBuffVirtualSqlMapper.update(update);
        }
    }

    private int syncUpdateTimeNull(EsBuffVirtualSqlPo sqlPo)
    {
        Map<String, AnchorType> mapAnchor = VirtualSqlUtil.getAnchor(sqlPo.getAnchor());
        Map<String, TypeEnum> mapTypes = VirtualSqlUtil.getFieldTypeFromJson(sqlPo.getFields());

        int pos = 0;
        while (true)
        {
            List<Map> items = syncTime(sqlPo, null, null, mapAnchor, mapTypes, pos, KEY_LIMIT);
            if (items == null || items.isEmpty())
            {
                break;
            }

            pos += items.size();
            if (items.size() < KEY_LIMIT)
            {
                break;
            }
        }

        return  pos;
    }

    private int syncTime(EsBuffVirtualSqlPo sqlPo, Date startTime, Date endTime)
    {
        Map<String, AnchorType> mapAnchor = VirtualSqlUtil.getAnchor(sqlPo.getAnchor());
        Map<String, TypeEnum> mapTypes = VirtualSqlUtil.getFieldTypeFromJson(sqlPo.getFields());

        int pos = 0;
        while (true)
        {
            List<Map> items = syncTime(sqlPo, startTime, endTime, mapAnchor, mapTypes, pos, KEY_LIMIT);
            if (items == null || items.isEmpty())
            {
                break;
            }

            pos += items.size();
            if (items.size() < KEY_LIMIT)
            {
                break;
            }
        }

        return  pos;
    }

    private List<Map>  syncTime(EsBuffVirtualSqlPo sqlPo, Date startTime, Date endTime,
                          Map<String, AnchorType> mapAnchor, Map<String, TypeEnum> mapTypes,
                          int pos, int limit)
    {
        String dbName = sqlPo.getDbName();
        String tableSpace = dbName;
        int index = dbName.indexOf('.');
        if (index >= 0)
        {
            tableSpace = dbName.substring(index + 1);
            dbName = dbName.substring(0, index);
        }

        StringBuilder sql = new StringBuilder(sqlPo.getRunSql());
        sql.append(" where 1=1 ");
        List<Object> lstObjs = new ArrayList<>();
        if (startTime != null && endTime != null)
        {
            sql.append(" and ( ");
            boolean hasAdd = false;
            for (Map.Entry<String, AnchorType> entry : mapAnchor.entrySet())
            {
                if (entry.getValue() == AnchorType.UPDATE_TIME)
                {
                    if (hasAdd == true)
                    {
                        sql.append(" or ");
                    }
                    sql.append(" ( ").append(entry.getKey()).append(" > ? and ").append(entry.getKey()).append(" < ? )");
                    lstObjs.add(startTime);
                    lstObjs.add(endTime);
                    hasAdd = true;
                }
            }
            sql.append(" ) ");
        }
        else
        {
            sql.append(" and ( ");
            boolean hasAdd = false;
            for (Map.Entry<String, AnchorType> entry : mapAnchor.entrySet())
            {
                if (entry.getValue() == AnchorType.UPDATE_TIME)
                {
                    if (hasAdd == true)
                    {
                        sql.append(" or ");
                    }
                    sql.append(entry.getKey()).append(" is null ");
                    hasAdd = true;
                }
            }

            sql.append(" ) ");
        }

        if (sqlPo.getDbType() == 0)
        {
            sql.append(" limit ").append(pos).append(", ").append(limit);
        }
        else
        {
            sql.append(" ROWNUM > ").append(pos).append(" && ROWNUM < ").append(pos + limit);
        }

        Object[] arrObjs = lstObjs.toArray(new Object[lstObjs.size()]);
        List<Map> items = rawSqlService.list(Map.class, sqlPo.getDbSource(), dbName,
                sql.toString(), arrObjs);

        List<Map> lstEsItems = new ArrayList<>();
        for (Map<String, Object> entry : items)
        {
            String id = getKeyFromData(sqlPo, entry, mapAnchor);
            if (id == null || id.isEmpty())
            {
                continue;
            }

            convertTime(entry, mapTypes);
            entry.put(KEY_ES_INDEX_ID, id);
            entry.put(KEY_ES_IS_DELETE, false);
            lstEsItems.add(entry);
        }

        saveBuffDataIndexMapList(sqlPo.getVirtualName(), lstEsItems);
        return items;
    }

    public void saveBuffDataIndexMapList(String virtualName, List<Map> lstMap)
    {
        List<IndexQuery> indexQueries = new ArrayList<>();
        if (CollectionUtils.isEmpty(lstMap))
        {
            return;
        }

        lstMap.forEach(map ->
        {
            String esId = map.get(KEY_ES_INDEX_ID).toString();
            indexQueries
                    .add(new IndexQueryBuilder().withIndexName(virtualName)
                    .withType(INDEX_NAME_BUFF_DATA_TYPE)
                    .withId(esId)
                    .withObject(map).build());
        });

        if (CollectionUtils.isEmpty(indexQueries))
        {
            return;
        }

        elasticsearchRestTemplate.bulkIndex(indexQueries);
    }

    private String getKeyFromData(EsBuffVirtualSqlPo sqlPo, Map<String, Object> map, Map<String, AnchorType> mapAnchor)
    {
        StringBuilder b = new StringBuilder("").append(sqlPo.getId());
        for (Map.Entry<String, AnchorType> entry : mapAnchor.entrySet())
        {
            if (entry.getValue() == AnchorType.UNIQUE_KEY)
            {
                if (entry.getKey() == null)
                {
                    return null;
                }

                b.append(".").append(map.get(entry.getKey()));
            }
        }

        return b.toString();
    }

    private void convertTime(Map<String, Object> map, Map<String, TypeEnum> mapFields)
    {
        Map<String, Object> mapTime = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            TypeEnum typeEnum = mapFields.get(entry.getKey());
            if (typeEnum == null || typeEnum != TypeEnum.DATE)
            {
                continue;
            }
            if (entry.getValue() == null)
            {
                continue;
            }

            String ccTime = getUTCTimeAsString((String)entry.getValue());
            mapTime.put(entry.getKey(), ccTime);
        }

        map.putAll(mapTime);
    }

    private Date getUTCTime(String time)
    {
        TimeZone zone = TimeZone.getDefault();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DB_DATA_TIME_FORMAT);//注意月份是MM
        try
        {
            Date startTime = simpleDateFormat.parse(time);
            return new Date(startTime.getTime() - zone.getRawOffset());
        }
        catch (Throwable e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private String getUTCTimeAsString(String time)
    {
        Date date = getUTCTime(time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ES_DATA_TIME_FORMAT);
        return  simpleDateFormat.format(date);
    }

}

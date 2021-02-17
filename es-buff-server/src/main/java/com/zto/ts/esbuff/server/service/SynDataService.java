package com.zto.ts.esbuff.server.service;

import com.zto.titans.common.entity.Result;
import com.zto.titans.common.util.DateUtil;
import com.zto.titans.common.util.StringUtil;
import com.zto.ts.esbuff.api.dto.SynConfigDto;
import com.zto.ts.esbuff.dao.pojo.po.EsBuffSynConfigPo;
import com.zto.ts.esbuff.plugin.sql.RawSqlService;
import com.zto.ts.esbuff.server.common.config.TsConfig;
import com.zto.ts.esbuff.server.entity.BuffDataIndex;
import com.zto.ts.esbuff.server.entity.DbTypeEnum;
import com.zto.ts.esbuff.server.entity.EsFieldEnum;
import com.zto.ts.esbuff.server.entity.SynStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

/**
 * 作者：郑余
 * 描述：同步数据服务
 * 时间：2020/12/9 11:09
 */
@Slf4j
@Service
public class SynDataService {

    @Resource
    SynConfigService synConfigService;
    @Resource
    ElasticSearchService elasticSearchService;
    @Resource
    RawSqlService rawSqlService;
    @Resource
    TsConfig tsConfig;
    private final static Map<String, Boolean> TB_REINDEX_FLAG_MAP = new HashMap<>();// 表重建同步数据开关状态，true进行中，false终止
    private final static String ES_DATA_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private final static String DB_DATA_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final static String FIRST_DATA_TIME = "2000-01-01 00:00:01";
    private final static int PAGE_SIZE = 1000;
    private final static int NO_DATA_BATCH = 10;// 重建批次时长10天

    public Map<String, Boolean> getTbReindexFlagMap() {
        return TB_REINDEX_FLAG_MAP;
    }

    /**
     * 作者：郑余
     * 描述：定时同步单表增量数据
     * 时间： 2020/12/9 13:36
     * 分页查询数据
     * 构建同步sql语句
     * 查询结果写入es
     * 最新同步时间，恢复空闲状态
     * @param
     * @return
     */
    public int scheduleSynData(EsBuffSynConfigPo configPo) {
        Date endDate = DateUtils.addMinutes(configPo.getSynTime(), configPo.getBatchTime());
        Date lastDataTime = synData(configPo, configPo.getSynTime(), endDate);
        EsBuffSynConfigPo update = new EsBuffSynConfigPo();
        update.setId(configPo.getId());
        update.setSynTime(lastDataTime);
        synConfigService.updateSynTimeAndStatus(update);
        return 1;
    }

    /**
     * 作者：郑余
     * 描述：重新构建同步数据
     * 时间： 2020/12/9 13:37
     * @param
     * @return
     */
    public Result reIndexData(SynConfigDto synConfigDto) {
        // 构建配置
        Result res = synConfigService.buildSynConfig(synConfigDto);
        // 重新同步数据
        Date now = new Date();
        Date lastDataTime = new Date(DateUtil.parseDateTime(FIRST_DATA_TIME,DB_DATA_TIME_FORMAT).toInstant(ZoneOffset.of("+8")).toEpochMilli());
        if (res.isStatus()&&Objects.nonNull(res.getResult())) {
            EsBuffSynConfigPo configPo = (EsBuffSynConfigPo) res.getResult();
            configPo.setSynStatus((byte) SynStatusEnum.RE_SYN.getValue());
            // 更新重建中状态
            int success = synConfigService.setSynStatus((byte)SynStatusEnum.IDLE.getValue(), configPo);
            if (success<0) {
                return Result.success(0);
            }

            // 重建同步数据直到当前时间
            String key = synConfigDto.getDbSource()+synConfigDto.getDbName()+synConfigDto.getTbName();
            TB_REINDEX_FLAG_MAP.put(key, true);
            Date endDate = DateUtils.addDays(lastDataTime, NO_DATA_BATCH);
            while (TB_REINDEX_FLAG_MAP.get(key)&&lastDataTime.before(now)) {
                log.info("reIndexData {}:{}:{}->{}->{}", synConfigDto.getDbSource(),synConfigDto.getDbName(),synConfigDto.getTbName(),
                        DateUtil.formatDateTime(LocalDateTime.ofInstant(lastDataTime.toInstant(), ZoneId.of("+8")),DB_DATA_TIME_FORMAT),
                        DateUtil.formatDateTime(LocalDateTime.ofInstant(now.toInstant(), ZoneId.of("+8")),DB_DATA_TIME_FORMAT));
                Date resDate = synData(configPo, lastDataTime, endDate);
                if (Objects.nonNull(resDate)) {
                    lastDataTime = DateUtils.addSeconds(resDate, 1);// 精确到秒
                    EsBuffSynConfigPo update = new EsBuffSynConfigPo();
                    update.setId(configPo.getId());
                    update.setSynTime(lastDataTime);
                    synConfigService.updateReSynTime(update);// 有实际同步到数据时，记录同步时间点
                } else {
                    lastDataTime = endDate;
                }
                endDate = DateUtils.addDays(lastDataTime, NO_DATA_BATCH);
            }
            // 恢复空闲状态
            configPo.setSynStatus((byte)SynStatusEnum.IDLE.getValue());
            synConfigService.setSynStatus((byte)SynStatusEnum.RE_SYN.getValue(), configPo);
        }
        return Result.success(1);
    }

    // 单次同步数据
    private Date synData(EsBuffSynConfigPo configPo, Date startDate, Date endDate) {
        int pageSize = PAGE_SIZE;
        int pageNum = 0;
        Date lastDataTime = null;
        while (pageSize==PAGE_SIZE) {
            List<Map> queryResult = queryFromDb(configPo, pageSize, pageNum, startDate, endDate);
            if (!CollectionUtils.isEmpty(queryResult)) {
                List<Map<String, String>> resList = builderBuffDataIndexList(configPo, queryResult);
                Map<String, String> lastIndex =  resList.get(resList.size()-1);
                if (Objects.nonNull(lastIndex)) {
                    lastDataTime = new Date(DateUtil.parseDateTime(lastIndex.get(EsFieldEnum.DTIME.getName()), ES_DATA_TIME_FORMAT).toInstant(ZoneOffset.of("+8")).toEpochMilli());
                }
                elasticSearchService.saveBuffDataIndexMapList(resList);
            }
            pageSize = queryResult.size();
            pageNum++;
        }
        return lastDataTime;
    }

    // 查询db
    private List<Map> queryFromDb(EsBuffSynConfigPo configPo, int pageSize, int pageNum, Date startDate, Date endDate) {
        if (Objects.isNull(startDate)) {
            return null;
        }
        String startTime = DateUtil.formatDateTime(LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.of("+8")),"yyyy-MM-dd HH:mm:ss");
        String endTime = DateUtil.formatDateTime(LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.of("+8")),"yyyy-MM-dd HH:mm:ss");
        int startRow = 0;
        int endRow = 0;
        if (DbTypeEnum.MYSQL.getType()==configPo.getDbType()) {
            startRow = pageNum*pageSize;
            endRow = pageSize;
        } else if (DbTypeEnum.ORACLE.getType()==configPo.getDbType()) {
            startRow = pageNum*pageSize;
            endRow = pageNum*pageSize + pageSize;
        }
        String synSql = synConfigService.builderSynDataSql(configPo);
        String dbName = configPo.getDbName();
        dbName = dbName.split("\\.")[0];
        return rawSqlService.list(Map.class, configPo.getDbSource(), dbName, synSql, startTime, endTime, startRow, endRow);
    }

    // 转换sql查询结果->es入库
    private List<Map<String,String>> builderBuffDataIndexList(EsBuffSynConfigPo configPo, List<Map> queryResult) {
        List<Map<String,String>> mapList = new ArrayList<>();
        String now = DateUtil.formatDateTime(DateUtil.newDateTime(),"yyyy-MM-dd'T'HH:mm:ss.SSS");
        Map<String, String> fieldMapper = synConfigService.getFieldMap(configPo);
        Map<String, String> esFieldMapper = synConfigService.getEsFieldMap(fieldMapper);
        queryResult.forEach(record->{
            String recordId = "";
            if (record.get(esFieldMapper.get(EsFieldEnum.DID.getName())) instanceof String) {
                recordId = (String) record.get(esFieldMapper.get(EsFieldEnum.DID.getName()));
            }
            String recordTime = null;
            if (Objects.nonNull(record.get(esFieldMapper.get(EsFieldEnum.DTIME.getName())))) {
                if (record.get(esFieldMapper.get(EsFieldEnum.DTIME.getName())) instanceof Date) {
                    recordTime = DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date) record.get(esFieldMapper.get(EsFieldEnum.DTIME.getName()))).toInstant(), ZoneId.of("+8")),ES_DATA_TIME_FORMAT);
                } else if (record.get(esFieldMapper.get(EsFieldEnum.DTIME.getName())) instanceof String) {
                    recordTime = (String) record.get(esFieldMapper.get(EsFieldEnum.DTIME.getName()));
                    recordTime = DateUtil.formatDateTime(DateUtil.parseDateTime(recordTime, DB_DATA_TIME_FORMAT),ES_DATA_TIME_FORMAT);
                }
            }
            String recordDelete = (String) record.get(esFieldMapper.get(EsFieldEnum.ISDEL.getName()));
            if (StringUtil.isNotEmpty(recordId)&&StringUtil.isNotEmpty(recordTime)&&StringUtil.isNotEmpty(recordDelete)) {
                Map<String, String> indexMap = new HashMap<>();
                indexMap.put(EsFieldEnum.ID.getName(), configPo.getDbName()+"#"+configPo.getTbName()+"#" + recordId);
                indexMap.put(EsFieldEnum.DID.getName(), recordId);
                indexMap.put(EsFieldEnum.DTIME.getName(), recordTime);
                indexMap.put(EsFieldEnum.ISDEL.getName(), recordDelete);
                indexMap.put(EsFieldEnum.CTIME.getName(), now);
                Object f0 = record.get(esFieldMapper.get(EsFieldEnum.FD0.getName()));
                if (Objects.nonNull(f0)) {
                    if(f0 instanceof String) {
                        indexMap.put(EsFieldEnum.FD0.getName(), (String)f0);
                    } else if (f0 instanceof Date) {
                        indexMap.put(EsFieldEnum.FD0.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)f0).toInstant(), ZoneId.of("+8")),DB_DATA_TIME_FORMAT));
                    }
                }
                Object f1 = record.get(esFieldMapper.get(EsFieldEnum.FD1.getName()));
                if (Objects.nonNull(f1)) {
                    if(f1 instanceof String) {
                        indexMap.put(EsFieldEnum.FD1.getName(), (String)f1);
                    } else if (f1 instanceof Date) {
                        indexMap.put(EsFieldEnum.FD1.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)f1).toInstant(), ZoneId.of("+8")),DB_DATA_TIME_FORMAT));
                    }
                }
                Object f2 = record.get(esFieldMapper.get(EsFieldEnum.FD2.getName()));
                if (Objects.nonNull(f2)) {
                    if(f2 instanceof String) {
                        indexMap.put(EsFieldEnum.FD2.getName(), (String)f2);
                    } else if (f2 instanceof Date) {
                        indexMap.put(EsFieldEnum.FD2.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)f2).toInstant(), ZoneId.of("+8")),DB_DATA_TIME_FORMAT));
                    }
                }
                Object f3 = record.get(esFieldMapper.get(EsFieldEnum.FD3.getName()));
                if (Objects.nonNull(f3)) {
                    if(f3 instanceof String) {
                        indexMap.put(EsFieldEnum.FD3.getName(), (String)f3);
                    } else if (f3 instanceof Date) {
                        indexMap.put(EsFieldEnum.FD3.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)f3).toInstant(), ZoneId.of("+8")),DB_DATA_TIME_FORMAT));
                    }
                }
                Object f4 = record.get(esFieldMapper.get(EsFieldEnum.FD4.getName()));
                if (Objects.nonNull(f4)) {
                    if(f4 instanceof String) {
                        indexMap.put(EsFieldEnum.FD4.getName(), (String)f4);
                    } else if (f4 instanceof Date) {
                        indexMap.put(EsFieldEnum.FD4.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)f4).toInstant(), ZoneId.of("+8")),DB_DATA_TIME_FORMAT));
                    }
                }
                Object f5 = record.get(esFieldMapper.get(EsFieldEnum.FD5.getName()));
                if (Objects.nonNull(f5)) {
                    if(f5 instanceof String) {
                        indexMap.put(EsFieldEnum.FD5.getName(), (String)f5);
                    } else if (f5 instanceof Date) {
                        indexMap.put(EsFieldEnum.FD5.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)f5).toInstant(), ZoneId.of("+8")),DB_DATA_TIME_FORMAT));
                    }
                }
                Object f6 = record.get(esFieldMapper.get(EsFieldEnum.FD6.getName()));
                if (Objects.nonNull(f6)) {
                    if(f6 instanceof String) {
                        indexMap.put(EsFieldEnum.FD6.getName(), (String)f6);
                    } else if (f6 instanceof Date) {
                        indexMap.put(EsFieldEnum.FD6.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)f6).toInstant(), ZoneId.of("+8")),DB_DATA_TIME_FORMAT));
                    }
                }
                Object f7 = record.get(esFieldMapper.get(EsFieldEnum.FD7.getName()));
                if (Objects.nonNull(f7)) {
                    if(f7 instanceof String) {
                        indexMap.put(EsFieldEnum.FD7.getName(), (String)f7);
                    } else if (f7 instanceof Date) {
                        indexMap.put(EsFieldEnum.FD7.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)f7).toInstant(), ZoneId.of("+8")),DB_DATA_TIME_FORMAT));
                    }
                }
                Object f8 = record.get(esFieldMapper.get(EsFieldEnum.FD8.getName()));
                if (Objects.nonNull(f8)) {
                    if(f8 instanceof String) {
                        indexMap.put(EsFieldEnum.FD8.getName(), (String)f8);
                    } else if (f8 instanceof Date) {
                        indexMap.put(EsFieldEnum.FD8.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)f8).toInstant(), ZoneId.of("+8")),DB_DATA_TIME_FORMAT));
                    }
                }
                Object f9 = record.get(esFieldMapper.get(EsFieldEnum.FD9.getName()));
                if (Objects.nonNull(f9)) {
                    if(f9 instanceof String) {
                        indexMap.put(EsFieldEnum.FD9.getName(), (String)f9);
                    } else if (f9 instanceof Date) {
                        indexMap.put(EsFieldEnum.FD9.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)f9).toInstant(), ZoneId.of("+9")),DB_DATA_TIME_FORMAT));
                    }
                }

                Object t0 = record.get(esFieldMapper.get(EsFieldEnum.FT0.getName()));
                if (Objects.nonNull(t0)) {
                    if(t0 instanceof String) {
                        indexMap.put(EsFieldEnum.FT0.getName(), DateUtil.formatDateTime(DateUtil.parseDateTime((String)t0, DB_DATA_TIME_FORMAT),ES_DATA_TIME_FORMAT));
                    } else if (t0 instanceof Date) {
                        indexMap.put(EsFieldEnum.FT0.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)t0).toInstant(), ZoneId.of("+9")), ES_DATA_TIME_FORMAT));
                    }
                }
                Object t1 = record.get(esFieldMapper.get(EsFieldEnum.FT1.getName()));
                if (Objects.nonNull(t1)) {
                    if(t1 instanceof String) {
                        indexMap.put(EsFieldEnum.FT1.getName(), DateUtil.formatDateTime(DateUtil.parseDateTime((String)t1, DB_DATA_TIME_FORMAT),ES_DATA_TIME_FORMAT));
                    } else if (t1 instanceof Date) {
                        indexMap.put(EsFieldEnum.FT1.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)t1).toInstant(), ZoneId.of("+9")), ES_DATA_TIME_FORMAT));
                    }
                }
                Object t2 = record.get(esFieldMapper.get(EsFieldEnum.FT2.getName()));
                if (Objects.nonNull(t2)) {
                    if(t2 instanceof String) {
                        indexMap.put(EsFieldEnum.FT2.getName(), DateUtil.formatDateTime(DateUtil.parseDateTime((String)t2, DB_DATA_TIME_FORMAT),ES_DATA_TIME_FORMAT));
                    } else if (t2 instanceof Date) {
                        indexMap.put(EsFieldEnum.FT2.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)t2).toInstant(), ZoneId.of("+9")), ES_DATA_TIME_FORMAT));
                    }
                }
                Object t3 = record.get(esFieldMapper.get(EsFieldEnum.FT3.getName()));
                if (Objects.nonNull(t3)) {
                    if(t3 instanceof String) {
                        indexMap.put(EsFieldEnum.FT3.getName(), DateUtil.formatDateTime(DateUtil.parseDateTime((String)t3, DB_DATA_TIME_FORMAT),ES_DATA_TIME_FORMAT));
                    } else if (t3 instanceof Date) {
                        indexMap.put(EsFieldEnum.FT3.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)t3).toInstant(), ZoneId.of("+9")), ES_DATA_TIME_FORMAT));
                    }
                }
                Object t4 = record.get(esFieldMapper.get(EsFieldEnum.FT4.getName()));
                if (Objects.nonNull(t4)) {
                    if(t4 instanceof String) {
                        indexMap.put(EsFieldEnum.FT4.getName(), DateUtil.formatDateTime(DateUtil.parseDateTime((String)t4, DB_DATA_TIME_FORMAT),ES_DATA_TIME_FORMAT));
                    } else if (t4 instanceof Date) {
                        indexMap.put(EsFieldEnum.FT4.getName(), DateUtil.formatDateTime(LocalDateTime.ofInstant(((Date)t4).toInstant(), ZoneId.of("+9")), ES_DATA_TIME_FORMAT));
                    }
                }
                mapList.add(indexMap);
            }
        });
        return mapList;
    }
}

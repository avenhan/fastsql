package com.havetree.fastsql.server.service;

import com.zto.titans.common.entity.Result;
import com.zto.titans.common.util.JsonUtil;
import com.zto.titans.common.util.StringUtil;
import com.zto.ts.esbuff.api.dto.AdminDto;
import com.zto.ts.esbuff.api.dto.SynConfigDto;
import com.havetree.fastsql.dao.mapper.one.EsBuffSynConfigMapper;
import com.havetree.fastsql.dao.pojo.po.EsBuffSynConfigPo;
import com.havetree.fastsql.plugin.sql.RawSqlService;
import com.havetree.fastsql.server.entity.DbTypeEnum;
import com.havetree.fastsql.server.entity.EsFieldEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 作者：郑余
 * 描述：同步配置服务
 * 时间：2020/12/9 10:15
 */
@Slf4j
@Service
public class SynConfigService {

    @Resource
    EsBuffSynConfigMapper synConfigMapper;
    @Resource
    RawSqlService rawSqlService;
    //查询所有表同步配置
    public List<EsBuffSynConfigPo> getAllSynConfig() {
        return synConfigMapper.listAllSynConfig();
    }

    // 查询单表配置信息
    EsBuffSynConfigPo getSynConfig(EsBuffSynConfigPo record) {
        return synConfigMapper.getSynConfig(record);
    }

    // admin
    public int adminUpdate(AdminDto dto) {
        EsBuffSynConfigPo query = new  EsBuffSynConfigPo();
        query.setDbType((byte)dto.getDbType());
        query.setDbSource(dto.getDbSource());
        query.setDbName(dto.getDbName());
        query.setTbName(dto.getTbName());
        EsBuffSynConfigPo po = getSynConfig(query);
        if (Objects.nonNull(po) &&dto.getSynBatchTime()>0) {
            po.setBatchTime(dto.getSynBatchTime());
            synConfigMapper.updateSynBatchTime(po);
        }
        if (Objects.nonNull(po) &&dto.getIdDel()>0) {
            synConfigMapper.deleteSynConfigById(po.getId());
        }
        return 1;
    }

    // 设置同步状态
    public int setSynStatus(int oldStatus, EsBuffSynConfigPo record) {
        return synConfigMapper.updateSynStatus(oldStatus, record);
    }

    //更新重建同步时间
    int updateReSynTime(EsBuffSynConfigPo record) {
        return synConfigMapper.updateReSynTime(record);
    }

    // 更新定时同步时间和状态
    int updateSynTimeAndStatus(EsBuffSynConfigPo record) {
        return synConfigMapper.updateSynTimeAndStatus(record);
    }

    // 保存同步配置
    public Result<EsBuffSynConfigPo> buildSynConfig(SynConfigDto synConfigDto) {
        // saveOrUpdate synConfig
        Map<String, Set<String>> dbSource = rawSqlService.listDatabases();
        // validate dbSource dbName
//        if (dbSource.containsKey(synConfigDto.getDbSource())) {
//            if (dbSource.get(synConfigDto.getDbSource()).contains(synConfigDto.getDbName())) {
//                // TODO
//            }
//        }

        EsBuffSynConfigPo query = new EsBuffSynConfigPo();
        query.setDbType((byte)synConfigDto.getDbType());
        query.setDbSource(synConfigDto.getDbSource());
        query.setTbName(synConfigDto.getTbName());
        query.setDbName(synConfigDto.getDbName());
        query.setFieldMapper(synConfigDto.getFieldMapperJson());
        if (!validateRequiredFieldMapper(query)) {
            return Result.error("SYS0001","did dTime isDel should not empty");
        }
        EsBuffSynConfigPo existConfig =  this.getSynConfig(query);
        if (Objects.nonNull(existConfig)) {
            if (existConfig.getFieldMapper().equals(synConfigDto.getFieldMapperJson())) {
                return Result.error("SYS0001", "fileMapperInfo no change");
            }
            existConfig.setFieldMapper(synConfigDto.getFieldMapperJson());
            if(synConfigMapper.updateSynConfig(existConfig)>0) {
                return Result.success(existConfig);
            }
        } else {
            // new config
            EsBuffSynConfigPo insert = new EsBuffSynConfigPo();
            insert.setDbType((byte)synConfigDto.getDbType());
            insert.setDbSource(synConfigDto.getDbSource());
            insert.setTbName(synConfigDto.getTbName());
            insert.setDbName(synConfigDto.getDbName());
            insert.setFieldMapper(synConfigDto.getFieldMapperJson());
            insert.setBatchTime(5);// 默认5min
            synConfigMapper.insertSelective(insert);
            return Result.success(insert);
        }
        return Result.success();
    }

    // 构建同步数据sql
    public String builderSynDataSql(EsBuffSynConfigPo configPo) {
        Map<String,String> fields = getFieldMap(configPo);
        if (fields.size()==0) {
            return null;
        }
        String selectFields = StringUtil.join(",", fields.keySet());
        if (StringUtil.isEmpty(selectFields)) {
            return null;
        }
        Map<String, String> esFieldMap = getEsFieldMap(fields);
        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append("select ").append(selectFields).append(" from ");
        String dataTimeField = esFieldMap.get(EsFieldEnum.DTIME.getName());
        if (DbTypeEnum.MYSQL.getType()==configPo.getDbType()) {
            sqlSb.append(configPo.getDbName()).append(".").append(configPo.getTbName()).append(" where ")
                    .append(dataTimeField).append(">=? and ").append(dataTimeField).append("<? order by ").append(dataTimeField).append(" limit ?,?");
        } else if (DbTypeEnum.ORACLE.getType()==configPo.getDbType()) {
            String dbName = configPo.getDbName();
            dbName = dbName.split("\\.")[1];
            sqlSb.append(dbName).append(".").append(configPo.getTbName()).append(" where ")
                .append(dataTimeField).append(">= TO_DATE(?,'YYYY-MM-DD HH24:MI:SS') and ")
                    .append(dataTimeField).append("< TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS') and  ROWNUM >= ? and ROWNUM <? order by ").append(dataTimeField);
        }
        return sqlSb.toString();
    }

    // 数据库字段->es字段映射
    public Map<String, String> getFieldMap(EsBuffSynConfigPo configPo) {
        Map<String, String> fieldMap = new HashMap<>();
        try {
            fieldMap =  JsonUtil.parse(configPo.getFieldMapper(), Map.class);
        } catch (Exception e) {
            log.warn("fieldMapperJson parse fail:{}", JsonUtil.toJSON(configPo));
        }
        return fieldMap;
    }

    // es字段映射->数据库字段
    public Map<String, String> getEsFieldMap(Map<String, String> fieldMap) {
        Map<String, String> esFieldMap = new HashMap<>();
        fieldMap.forEach((k,v)->esFieldMap.put(v,k));
        return esFieldMap;
    }

    // 必须映射字段校验
    private boolean validateRequiredFieldMapper(EsBuffSynConfigPo configPo) {
        Map<String, String> fieldMap = this.getFieldMap(configPo);
        Map<String, String> esFieldMap = this.getEsFieldMap(fieldMap);
        if (!esFieldMap.containsKey(EsFieldEnum.DID.getName())) {
            return false;
        }
        if (!esFieldMap.containsKey(EsFieldEnum.DTIME.getName())) {
            return false;
        }
        if (!esFieldMap.containsKey(EsFieldEnum.ISDEL.getName())) {
            return false;
        }
        return true;
    }
}

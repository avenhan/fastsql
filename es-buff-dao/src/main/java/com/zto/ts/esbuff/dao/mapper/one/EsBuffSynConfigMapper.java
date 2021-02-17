package com.zto.ts.esbuff.dao.mapper.one;

import com.zto.ts.esbuff.dao.pojo.po.EsBuffSynConfigPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EsBuffSynConfigMapper {
    int insert(EsBuffSynConfigPo record);
    int insertSelective(EsBuffSynConfigPo record);
    EsBuffSynConfigPo getSynConfig(EsBuffSynConfigPo record);
    List<EsBuffSynConfigPo> listAllSynConfig();
    int updateSynStatus(@Param("oldStatus") int oldStatus, @Param("record") EsBuffSynConfigPo record);
    int updateReSynTime(EsBuffSynConfigPo record);
    int updateSynTimeAndStatus(EsBuffSynConfigPo record);
    int updateSynConfig(EsBuffSynConfigPo record);
    int updateSynBatchTime(EsBuffSynConfigPo record);
    int deleteSynConfigById(@Param("id") int id);
}
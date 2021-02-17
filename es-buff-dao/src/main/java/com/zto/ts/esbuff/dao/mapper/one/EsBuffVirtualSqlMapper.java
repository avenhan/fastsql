package com.zto.ts.esbuff.dao.mapper.one;

import com.zto.ts.esbuff.dao.pojo.po.EsBuffSynConfigPo;
import com.zto.ts.esbuff.dao.pojo.po.EsBuffVirtualSqlPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EsBuffVirtualSqlMapper {
    int insert(EsBuffVirtualSqlPo record);
    EsBuffVirtualSqlPo get(@Param("id") Long id);
    List<EsBuffVirtualSqlPo> list(EsBuffVirtualSqlPo record);
    int update(EsBuffVirtualSqlPo record);
}
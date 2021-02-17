package com.zto.ts.esbuff.dao.mapper.one;

import com.zto.ts.esbuff.dao.pojo.po.EsBuffDb;
import com.zto.ts.esbuff.dao.pojo.po.EsBuffVirtualSqlPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EsBuffDbMapper {
    int insert(EsBuffDb record);
    EsBuffDb get(@Param("id") Long id);
    List<EsBuffDb> list(EsBuffDb record);
    int update(EsBuffDb record);
}
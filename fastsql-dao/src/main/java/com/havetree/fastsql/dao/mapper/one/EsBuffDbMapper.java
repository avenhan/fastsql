package com.havetree.fastsql.dao.mapper.one;

import com.havetree.fastsql.dao.pojo.po.EsBuffDb;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EsBuffDbMapper {
    int insert(EsBuffDb record);
    EsBuffDb get(@Param("id") Long id);
    List<EsBuffDb> list(EsBuffDb record);
    int update(EsBuffDb record);
}
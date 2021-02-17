package com.havetree.fastsql.dao.mapper.one;

import com.havetree.fastsql.dao.pojo.po.EsBuffVirtualSqlPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EsBuffVirtualSqlMapper {
    int insert(EsBuffVirtualSqlPo record);
    EsBuffVirtualSqlPo get(@Param("id") Long id);
    List<EsBuffVirtualSqlPo> list(EsBuffVirtualSqlPo record);
    int update(EsBuffVirtualSqlPo record);
}
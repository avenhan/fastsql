package com.havetree.fastsql.plugin.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface BuffConfigMapper
{
    @SuppressWarnings("rawtypes")
    public List<Map> list(Object ... params);
}

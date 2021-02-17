package com.zto.ts.esbuff.api;

import com.github.pagehelper.PageInfo;
import com.zto.titans.common.entity.Result;
import com.zto.ts.esbuff.api.dto.SearchConditionDto;
import com.zto.ts.esbuff.api.dto.ValueCondition;
import com.zto.ts.esbuff.api.dto.VirtualRealFields;
import com.zto.ts.esbuff.api.dto.VirtualSqlDto;

import java.util.List;
import java.util.Map;

public interface IVirtualSqlApi {

    /**
     * 作者：韓建建
     * 描述：添加搜索數據來源
     * @param
     * @return
     */
    public boolean addSql(VirtualSqlDto rqst);

    /**
     * 作者：韓建建
     * 描述：更新所有數據的來源
     * @param
     * @return
     */
    public boolean updateSql(List<VirtualSqlDto> sqls);

    /**
     * 作者：韓建建
     * 描述：搜索數據
     * @param
     * @return
     */
    List<Map<String, Object>> search(List<VirtualRealFields> lstRealFields, String virtualName, List<ValueCondition> condition, Integer page, Integer pageSize, String sortField, String sort);


    /**
     * 作者：韓建建
     * 描述：获取数据数量
     * @param
     * @return
     */
    long count(String virtualName, List<ValueCondition> condition);
}

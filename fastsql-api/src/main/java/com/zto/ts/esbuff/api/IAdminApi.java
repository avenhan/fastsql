package com.zto.ts.esbuff.api;

import com.zto.ts.esbuff.api.dto.AdminDto;
import com.zto.ts.esbuff.api.dto.SynConfigDto;

/**
 * 作者：郑余
 * 描述：管理接口
 * 时间：2020/12/10 18:06
 */
public interface IAdminApi {

    // 终止重建表索引
    int stopReindex(AdminDto dto);
    // 更新表同步配置
    int updateSynConfig(AdminDto dto);
}

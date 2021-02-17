package com.zto.ts.esbuff.server.facade;

import com.alibaba.dubbo.config.annotation.Service;
import com.zto.ts.esbuff.api.IAdminApi;
import com.zto.ts.esbuff.api.dto.AdminDto;
import com.zto.ts.esbuff.server.service.SynConfigService;
import com.zto.ts.esbuff.server.service.SynDataService;

import javax.annotation.Resource;

/**
 * 作者：郑余
 * 描述：管理操作接口
 * 时间：2020/12/10 18:11
 */
@Service
public class AdminApi implements IAdminApi {

    @Resource
    SynConfigService configService;
    @Resource
    SynDataService dataService;

    @Override
    public int stopReindex(AdminDto dto) {
        if (validatePass(dto.getPass())) {
            String key = dto.getDbSource()+dto.getDbName()+dto.getTbName();
            if (dataService.getTbReindexFlagMap().containsKey(key)) {
                dataService.getTbReindexFlagMap().put(key, false);
            }
        }
        return 0;
    }

    @Override
    public int updateSynConfig(AdminDto dto) {
        if (validatePass(dto.getPass())) {
            return configService.adminUpdate(dto);
        }
        return 0;
    }

    // 默认口令校验
    private boolean validatePass(String pass) {
        return "131517".equals(pass)?true:false;
    }
}

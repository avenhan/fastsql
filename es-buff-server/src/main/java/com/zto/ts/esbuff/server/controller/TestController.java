package com.zto.ts.esbuff.server.controller;

import java.text.SimpleDateFormat;
import java.util.*;

import com.zto.titans.common.util.StringUtil;
import com.zto.ts.esbuff.api.IVirtualSqlApi;
import com.zto.ts.esbuff.api.Service.EsBuff;
import com.zto.ts.esbuff.api.dto.ValueCondition;
import com.zto.ts.esbuff.api.dto.VirtualRealFields;
import com.zto.ts.esbuff.api.dto.VirtualSqlDto;
import com.zto.ts.esbuff.api.en.AnchorType;
import com.zto.ts.esbuff.api.en.TypeEnum;
import com.zto.ts.esbuff.dao.mapper.one.EsBuffVirtualSqlMapper;
import com.zto.ts.esbuff.dao.pojo.po.EsBuffVirtualSqlPo;
import com.zto.ts.esbuff.plugin.sql.RawSqlService;
import com.zto.ts.esbuff.server.excel.ExcelSheetUtil;
import com.zto.ts.esbuff.server.excel.TableRow;
import com.zto.ts.esbuff.server.virtual.EsBuffHelper;
import com.zto.ts.esbuff.server.virtual.VirtualSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController
{
    private final static String ES_DATA_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private final static String DB_DATA_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private VirtualSyncService syncService;

    @Autowired
    private EsBuffHelper esBuff;

    @Autowired
    private EsBuffVirtualSqlMapper mapper;

    @Autowired
    private RawSqlService rawSqlService;

    @Autowired
    private TableRow tableRow;

    @RequestMapping(path = "/test", method = { RequestMethod.GET })
    public Object test2(String name)
    {
        if (name.equals("update"))
        {
            String datasource = "10.9.20.251:3306/panda_user_center";
            String sql = "update user_info set email=? where id=?";
            rawSqlService.update(datasource, sql, "han@test.com", 5);
        }
        else if (name.equals("excel"))
        {
            List<ExcelSheetUtil> lst = ExcelSheetUtil.listSheet("D:\\java\\panda\\项目进展\\raw.xlsx");
            for (ExcelSheetUtil sheet : lst)
            {
                sheet.doSheetRows(tableRow);
            }
            return "success";
        }
        else if (name.equals("add"))
        {
            return addSql(name);
        }
        else if (name.equals("sync"))
        {
            sync();
        }
        else if (name.startsWith("resync"))
        {
            return resync(name);
        }
        else {
            return query(name);
        }

        return name;
    }

    private Object addSql(String name)
    {
        String datasource = "10.9.36.64:3306/maintain";
        if (name.indexOf(':') > 0)
        {
            datasource = name.substring(name.indexOf(':') + 1);
        }

        String sql ="select a.id as a_id, b.id as b_id, '内修' as type, a.car_service_order_code, b.maintain_work_order_code, \n" +
                "a.truck_number, a.car_brand, a.apply_team, a.truck_team,a.start_time, a.end_time, a.maintain_plant, \n" +
                "'' as project_name, null as work_hour_amount, b.material_name, b.material_count, a.remark, a.create_user, a.create_time,\n" +
                "case when a.delete_flag=1 then 1 when b.delete_flag=1 then 1 else 0 end as is_deleted\n" +
                "from zto_maintain_work_order a inner join zto_maintain_work_order_material b on a.id=b.work_order_id\n";


        Map<String, AnchorType> mapAnchor = new HashMap<>();
        mapAnchor.put("a_id", AnchorType.UNIQUE_KEY);
        mapAnchor.put("b_id", AnchorType.UNIQUE_KEY);
        mapAnchor.put("a.update_time", AnchorType.UPDATE_TIME);
        mapAnchor.put("b.update_time", AnchorType.UPDATE_TIME);
        mapAnchor.put("a.create_time", AnchorType.UPDATE_TIME);
        mapAnchor.put("b.create_time", AnchorType.UPDATE_TIME);

        int success = 0;
        if (esBuff.addSql(TestTable.class, datasource, sql, mapAnchor))
        {
            success++;
        }


        String sql2="select a.id as a_id, b.id as b_id, '内修' as type, a.car_service_order_code, b.maintain_work_order_code, \n" +
                "a.truck_number, a.car_brand, a.apply_team, a.truck_team,a.start_time, a.end_time,a.maintain_plant, \n" +
                "b.project_name, b.work_hour_amount, '' as material_name,  null as material_count, a.remark, a.create_user, a.create_time,\n" +
                "case when a.delete_flag=1 then 1 when b.delete_flag=1 then 1 else 0 end as is_deleted\n" +
                "from zto_maintain_work_order a inner join zto_maintain_work_order_work b on a.id=b.work_order_id\n";

        if (esBuff.addSql(TestTable.class, datasource, sql2, mapAnchor))
        {
            success++;
        }

        String sql3 = "select a.id as a_id, b.id as b_id, '外修' as type, a.work_order_number as car_service_order_code, a.order_number as maintain_work_order_code, \n" +
                "a.truck_number, a.brand as car_brand, a.repair_truck_team as apply_team, a.truck_team,a.repair_start_time as start_time, a.repair_end_time as end_time,a.maintain_shop as maintain_plant, \n" +
                "'' as project_name, null as work_hour_amount, b.name as material_name,  b.quantity as material_count, a.out_repair_remark as remark, a.apply_user_name as create_user, a.create_time,\n" +
                "case when a.delete_flag=1 then 1 when b.delete_flag=1 then 1 else 0 end as is_deleted\n" +
                "from zto_outer_maintain_order a inner join zto_outer_maintain_parts_list b on a.id=b.order_id\n";

        if (esBuff.addSql(TestTable.class, datasource, sql3, mapAnchor))
        {
            success++;
        }

        String sql4="select a.id as a_id, b.id as b_id, '外修' as type, a.work_order_number as car_service_order_code, a.order_number as maintain_work_order_code, \n" +
                "a.truck_number, a.brand as car_brand, a.repair_truck_team as apply_team, a.truck_team,a.repair_start_time as start_time, a.repair_end_time as end_time,a.maintain_shop as maintain_plant, \n" +
                "b.name as project_name, b.labor as work_hour_amount, '' as material_name,  null as material_count, a.out_repair_remark as remark, a.apply_user_name as create_user, a.create_time,\n" +
                "case when a.delete_flag=1 then 1 when b.delete_flag=1 then 1 else 0 end as is_deleted\n" +
                "from zto_outer_maintain_order a inner join zto_outer_maintain_labor_list b on a.id=b.order_id\n";

        if (esBuff.addSql(TestTable.class, datasource, sql4, mapAnchor))
        {
            success++;
        }

        return "add to datasour: " + datasource + " success count: " + success;
    }

    private Object query(String name)
    {
        if (StringUtil.isEmpty(name))
        {
            name = "A6M019";
        }
        List<ValueCondition> lstCondtion = new ArrayList<>();

        ValueCondition condition = new ValueCondition();
        condition.setFieldName("truck_number");
        condition.setFiledValue(name);
        condition.setMatchOption((byte)0);

        lstCondtion.add(condition);

        List<TestTable> items = esBuff.search(TestTable.class, lstCondtion, 1, 10);
        long count = esBuff.count(TestTable.class, lstCondtion);

        Map<String, Object> mapRet = new HashMap<>();
        mapRet.put("count", count);
        mapRet.put("items", items);

        return mapRet;
    }

    private void sync()
    {
        syncService.doSync();
    }

    private Object resync(String name)
    {
        String [] syncInfo = name.split(":");
        if (syncInfo.length < 2)
        {
            return "must be: rescyn:id:sync_time";
        }

        Long id = null;
        try
        {
            id = Long.parseLong(syncInfo[1]);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return "must be: rescyn:id:sync_time...";
        }

        Date syncTime = null;
        if (syncInfo.length == 3)
        {
            String syncTimeInfo = syncInfo[2];

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DB_DATA_TIME_FORMAT);
            try
            {
                syncTime = simpleDateFormat.parse(syncTimeInfo);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                return "must be: rescyn:id:sync_time...with yyyy-MM-dd HH:mm:ss";
            }
        }

        EsBuffVirtualSqlPo sqlPo = new EsBuffVirtualSqlPo();
        sqlPo.setId(id);
        sqlPo.setStatus(1);
        sqlPo.setSyncTime(syncTime);

        mapper.update(sqlPo);
        return "success";
    }
}

package com.zto.ts.test.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zto.titans.common.util.StringUtil;
import com.zto.ts.esbuff.api.IVirtualSqlApi;
import com.zto.ts.esbuff.api.Service.EsBuff;
import com.zto.ts.esbuff.api.dto.ValueCondition;
import com.zto.ts.esbuff.api.dto.VirtualSqlDto;
import com.zto.ts.esbuff.api.en.AnchorType;
import com.zto.ts.esbuff.api.en.TypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController
{
    private static Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private EsBuff esBuff;

    @RequestMapping(path = "/test", method = { RequestMethod.GET })
    public Object test2(String name)
    {
        if (name.equals("add"))
        {
            addSql();
        }
        else
        {
            query(name);
        }


        return name;
    }

    private void addSql()
    {
        String sql ="select a.id as a_id, b.id as b_id, '内修' as type, a.car_service_order_code, b.maintain_work_order_code, \n" +
                "a.truck_number, a.car_brand, a.apply_team, a.truck_team,a.start_time, a.end_time, a.maintain_plant, \n" +
                "'' as project_name, null as work_hour_amount, b.material_name, b.material_count, a.remark, a.create_user, a.create_time\n" +
                "from zto_maintain_work_order a inner join zto_maintain_work_order_material b on a.id=b.work_order_id\n";


        Map<String, AnchorType> mapAnchor = new HashMap<>();
        mapAnchor.put("a_id", AnchorType.UNIQUE_KEY);
        mapAnchor.put("b_id", AnchorType.UNIQUE_KEY);
        mapAnchor.put("a.update_time", AnchorType.UPDATE_TIME);
        mapAnchor.put("b.update_time", AnchorType.UPDATE_TIME);
        mapAnchor.put("a.create_time", AnchorType.UPDATE_TIME);
        mapAnchor.put("b.create_time", AnchorType.UPDATE_TIME);

        esBuff.addSql(TestTable.class, "10.9.36.64:3306/maintain", sql, mapAnchor);


        String sql2="select a.id as a_id, b.id as b_id, '内修' as type, a.car_service_order_code, b.maintain_work_order_code, \n" +
                "a.truck_number, a.car_brand, a.apply_team, a.truck_team,a.start_time, a.end_time,a.maintain_plant, \n" +
                "b.project_name, b.work_hour_amount, '' as material_name,  null as material_count, a.remark, a.create_user, a.create_time\n" +
                "from zto_maintain_work_order a inner join zto_maintain_work_order_work b on a.id=b.work_order_id\n";

        esBuff.addSql(TestTable.class, "10.9.36.64:3306/maintain", sql2, mapAnchor);

        String sql3 = "select a.id as a_id, b.id as b_id, '外修' as type, a.work_order_number as car_service_order_code, a.order_number as maintain_work_order_code, \n" +
                "a.truck_number, a.brand as car_brand, a.repair_truck_team as apply_team, a.truck_team,a.repair_start_time as start_time, a.repair_end_time as end_time,a.maintain_shop as maintain_plant, \n" +
                "'' as project_name, null as work_hour_amount, b.name as material_name,  b.quantity as material_count, a.out_repair_remark as remark, a.apply_user_name as create_user, a.create_time\n" +
                "from zto_outer_maintain_order a inner join zto_outer_maintain_parts_list b on a.id=b.order_id\n";

        esBuff.addSql(TestTable.class, "10.9.36.64:3306/maintain", sql3, mapAnchor);

        String sql4="select a.id as a_id, b.id as b_id, '外修' as type, a.work_order_number as car_service_order_code, a.order_number as maintain_work_order_code, \n" +
                "a.truck_number, a.brand as car_brand, a.repair_truck_team as apply_team, a.truck_team,a.repair_start_time as start_time, a.repair_end_time as end_time,a.maintain_shop as maintain_plant, \n" +
                "b.name as project_name, b.labor as work_hour_amount, '' as material_name,  null as material_count, a.out_repair_remark as remark, a.apply_user_name as create_user, a.create_time\n" +
                "from zto_outer_maintain_order a inner join zto_outer_maintain_labor_list b on a.id=b.order_id\n";

        esBuff.addSql(TestTable.class, "10.9.36.64:3306/maintain", sql4, mapAnchor);

    }

    private void query(String name)
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
        long count = items.size();
        long count2 = esBuff.count(TestTable.class, lstCondtion);

        if (count != count2)
        {
            System.out.println("count not equal");
        }
    }

    private void sync()
    {
        //syncService.doSync();
    }
}

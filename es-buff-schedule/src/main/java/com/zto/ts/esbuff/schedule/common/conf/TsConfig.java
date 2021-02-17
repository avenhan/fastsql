package com.zto.ts.esbuff.schedule.common.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 作者: 陈征
 * 描述: 用于读取apollo的配置
 * 日期: 2019/10/27 18:53
 */
@ConfigurationProperties(prefix = "ts")
@Data
public class TsConfig {
    //附近空车的作业类型
    private boolean traceAbnormal;
    //离线间隔时长(mins)
    private int offlineTimes;
    //离线间隔距离(km)
    private int offlineDistances;
    //星软相关配置
    private String  xrUid;
    private String  xrCid;
    private String  xrKey;
    private String  xrFuelUrl;
    private int xrHour;
    private long xrDisStartTime;
    //百度相关配置
    private String baiduAk;

    //中转部id集合
    private List<String> transferIds;

    // 判断晚卸最近范围(分钟)
    private Integer truckIntervalMinute;

    // 获取未签收运单的范围(小时)
    private Integer unSignBillInterval;

    // 支线处罚逾期天数
    private Integer byWayLateDay;

    //支线处罚返款比例
    private Double refundRatio;


    //星软-新增设备报修url
    private String xrDeviceRepairAddUrl;
    //星软-查看设备报修url
    private String xrDeviceRepairDetailUrl ;
    //星软秘钥
    private String xrAPIKey;
    //星软提供
    private String xrXRUID;
    //星软提供
    private String xrXRCID;

    //短信账号id
    private String smsAccountCode;
    //短信账号key
    private String smsAccountKey;
    //短信场景code-晚点罚款短信推送司机
    private String sceneCode_driver;
    //短信场景code-晚点罚款短信推送承运商经理
    private String sceneCode_carrierManager;
    //短信场景code-晚发罚款短信推送中心经理
    private String sceneCode_centerManager;
    //短信场景code-星联航空短信
    private String sceneCode_aviation;

    // 根据站点刷新车辆到车时间开关
    private boolean refreshTruckGpsTimeBySiteCode;
}

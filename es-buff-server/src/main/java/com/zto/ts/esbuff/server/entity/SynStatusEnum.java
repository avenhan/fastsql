package com.zto.ts.esbuff.server.entity;

/**
 * 作者：郑余
 * 描述：同步状态
 * 时间：2020/12/9 10:31
 */
public enum SynStatusEnum {

    IDLE(0,"空闲"),
    SYN(1,"同步中"),
    RE_SYN(2,"重建中");
    private int statusValue;
    private String statusName;
    SynStatusEnum(int statusValue, String statusName) {
        this.statusValue = statusValue;
        this.statusName = statusName;
    }

    public int getValue() {
        return this.statusValue;
    }
}

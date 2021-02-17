package com.zto.ts.esbuff.server.entity;

/**
 * 作者：郑余
 * 描述：数据库类型
 * 时间：2020/12/9 15:17
 */
public enum  DbTypeEnum {

    MYSQL(0,"mysql"),ORACLE(1,"oracle");
    private int type;
    private String name;
    DbTypeEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }
    public int getType() {
        return this.type;
    }
}

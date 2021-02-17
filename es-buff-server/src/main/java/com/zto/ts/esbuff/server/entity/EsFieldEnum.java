package com.zto.ts.esbuff.server.entity;

/**
 * 作者：郑余
 * 描述：es映射字段范围
 * 时间：2020/12/7 14:19
 */
public enum EsFieldEnum {
    ID(-1,"id"),CTIME(13,"cTime"),
    FD0(0,"d0"),FD1(1,"d1"),FD2(2,"d2"),FD3(3,"d3"),FD4(4,"d4"),
    FD5(5,"d5"),FD6(6,"d6"),FD7(7,"d7"),FD8(8,"d8"),FD9(9,"d9"),
    DID(10,"did"),DTIME(11,"dTime"),ISDEL(12,"isDel"),
    FT0(20,"t0"),FT1(21,"t1"),FT2(22,"t2"),FT3(23,"t3"),FT4(4,"t4");

    private int index;
    private String name;
    EsFieldEnum(int index, String name) {
        this.index = index;
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public int getIndex(){
        return this.index;
    }
}

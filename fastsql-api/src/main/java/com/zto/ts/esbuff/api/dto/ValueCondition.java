package com.zto.ts.esbuff.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 作者：郑余
 * 描述：检索字段值
 * 时间：2020/12/7 14:54
 */
@Data
public class ValueCondition implements Serializable {
    private static final long serialVersionUID = 1186827759535057913L;
    private String fieldName;
    private String filedValue;// date type need yyyy-MM-dd HH:mm:ss
    private byte matchOption;// 0-like 1-equal 2-range
    private String from;
    private String to;

    public byte getMatchOption() {
        return matchOption;
    }

    public void setMatchOption(byte matchOption) {
        this.matchOption = matchOption;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFiledValue() {
        return filedValue;
    }

    public void setFiledValue(String filedValue) {
        this.filedValue = filedValue;
    }


}

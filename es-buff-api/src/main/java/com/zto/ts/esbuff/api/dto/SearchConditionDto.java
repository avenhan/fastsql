package com.zto.ts.esbuff.api.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 作者：郑余
 * 描述：检索条件
 * 时间：2020/11/29 18:05
 */
public class SearchConditionDto implements Serializable {
    private static final long serialVersionUID = -4733439295858819637L;
    private byte dbType;
    private String dbSource;
    private String dbName;
    private String tbName;
    private List<ValueCondition> conditions;
    private List<String> resultFields;
    private int pageNum;
    private int pageSize;

    public int getPageNum() {
        return pageNum;
    }
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public byte getDbType() {
        return dbType;
    }
    public void setDbType(byte dbType) {
        this.dbType = dbType;
    }
    public String getDbSource() {
        return dbSource;
    }
    public void setDbSource(String dbSource) {
        this.dbSource = dbSource;
    }
    public String getDbName() {
        return dbName;
    }
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
    public String getTbName() {
        return tbName;
    }

    public void setTbName(String tbName) {
        this.tbName = tbName;
    }

    public List<ValueCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<ValueCondition> conditions) {
        this.conditions = conditions;
    }

    public List<String> getResultFields() {
        return resultFields;
    }

    public void setResultFields(List<String> resultFields) {
        this.resultFields = resultFields;
    }
}

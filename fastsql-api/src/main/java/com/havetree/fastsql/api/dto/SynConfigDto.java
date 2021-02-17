package com.zto.ts.esbuff.api.dto;

import java.io.Serializable;

/**
 * 作者：郑余
 * 描述：配置入参
 * 时间：2020/12/9 11:01
 */
public class SynConfigDto implements Serializable {
    private static final long serialVersionUID = 2486751039195476626L;
    private int dbType;// 0-mysql 1-oracle
    private String dbSource;
    private String tbName;
    private String dbName;
    private String fieldMapperJson;

    public int getDbType() {
        return dbType;
    }

    public void setDbType(int dbType) {
        this.dbType = dbType;
    }

    public String getDbSource() {
        return dbSource;
    }

    public void setDbSource(String dbSource) {
        this.dbSource = dbSource;
    }

    public String getTbName() {
        return tbName;
    }

    public void setTbName(String tbName) {
        this.tbName = tbName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getFieldMapperJson() {
        return fieldMapperJson;
    }

    public void setFieldMapperJson(String fieldMapperJson) {
        this.fieldMapperJson = fieldMapperJson;
    }
}

package com.zto.ts.esbuff.api.dto;

import java.io.Serializable;

/**
 * 作者：郑余
 * 描述：管理操作入参
 * 时间：2020/12/10 18:18
 */
public class AdminDto implements Serializable {
    private static final long serialVersionUID = 2783559160592083688L;
    private String pass;//口令
    private int dbType;// 0-mysql 1-oracle
    private String dbSource;
    private String tbName;
    private String dbName;
    private String fieldMapperJson;
    private int synBatchTime;//同步批次时间
    private byte idDel;

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

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

    public int getSynBatchTime() {
        return synBatchTime;
    }

    public void setSynBatchTime(int synBatchTime) {
        this.synBatchTime = synBatchTime;
    }

    public byte getIdDel() {
        return idDel;
    }

    public void setIdDel(byte idDel) {
        this.idDel = idDel;
    }
}

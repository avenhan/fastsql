package com.havetree.fastsql.dao.pojo.po;

import java.util.Date;

public class EsBuffSynConfigPo {
    private Integer id;

    private Byte dbType;
    private String dbSource;
    private String dbName;

    private String tbName;

    private String fieldMapper;

    private Date synTime;

    private Date reSynTime;

    private Integer batchTime;

    private Byte synStatus;

    private Byte isDeleted;

    private Date createTime;

    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDbSource() {
        return dbSource;
    }

    public void setDbSource(String dbSource) {
        this.dbSource = dbSource;
    }

    public Byte getDbType() {
        return dbType;
    }

    public void setDbType(Byte dbType) {
        this.dbType = dbType;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName == null ? null : dbName.trim();
    }

    public String getTbName() {
        return tbName;
    }

    public void setTbName(String tbName) {
        this.tbName = tbName == null ? null : tbName.trim();
    }

    public String getFieldMapper() {
        return fieldMapper;
    }

    public void setFieldMapper(String fieldMapper) {
        this.fieldMapper = fieldMapper == null ? null : fieldMapper.trim();
    }

    public Date getSynTime() {
        return synTime;
    }

    public void setSynTime(Date synTime) {
        this.synTime = synTime;
    }

    public Date getReSynTime() {
        return reSynTime;
    }

    public void setReSynTime(Date reSynTime) {
        this.reSynTime = reSynTime;
    }

    public Integer getBatchTime() {
        return batchTime;
    }

    public void setBatchTime(Integer batchTime) {
        this.batchTime = batchTime;
    }

    public Byte getSynStatus() {
        return synStatus;
    }

    public void setSynStatus(Byte synStatus) {
        this.synStatus = synStatus;
    }

    public Byte getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Byte isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
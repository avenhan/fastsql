package com.zto.ts.esbuff.api.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 作者：郑余
 * 描述：同步配置
 * 时间：2020/12/14 10:10
 */
public class EsBuffSynConfigBo implements Serializable {
    private static final long serialVersionUID = 7300559126680550664L;
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

    public Byte getDbType() {
        return dbType;
    }

    public void setDbType(Byte dbType) {
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

    public String getFieldMapper() {
        return fieldMapper;
    }

    public void setFieldMapper(String fieldMapper) {
        this.fieldMapper = fieldMapper;
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

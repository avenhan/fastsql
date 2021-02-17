package com.havetree.fastsql.dao.pojo.po;

import java.util.Date;

public class EsBuffVirtualSqlPo
{
    // 虚拟表的ID
    private Long id;

    // '虚拟表名'
    private String virtualName;

    // 数据源类型：0-mysql，1-oracle
    private Integer dbType;

    // 数据源的ip端口
    private String dbSource;

    // 數據庫名稱
    private String dbName;

    // sql md5
    private String sqlMd5;

    // '执行的SQL'
    private String runSql;

    // 'SQL中的表字段名称'
    private String fields;

    // db fields
    private String dbFields;

    // 每个表的锚字段，包含：唯一键、更新时间字段
    private String anchor;

    //'添加用户的ID'
    private String userId;

    // '添加的用户名'
    private String userName;

    // 虚拟表sql的状态：0-create，1-es索引建立，2-同步中，3-同步完成
    private Integer status;

    // 是否已经同步过
    private Boolean isSynced;

    // '已同步完成的时间'
    private Date syncTime;

    //'创建时间'
    private Date createTime;

    //'更新时间'
    private Date updateTime;

    // '是否删除标识'
    private Boolean isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVirtualName() {
        return virtualName;
    }

    public void setVirtualName(String virtualName) {
        this.virtualName = virtualName;
    }

    public Integer getDbType() {
        return dbType;
    }

    public void setDbType(Integer dbType) {
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

    public String getSqlMd5() {
        return sqlMd5;
    }

    public void setSqlMd5(String sqlMd5) {
        this.sqlMd5 = sqlMd5;
    }

    public String getRunSql() {
        return runSql;
    }

    public void setRunSql(String runSql) {
        this.runSql = runSql;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getDbFields() {
        return dbFields;
    }

    public void setDbFields(String dbFields) {
        this.dbFields = dbFields;
    }

    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getSynced() {
        return isSynced;
    }

    public void setSynced(Boolean synced) {
        isSynced = synced;
    }

    public Date getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(Date syncTime) {
        this.syncTime = syncTime;
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

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}

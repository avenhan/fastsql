package com.zto.ts.esbuff.api.dto;

import com.zto.ts.esbuff.api.en.AnchorType;
import com.zto.ts.esbuff.api.en.TypeEnum;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class VirtualSqlDto implements Serializable {

    private String virtualName;

    // 数据源类型：0-mysql，1-oracle
    private Integer dbType;

    // 数据源的ip端口
    private String dbSource;

    // 數據庫名稱
    private String dbName;

    // '执行的SQL'
    private String sql;

    private Map<String, TypeEnum> fields;

    // 每个表的锚字段，包含：唯一键、更新时间字段
    private Map<String, AnchorType> anchors;

    //'添加用户的ID'
    private String userId;

    // '添加的用户名'
    private String userName;

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

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Map<String, TypeEnum> getFields() {
        return fields;
    }

    public void setFields(Map<String, TypeEnum> fields) {
        this.fields = fields;
    }

    public Map<String, AnchorType> getAnchors() {
        return anchors;
    }

    public void setAnchors(Map<String, AnchorType> anchors) {
        this.anchors = anchors;
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
}

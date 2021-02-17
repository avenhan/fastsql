package com.havetree.fastsql.plugin.sql;

public class DatabaseField {
    private String dbName;
    private String tableName;
    private String tableNick;
    private String fieldName;
    private String fieldNick;
    private String fieldType;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableNick() {
        return tableNick;
    }

    public void setTableNick(String tableNick) {
        this.tableNick = tableNick;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldNick() {
        return fieldNick;
    }

    public void setFieldNick(String fieldNick) {
        this.fieldNick = fieldNick;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
}

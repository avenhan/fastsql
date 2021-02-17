package com.zto.ts.esbuff.api.dto;

import java.io.Serializable;
import java.util.List;

public class VirtualRealFields implements Serializable
{
    private String table;
    private String id;
    private List<String> fields;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public void addField(String field)
    {
        this.fields.add(field);
    }
}

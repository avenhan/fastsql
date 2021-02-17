package com.zto.ts.esbuff.api.obj;

import com.zto.ts.esbuff.api.en.TypeEnum;

import java.lang.reflect.Field;

public class EsTypeField
{
    private Field field;
    private  String fieldName;
    private TypeEnum typeEnum;

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }
}

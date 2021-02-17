package com.zto.ts.esbuff.server.virtual;

import com.alibaba.fastjson.JSONObject;
import com.zto.ts.esbuff.api.en.AnchorType;
import com.zto.ts.esbuff.api.en.TypeEnum;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class VirtualSqlUtil
{
    public static Map<String, TypeEnum> getFieldTypeFromJson(String json)
    {
        Map<String, TypeEnum> mapRet = new TreeMap<>();

        JSONObject jMap = JSONObject.parseObject(json);
        if (!(jMap instanceof  Map))
        {
            throw new RuntimeException("json object must be map");
        }

        Map<String, Object> map = jMap;
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            String value = (String) entry.getValue();
            TypeEnum type = TypeEnum.valueOf(value);
            mapRet.put(entry.getKey(), type);
        }

        return mapRet;
    }

    public static Map<String, AnchorType> getAnchor(String json)
    {
        Map<String, AnchorType> mapRet = new TreeMap<>();

        JSONObject jMap = JSONObject.parseObject(json);
        if (!(jMap instanceof  Map))
        {
            throw new RuntimeException("json object must be map");
        }

        Map<String, Object> map = jMap;
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            String value = (String) entry.getValue();
            AnchorType type = AnchorType.valueOf(value);
            mapRet.put(entry.getKey(), type);
        }

        return mapRet;
    }

    public static String md5(String plainText)
    {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(plainText.getBytes());
        } catch (Throwable e)
        {
            throw new RuntimeException("没有这个md5算法！");
        }

        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }
}

package com.zto.ts.esbuff.server.excel;

import java.util.Map;

public interface IExcelRow
{
    public String onFirstOneColumn(String data);
    public Map<String, Integer> onMaybeColumnTitle(Map<String, Integer> map);
    public void onRow(String dbSource, Map<String, String> mapRow);
}

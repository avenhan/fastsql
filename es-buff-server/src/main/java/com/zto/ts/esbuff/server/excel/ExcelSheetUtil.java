package com.zto.ts.esbuff.server.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class ExcelSheetUtil
{
    private final static String excel2003L =".xls";    //2003- 版本的excel
    private final static String excel2007U =".xlsx";   //2007+ 版本的excel

    private Sheet sheet;

    public static void main(String [] args)
    {
        List<ExcelSheetUtil> lst = listSheet("D:\\java\\panda\\项目进展\\raw.xlsx");

        TableRow tableRow = new TableRow();

        for (ExcelSheetUtil sheet : lst)
        {
            sheet.doSheetRows(tableRow);
        }
    }

    public static List<ExcelSheetUtil> listSheet(String file)
    {
        List<ExcelSheetUtil> lstRet = new ArrayList<>();
        try
        {
            Workbook work = getWorkbook(new FileInputStream(new File(file)), file);
            Sheet sheet = null;
            for (int i = 0; i < work.getNumberOfSheets(); i++)
            {
                sheet = work.getSheetAt(i);
                if (sheet == null)
                {
                    continue;
                }

                ExcelSheetUtil excelSheet = new ExcelSheetUtil();
                excelSheet.sheet = sheet;
                lstRet.add(excelSheet);
            }

            return lstRet;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getName()
    {
        return sheet.getSheetName();
    }

    public void doSheetRows(IExcelRow rowRoutine)
    {
        String dbSource = null;
        Map<String, Integer> columnHeaderMap = null;
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++)
        {
            Row row = sheet.getRow(i);
            if (row == null)
            {
                continue;
            }

            Map<String, Integer> columnMap = new LinkedHashMap<>();
            if (columnHeaderMap == null)
            {
                for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++)
                {
                    Cell cell = row.getCell(j);
                    if (cell == null)
                    {
                        continue;
                    }
                    System.out.println("row: " + i + " cell: " + j + " --> " + cell.toString());
                    columnMap.put(cell.toString(), j);
                }
            }
            else
            {
                Map<String, String> mapTable = new HashMap<>();
                for (Map.Entry<String, Integer> entry : columnHeaderMap.entrySet())
                {
                    Cell cell = row.getCell(entry.getValue());
                    if (cell == null)
                    {
                        mapTable.put(entry.getKey(), "");
                        continue;
                    }

                    mapTable.put(entry.getKey(), cell.toString());
                }

                rowRoutine.onRow(dbSource, mapTable);
            }

            if (columnMap.isEmpty())
            {
                continue;
            }

            if (columnMap.size() == 1)
            {
                dbSource = columnMap.entrySet().iterator().next().getKey();
                dbSource = rowRoutine.onFirstOneColumn(dbSource);
                continue;
            }

            if (columnHeaderMap == null)
            {
                columnHeaderMap = columnMap;
                columnHeaderMap = rowRoutine.onMaybeColumnTitle(columnMap);
                continue;
            }
        }
    }

    private static Workbook getWorkbook(InputStream inStr, String fileName) throws Exception{

        Workbook wb = null;
        String fileType = fileName.substring(fileName.lastIndexOf("."));

        if(excel2003L.equals(fileType))
        {
            wb = new HSSFWorkbook(inStr);  //2003-
        }
        else if(excel2007U.equals(fileType))
        {
            wb = new XSSFWorkbook(inStr);  //2007+
        }
        else {
            throw new RuntimeException("解析的文件格式有误！");
        }

        return wb;
    }
}

package org.telegram.expensesbot.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CellStylesUtil {

    public static void makeBoldCell(XSSFCell cell, XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        cell.setCellStyle(cellStyle);
    }

    public static void changeCellFont(XSSFCell cell, XSSFWorkbook workbook, String fontName) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();

        font.setFontName(fontName);
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(cellStyle);
    }

    public static void makeItalicBoldCell(XSSFCell cell, XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();

        font.setBold(true);
        font.setItalic(true);
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(cellStyle);
    }

    public static void makeCentreAlignCell(XSSFCell cell, XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();

        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(cellStyle);
    }
}

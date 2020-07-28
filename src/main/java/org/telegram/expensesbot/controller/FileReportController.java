package org.telegram.expensesbot.controller;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.expensesbot.constants.sql.SQLConstants;
import org.telegram.expensesbot.model.Subexpenses;
import org.telegram.expensesbot.service.SubexpensesService;
import org.telegram.expensesbot.util.CellStylesUtil;
import org.telegram.expensesbot.util.DateUtil;

@Component
public class FileReportController implements DefaultFileReportController {
    private static final String NAME_NUMBER_HEADER = "===[ %s - %d ]===";
    private static final String GENERAL_SUM = "===[ Общая сумма - %d ]===";
    private static final String BLANK_CATEGORY_MESSAGE = "Category parameter is blank";
    private static final Logger log = LoggerFactory.getLogger(FileReportController.class);
    private final SubexpensesService subexpensesService;
    private XSSFSheet sheet;
    private String sheetName;
    private String category;
    private int rowsCounter = 0;
    private long chatId = 0;

    @Autowired
    public FileReportController(SubexpensesService subexpensesService) {
        this.subexpensesService = subexpensesService;
    }

    public File createAllTimeFileReport(String category) {
        if (StringUtils.isBlank(category)) {
            throw new IllegalArgumentException(BLANK_CATEGORY_MESSAGE);
        }

        this.sheetName = "всё время";
        return createReportByCategory(SQLConstants.ALL_TIME_DATE_SUBTRAHEND, category, "all_time_report.xlsx");
    }

    public File createSixMonthsFileReport(String category) {
        if (StringUtils.isBlank(category)) {
            throw new IllegalArgumentException(BLANK_CATEGORY_MESSAGE);
        }

        this.sheetName = "6 месяцев";
        return createReportByCategory(SQLConstants.SIX_MONTHS_DATE_SUBTRAHEND, category, "six_month_report.xlsx");
    }

    public File createThirtyDaysFileReport(String category) {
        if (StringUtils.isBlank(category)) {
            throw new IllegalArgumentException(BLANK_CATEGORY_MESSAGE);
        }

        this.sheetName = "30 дней";
        return createReportByCategory(SQLConstants.THIRTY_DAYS_DATE_SUBTRAHEND, category, "thirty_days_report.xlsx");
    }

    public File createSevenDaysFileReport(String category) {
        if (StringUtils.isBlank(category)) {
            throw new IllegalArgumentException(BLANK_CATEGORY_MESSAGE);
        }

        this.sheetName = "7 дней";
        return createReportByCategory(SQLConstants.SEVEN_DAYS_DATE_SUBTRAHEND, category, "seven_days_report.xlsx");
    }

    private File createReportByCategory(String subtrahend, String category, String fileName) {
        List<Date> monthYearBuffer = new ArrayList<>();
        this.category = category.equals("Суммарно") ? SQLConstants.ANY_STRING_SQL_REGEX : category;
        rowsCounter = 0;

        File file = new File(Files.createTempDir(), fileName);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
            OutputStream outputStream = new FileOutputStream(file)) {

            sheet = initSheet(workbook);
            writeReportTimeHeader();
            writeGeneralSumHeader(subtrahend);
            writeSubexpenses(subtrahend, monthYearBuffer);
            writeMonthsExpenses(subtrahend, monthYearBuffer);

            workbook.write(outputStream);
        } catch (IOException e) {
            log.error("Cannot create report file", e);
        }
        return  file;
    }

    private XSSFSheet initSheet(XSSFWorkbook workbook) {
        XSSFSheet workbookSheet = workbook.createSheet(sheetName);
        workbookSheet.setColumnWidth(0, 4000);
        workbookSheet.setColumnWidth(1, 8000);
        workbookSheet.setColumnWidth(2, 4000);

        return workbookSheet;
    }

    private void writeReportTimeHeader() {
        String middleCellText = String.format("❯ Отчёт за %s ❮", sheetName);
        XSSFCell cell = writeToMiddleCell(rowsCounter, middleCellText);

        CellStylesUtil.changeCellFont(cell, sheet.getWorkbook(), "Courier New");
    }

    private void writeGeneralSumHeader(String subtrahend) {
        Long summary = subexpensesService
            .findSumAfterSubtraction(chatId, this.category, subtrahend);
        String generalSum = String.format(GENERAL_SUM, summary);

        rowsCounter++;
        XSSFCell cell = writeToMiddleCell(rowsCounter, generalSum);
        CellStylesUtil.makeBoldCell(cell, sheet.getWorkbook());
    }

    private void writeSubexpenses(String subtrahend, List<Date> monthYearBuffer) {
        List<Subexpenses> subexpenses = subexpensesService.
            findAllAfterSubtraction(chatId, category, subtrahend);
        List<String> categoriesBuffer = new ArrayList<>();

        for (Subexpenses expenses : subexpenses) {
            writeCategoryHeader(expenses, subtrahend, categoriesBuffer);
            DateUtil.addToMonthYearBuffer(expenses, monthYearBuffer);
            writeSubexpensesInRow(expenses);
        }
    }

    private void writeCategoryHeader(Subexpenses subexpenses, String subtrahend, List<String> categoriesBuffer) {
        String subcategory = subexpenses.getCategory();

        if (!categoriesBuffer.contains(subcategory)) {
            Long categoryExpenses = subexpensesService
                .findSumAfterSubtraction(chatId, subcategory, subtrahend);
            String categoryHeader = String.format(NAME_NUMBER_HEADER, subcategory, categoryExpenses);

            categoriesBuffer.add(subcategory);
            writeHeader(categoryHeader);
        }
    }

    private void writeHeader(String categoryHeader) {
        rowsCounter++;
        XSSFCell cell = writeToMiddleCell(rowsCounter, categoryHeader);
        CellStylesUtil.makeBoldCell(cell, sheet.getWorkbook());

        XSSFRow row = sheet.createRow(rowsCounter);
        cell = writeToCell(row, 0, "Сумма");
        CellStylesUtil.makeItalicBoldCell(cell, sheet.getWorkbook());
        cell = writeToCell(row, 1, "Причина");
        CellStylesUtil.makeItalicBoldCell(cell, sheet.getWorkbook());
        cell = writeToCell(row, 2, "Дата");
        CellStylesUtil.makeItalicBoldCell(cell, sheet.getWorkbook());
        rowsCounter++;
    }

    private void writeSubexpensesInRow(Subexpenses subexpenses) {
        XSSFRow row = sheet.createRow(rowsCounter);

        writeToCell(row, 0, Integer.toString(subexpenses.getSubexpenses()));
        writeToCell(row, 1, subexpenses.getReasons());
        writeToCell(row, 2, subexpenses.getDate());

        rowsCounter++;
    }

    private void writeMonthsExpenses(String subtrahend, List<Date> monthYearBuffer) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.yyyy");

        monthYearBuffer.sort(Collections.reverseOrder());

        for (Date monthYear : monthYearBuffer) {
            final String monthYearFormat = dateFormat.format(monthYear);
            int monthNumber = Integer.parseInt(StringUtils.substringBefore(monthYearFormat, "."));
            int year = Integer.parseInt(StringUtils.substringAfter(monthYearFormat, "."));
            String oneMonthExpenses = createMonthReportHeader(subtrahend, monthNumber, year);

            writeHeader(oneMonthExpenses);

            List<Subexpenses> subexpenses = subexpensesService.
                findAllAfterSubtractionByMonthYear(chatId, category, subtrahend, monthNumber, year);

            for (Subexpenses expenses : subexpenses) {
                writeSubexpensesInRow(expenses);
            }
        }
    }

    private XSSFCell writeToCell(XSSFRow row, int columnIndex, String text) {
        XSSFCell cell = row.createCell(columnIndex);

        cell.setCellValue(text);
        CellStylesUtil.makeCentreAlignCell(cell, sheet.getWorkbook());

        return cell;
    }

    private XSSFCell writeToMiddleCell(int rowIndex, String text) {
        XSSFRow row = sheet.createRow(rowIndex);
        XSSFCell cell = row.createCell(1);

        cell.setCellValue(text);
        CellStylesUtil.makeBoldCell(cell, sheet.getWorkbook());
        rowsCounter++;

        return cell;
    }

    private String createMonthReportHeader(String subtrahend, int monthNumber, int year) {
        String monthYearName = String.format("%d, %s", year, DateUtil.getMonthName(monthNumber));
        Long summary = subexpensesService.
            findSumAfterSubtractionByMonthYear(chatId, category, subtrahend, monthNumber, year);

        return String.format(NAME_NUMBER_HEADER, monthYearName, summary);
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}

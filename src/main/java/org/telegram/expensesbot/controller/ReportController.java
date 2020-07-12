package org.telegram.expensesbot.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.expensesbot.constants.sql.SQLConstants;
import org.telegram.expensesbot.model.Subexpenses;
import org.telegram.expensesbot.service.SubexpensesService;
import org.telegram.expensesbot.util.DateUtil;

@Component
public class ReportController {
    private static final String REPORTS_PATH = "src\\main\\java\\org\\telegram\\expensesbot\\resources\\reports";
    private static final String BOLD_NAME_NUMBER_HEADER = "\n<b>===[ %s - %d ]===</b>";
    private static final String BOLD_GENERAL_SUM = "\n<b>===[ Общая сумма - %d ]===</b>";
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);
    private StringJoiner report;
    private String category;
    private long chatId = 0;

    @Autowired
    private SubexpensesService subexpensesService;

    public String createAllTimeReportMessage(String category) {
        report = new StringJoiner(System.lineSeparator());

        report.add("<b>❯ Отчёт за всё время ❮</b>");
        if (!StringUtils.isBlank(category)) {
            createReportByCategory(SQLConstants.ALL_TIME_DATE_SUBTRAHEND, category);
        }
        return report.toString();
    }

    public String createHalfYearReportMessage(String category) {
        report = new StringJoiner(System.lineSeparator());

        report.add("<b>❯ Отчёт за последние 6 месяцев ❮</b>");
        if (!StringUtils.isBlank(category)) {
            createReportByCategory(SQLConstants.SIX_MONTHS_DATE_SUBTRAHEND, category);
        }
        return report.toString();
    }

    public String createMonthReportMessage(String category) {
        report = new StringJoiner(System.lineSeparator());

        report.add("<b>❯ Отчёт за последние 30 дней ❮</b>");
        if (!StringUtils.isBlank(category)) {
            createReportByCategory(SQLConstants.ONE_MONTH_DATE_SUBTRAHEND, category);
        }
        return report.toString();
    }

    public String createSevenDaysReportMessage(String category) {
        report = new StringJoiner(System.lineSeparator());

        report.add("<b>❯ Отчёт за последние 7 дней ❮</b>");
        if (!StringUtils.isBlank(category)) {
            createReportByCategory(SQLConstants.SEVEN_DAYS_DATE_SUBTRAHEND, category);
        }
        return report.toString();
    }

    private void createReportByCategory(String subtrahend, String category) {
        this.category = category.equals("Суммарно") ? SQLConstants.ANY_STRING_SQL_REGEX : category;
        if (!StringUtils.isBlank(subtrahend)) {
            createReportAfterDateSubtraction(subtrahend);
        }
    }

    private void createReportAfterDateSubtraction(String subtrahend) {
        List<Subexpenses> subexpenses = subexpensesService.
            findAllAfterSubtraction(chatId, category, subtrahend);
        List<String> monthsYearBuffer = new ArrayList<>();
        List<String> categoriesBuffer = new ArrayList<>();

        addGeneralExpensesToReport(subtrahend);

        for (Subexpenses expenses : subexpenses) {
            addCategoriesHeaderToReport(categoriesBuffer, expenses, subtrahend);
            addToMonthYearBuffer(expenses, monthsYearBuffer);
            report.add(expenses.toString());
        }

        addMonthsExpensesToReport(subtrahend, monthsYearBuffer);
    }

    private void addGeneralExpensesToReport(String subtrahend) {
        if (category.equals(SQLConstants.ANY_STRING_SQL_REGEX)) {
            Long summary = subexpensesService.findSumAfterSubtraction(chatId, category, subtrahend);
            String generalSum = String.format(BOLD_GENERAL_SUM, summary);
            report.add(generalSum);
        }
    }

    private void addCategoriesHeaderToReport(List<String> categoriesBuffer,
        Subexpenses subexpenses, String subtrahend) {
        String subcategory = subexpenses.getCategory();

        if (!categoriesBuffer.contains(subcategory)) {
            Long categoryExpenses = subexpensesService
                .findSumAfterSubtraction(chatId, subcategory, subtrahend);
            String categoryHeader = String.format(BOLD_NAME_NUMBER_HEADER, subcategory, categoryExpenses);

            categoriesBuffer.add(subcategory);
            report.add(categoryHeader);
        }
    }

    private void addToMonthYearBuffer(Subexpenses subexpenses, List<String> monthYearBuffer) {
        String monthAndYear = subexpenses.getDate().substring(3);

        if (!monthYearBuffer.contains(monthAndYear)) {
            monthYearBuffer.add(monthAndYear);
        }
    }

    private void addMonthsExpensesToReport(String subtrahend, List<String> monthYearBuffer) {
        report.add("");
        monthYearBuffer.forEach(monthYear -> {
            int monthNumber = Integer.parseInt(StringUtils.substringBefore(monthYear, "."));
            int year = Integer.parseInt(StringUtils.substringAfter(monthYear, "."));
            String oneMonthExpenses = createMonthReportHeader(subtrahend, monthNumber, year);
            report.add(oneMonthExpenses);

            List<Subexpenses> subexpenses = subexpensesService.
                findAllAfterSubtractionByMonthYear(chatId, category, subtrahend, monthNumber, year);

            subexpenses.forEach(expenses -> report.add(expenses.toString()));
        });
    }

    private String createMonthReportHeader(String subtrahend, int monthNumber, int year) {
        String monthYearName = String.format("%d, %s", year, DateUtil.getMonthName(monthNumber));
        Long summary = subexpensesService.
            findSumAfterSubtractionByMonthYear(chatId, category, subtrahend, monthNumber, year);

        return String.format(BOLD_NAME_NUMBER_HEADER, monthYearName, summary);
    }

    //TODO: After messages
    public File createAllTimeFileReport(String category) {
        String directoryPath = String.format("%s\\%d", REPORTS_PATH, chatId);
        File directory = new File(directoryPath);

        if (!directory.exists() && directory.mkdirs()) {
            log.info("Successfully created new directory : {}", directoryPath);
        }

        String filePath = String.format("%s\\summary_all_time_report.csv", directoryPath);
        File reportFile = new File(filePath);

        String content = "EMPTY";

        try (FileOutputStream output = new FileOutputStream(reportFile)) {
            if (!reportFile.exists() && reportFile.createNewFile()) {
                log.info("Successfully created new file : {}", filePath);
            }

            byte[] contentInBytes = content.getBytes();

            output.write(contentInBytes);
            output.flush();
        } catch (IOException e) {
            log.error("Cannot work with file: {}", filePath);
        }

        return reportFile;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}

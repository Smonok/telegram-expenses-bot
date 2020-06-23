package org.telegram.expensesbot;

import org.apache.commons.lang.StringUtils;

public class ExpensesParser {
    /*any number|any quantity of whitespaces|-|any quantity of whitespaces|any string without '-'|*/
    private static final String EXPENSES_LINES_REGEX = "((\\d+\\s*\\-\\s*[^-]*)\\n?)+";

    public static boolean isExpensesLines(String message) {
        return message.matches(EXPENSES_LINES_REGEX);
    }

    public static int parseExpenses(String message) {
        return Integer.parseInt(message.split("-")[0].trim());
    }

    public static String[] splitByNewLine(String message) {
        return message.split("\\r?\\n");
    }

    public static String parseCategoryName(String message) {
        return StringUtils.substringBeforeLast(message, "-").trim().substring(1);
    }

    public static String[] parseForReport(String message) {
        String[] report = report = message.split("-");
        for (String reportPart : report) {
            reportPart = reportPart.trim();
        }

        return report;
    }
}

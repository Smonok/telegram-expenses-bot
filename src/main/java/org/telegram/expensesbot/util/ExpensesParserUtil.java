package org.telegram.expensesbot.util;

import org.apache.commons.lang.StringUtils;

public class ExpensesParserUtil {
    /*any number|any quantity of whitespaces|-|any quantity of whitespaces|any string without '-'|*/
    private static final String EXPENSES_LINES_REGEX = "((\\d+\\s*-\\s*[^-]*)\\n?)+";

    public static boolean isExpensesLines(String message) {
        return message.matches(EXPENSES_LINES_REGEX);
    }

    public static int parseExpenses(String message) {
        return Integer.parseInt(message.split("-")[0].trim());
    }

    public static String parseReasons(String message){
        return StringUtils.substringAfterLast(message, "-").trim();
    }

    public static String[] splitByNewLine(String message) {
        return message.split("\\r?\\n");
    }

    public static String parseCategoryName(String message) {
        return StringUtils.substringBeforeLast(message, "-").trim().substring(1);
    }
}

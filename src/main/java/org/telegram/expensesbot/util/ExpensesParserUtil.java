package org.telegram.expensesbot.util;

import java.util.Arrays;
import org.apache.commons.lang.StringUtils;

public class ExpensesParserUtil {
    /*whitespaces|natural number|whitespaces|-|whitespaces|any string without '-'|*/
    private static final String EXPENSES_LINES_REGEX = "((\\s*(0*[1-9][0-9]*)\\s*-\\s*[^-]*)\\n?)+";
    private static final String BLANK_PARAMETER_ERROR = "Blank message parameter";

    public static boolean isExpensesLines(String message) {
        if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException(BLANK_PARAMETER_ERROR);
        }

        return message.matches(EXPENSES_LINES_REGEX);
    }

    public static int countSeparators(String message) {
        if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException(BLANK_PARAMETER_ERROR);
        }

        return StringUtils.countMatches(message, "-");
    }

    public static boolean isTooBigExpenses(String message) {
        if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException(BLANK_PARAMETER_ERROR);
        }

        String[] messageLines = splitByNewLine(message);
        long count = Arrays.stream(messageLines)
            .filter(line -> parseExpenses(line) < 0).count();

        return count > 0;
    }

    public static int parseExpenses(String message) {
        if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException(BLANK_PARAMETER_ERROR);
        }

        String expenses = message.split("-")[0].trim();

        if (StringUtils.stripStart(expenses, "0").length() > 9) {
            return -1;
        }

        return Integer.parseInt(message.split("-")[0].trim());
    }

    public static String parseReasons(String message) {
        if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException(BLANK_PARAMETER_ERROR);
        }

        return StringUtils.substringAfterLast(message, "-").trim();
    }

    public static boolean isExpensesNonNaturalNumber(String message) {
        if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException(BLANK_PARAMETER_ERROR);
        }

        String[] expensesLines = splitByNewLine(message);
        long count = Arrays.stream(expensesLines)
            .filter(line -> line.trim().charAt(0) == '-' || line.trim().charAt(0) == '0').count();

        return count > 0;
    }

    public static String[] splitByNewLine(String message) {
        if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException(BLANK_PARAMETER_ERROR);
        }

        return message.split("\\r?\\n");
    }

    public static String parseCategoryName(String message) {
        if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException(BLANK_PARAMETER_ERROR);
        }

        return StringUtils.substringBeforeLast(message, "-").trim().substring(1);
    }
}

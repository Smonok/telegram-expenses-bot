package org.telegram.expensesbot.util;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;

public class DateUtil {

    public static String getMonthName(int monthNumber) {
        Month month = Month.of(monthNumber);
        Locale locale = Locale.forLanguageTag("ru");
        String monthName = month.getDisplayName(TextStyle.FULL_STANDALONE, locale);
        return StringUtils.capitalize(monthName);
    }
}

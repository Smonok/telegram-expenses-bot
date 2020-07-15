package org.telegram.expensesbot.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.expensesbot.model.Subexpenses;

public class DateUtil {
    private static final Logger log = LoggerFactory.getLogger(DateUtil.class);

    public static String getMonthName(int monthNumber) {
        if (monthNumber < 1 || monthNumber > 12) {
            throw new IllegalArgumentException("Illegal month number");
        }

        Month month = Month.of(monthNumber);
        Locale locale = Locale.forLanguageTag("ru");
        String monthName = month.getDisplayName(TextStyle.FULL_STANDALONE, locale);
        return StringUtils.capitalize(monthName);
    }

    public static void addToMonthYearBuffer(Subexpenses subexpenses, List<Date> monthYearBuffer) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.yyyy");
        try {
            Date monthAndYear = dateFormat.parse(subexpenses.getDate().substring(3));

            if (!monthYearBuffer.contains(monthAndYear)) {
                monthYearBuffer.add(monthAndYear);
            }
        } catch (ParseException e) {
            log.error("Cannot parse date: {} with date format pattern: {}",
                subexpenses.getDate().substring(3), dateFormat.toPattern());
        }
    }
}

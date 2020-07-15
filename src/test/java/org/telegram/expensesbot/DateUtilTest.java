package org.telegram.expensesbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.telegram.expensesbot.util.DateUtil;

public class DateUtilTest {

    @Test
    public void getMonthNameShouldThrowIllegalArgumentExceptionWhenNumberLessOneGreaterTwelve() {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getMonthName(0));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getMonthName(13));
    }

    @Test
    public void getMonthNameShouldReturnRussianMonthNameWithUpperCaseFirstLetterWhenNumberFromOneToTwelve() {
        String expected = "Январь";
        int firstMonth = 1;
        String actual = DateUtil.getMonthName(firstMonth);

        assertEquals(expected, actual);
    }
}

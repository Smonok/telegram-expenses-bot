package org.telegram.expensesbot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ExpensesParserUtilTest {

    @Test
    public void isExpensesLinesShouldThrowIllegalArgumentExceptionWhenBlankMessage() {
        final String empty = "";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(empty));

        final String blank = "   ";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(blank));

        final String nextLine = "\n";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(nextLine));
    }

    @Test
    public void isExpensesLinesShouldReturnTrueWhenOneExpensesStringWithRandomSpaces() {
        String expensesReasons = "55 - games";
        boolean actual = ExpensesParserUtil.isExpensesLines(expensesReasons);
        assertTrue(actual);

        expensesReasons = "   55 -games";
        actual = ExpensesParserUtil.isExpensesLines(expensesReasons);
        assertTrue(actual);

        expensesReasons = "55-games";
        actual = ExpensesParserUtil.isExpensesLines(expensesReasons);
        assertTrue(actual);

        expensesReasons = "55   -  games   ";
        actual = ExpensesParserUtil.isExpensesLines(expensesReasons);
        assertTrue(actual);
    }

    @Test
    public void isExpensesLinesShouldReturnTrueWhenMoreThanOneExpensesStrings() {
        String expensesReasons = "550 - games\n44 - test\n 543-simple  ";
        boolean actual = ExpensesParserUtil.isExpensesLines(expensesReasons);

        assertTrue(actual);
    }

    @Test
    public void isExpensesLinesShouldReturnTrueWhenLargeExpensesNumber() {
        String expensesReasons = "55444444444444444444444 - games";
        boolean actual = ExpensesParserUtil.isExpensesLines(expensesReasons);

        assertTrue(actual);
    }

    @Test
    public void isExpensesLinesShouldReturnTrueWhenExpensesStartsWithZero() {
        String expensesReasons = "007 - agent";
        boolean actual = ExpensesParserUtil.isExpensesLines(expensesReasons);

        assertTrue(actual);
    }

    @Test
    public void isExpensesLinesShouldReturnFalseWhenWrongSeparator() {
        String expensesReasons = "55 . games";
        boolean actual = ExpensesParserUtil.isExpensesLines(expensesReasons);

        assertFalse(actual);
    }

    @Test
    public void isExpensesLinesShouldReturnFalseWhenNonDigitFirst() {
        String expensesReasons = "-55 - games";
        boolean actual = ExpensesParserUtil.isExpensesLines(expensesReasons);

        assertFalse(actual);
    }

    @Test
    public void isExpensesLinesShouldReturnFalseWhenMessageWithTwoMoreSeparators() {
        String expensesReasons = "55 - gam-es";
        boolean actual = ExpensesParserUtil.isExpensesLines(expensesReasons);

        assertFalse(actual);
    }

    @Test
    public void isExpensesLinesShouldReturnFalseWhenExpensesEqualsZero() {
        String expensesReasons = "0 - games";
        boolean actual = ExpensesParserUtil.isExpensesLines(expensesReasons);

        assertFalse(actual);
    }

    @Test
    public void countSeparatorsShouldThrowIllegalArgumentExceptionWhenBlankMessage() {
        final String empty = "";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(empty));

        final String blank = "   ";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(blank));

        final String nextLine = "\n";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(nextLine));
    }

    @Test
    public void parseExpensesShouldThrowIllegalArgumentExceptionWhenBlankMessage() {
        final String empty = "";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(empty));

        final String blank = "   ";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(blank));

        final String nextLine = "\n";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(nextLine));
    }

    @Test
    public void isTooBigExpensesShouldThrowIllegalArgumentExceptionWhenBlankMessage() {
        final String empty = "";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(empty));

        final String blank = "   ";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(blank));

        final String nextLine = "\n";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(nextLine));
    }

    @Test
    public void parseReasonsShouldThrowIllegalArgumentExceptionWhenBlankMessage() {
        final String empty = "";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(empty));

        final String blank = "   ";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(blank));

        final String nextLine = "\n";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(nextLine));
    }

    @Test
    public void isLessThanOneExpensesShouldThrowIllegalArgumentExceptionWhenBlankMessage() {
        final String empty = "";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(empty));

        final String blank = "   ";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(blank));

        final String nextLine = "\n";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(nextLine));
    }

    @Test
    public void parseCategoryNameShouldThrowIllegalArgumentExceptionWhenBlankMessage() {
        final String empty = "";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(empty));

        final String blank = "   ";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(blank));

        final String nextLine = "\n";
        assertThrows(IllegalArgumentException.class, () -> ExpensesParserUtil.isExpensesLines(nextLine));
    }

    @Test
    public void countSeparatorsShouldReturnTwoWhenTwoSeparators() {
        int expected = 2;
        int actual = ExpensesParserUtil.countSeparators("50 - gam-es");

        assertEquals(expected, actual);
    }

    @Test
    public void parseExpensesShouldReturnNegativeNumberWhenExpensesMoreEqualsBillion() {
        int actual = ExpensesParserUtil.parseExpenses("500000000000000000 - games");

        assertTrue(actual < 0);
    }

    @Test
    public void parseExpensesShouldReturnExpensesNumberWhenLessThanBillion() {
        int expected = 5000;
        int actual = ExpensesParserUtil.parseExpenses("5000 - games");

        assertEquals(expected, actual);
    }

    @Test
    public void isTooBigExpensesShouldReturnTrueWhenExpensesMoreEqualsBillion() {
        boolean actual = ExpensesParserUtil.isTooBigExpenses("1000000000 - games");

        assertTrue(actual);
    }

    @Test
    public void parseReasonsShouldReturnReasonsWhenExpensesString() {
        String expected = "games";
        String actual = ExpensesParserUtil.parseReasons("100 - games");

        assertEquals(expected, actual);
    }

    @Test
    public void isExpensesNonNaturalNumberShouldReturnTrueWhenExpensesLessOrEqualsZero() {
        boolean actual = ExpensesParserUtil.isExpensesNonNaturalNumber("-100 - games");
        assertTrue(actual);

        actual = ExpensesParserUtil.isExpensesNonNaturalNumber("0 - games");
        assertTrue(actual);
    }

    @Test
    public void parseCategoryNameShouldReturnCategoryWithoutExpensesAndBracketsWhenCategoryButton() {
        String expected = "Goods";
        String actual = ExpensesParserUtil.parseCategoryName("'Goods - 17000");

        assertEquals(expected, actual);
    }
}

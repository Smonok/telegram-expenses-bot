package org.telegram.expensesbot.controller;

public interface DefaultMessageReportController {

    String createAllTimeReportMessage(String category);

    String createSixMonthsReportMessage(String category);

    String createThirtyDaysReportMessage(String category);

    String createSevenDaysReportMessage(String category);
}

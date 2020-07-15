package org.telegram.expensesbot.controller;

import java.io.File;

public interface DefaultFileReportController {

    File createAllTimeFileReport(String category);

    File createSixMonthsFileReport(String category);

    File createThirtyDaysFileReport(String category);

    File createSevenDaysFileReport(String category);
}

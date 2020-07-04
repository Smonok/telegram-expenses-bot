package org.telegram.expensesbot.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.expensesbot.model.Subexpenses;
import org.telegram.expensesbot.service.SubexpensesService;

@Component
public class ReportController {
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);
    private static final String REPORTS_PATH = "src\\main\\java\\org\\telegram\\expensesbot\\resources\\reports";
    private long chatId = 0;

    @Autowired
    private SubexpensesService subexpensesService;

    public String createAllTimeReportMessage() {
        StringJoiner report = new StringJoiner(System.lineSeparator());
        List<Subexpenses> subexpenses = subexpensesService.findAllByChatIdOrderByCategory(chatId);
        List<String> categoriesBuffer = new ArrayList<>();

        for (Subexpenses expenses : subexpenses) {
            String category = String.format("*==> %s <==*", expenses.getCategory());
            if (!categoriesBuffer.contains(category)) {
                categoriesBuffer.add(category);
                report.add(category);
            }

            report.add(expenses.toString());
        }

        return report.toString();
    }

    public String createAllTimeReportMessage(String category) {
        return "TEMP";
    }

    public File createAllTimeFileReport() {
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

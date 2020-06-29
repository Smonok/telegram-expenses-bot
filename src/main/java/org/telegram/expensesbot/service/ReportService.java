package org.telegram.expensesbot.service;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.expensesbot.model.Report;
import org.telegram.expensesbot.repository.ReportRepository;

@Transactional
@Service
public class ReportService implements DefaultReportService {
    @Autowired
    private ReportRepository reportRepository;

    @Override
    public Report add(Report report) {
        return reportRepository.save(report);
    }
}

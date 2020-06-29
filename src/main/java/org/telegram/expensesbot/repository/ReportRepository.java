package org.telegram.expensesbot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.telegram.expensesbot.model.Report;

@Repository
public interface ReportRepository extends CrudRepository<Report, Long> {


}

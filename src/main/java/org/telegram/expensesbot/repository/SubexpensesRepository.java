package org.telegram.expensesbot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.telegram.expensesbot.constants.sql.SubexpensesQueryConstants;
import org.telegram.expensesbot.model.Subexpenses;

@Repository
public interface SubexpensesRepository extends CrudRepository<Subexpenses, Long> {

    Long deleteAllByChatIdAndCategory(long chatId, String category);

    Long deleteAllByChatId(long chatId);

    List<Subexpenses> findAllByChatIdOrderByCategory(long chatId);

    List<Subexpenses> findAllByChatIdAndCategory(long chatId, String category);

    @Query(value = SubexpensesQueryConstants.FIND_AFTER_SUBTRACTION, nativeQuery = true)
    List<Subexpenses> findAllAfterSubtraction(long chatId, String subtrahend);

    @Query(value = SubexpensesQueryConstants.FIND_SUM, nativeQuery = true)
    Long findSumAfterSubtraction(long chatId, String subtrahend);

    @Query(value = SubexpensesQueryConstants.FIND_AFTER_SUBTRACTION_BY_CATEGORY, nativeQuery = true)
    List<Subexpenses> findAllAfterSubtraction(long chatId, String category, String subtrahend);

    @Query(value = SubexpensesQueryConstants.FIND_SUM_BY_CATEGORY, nativeQuery = true)
    Long findSumAfterSubtraction(long chatId, String category, String subtrahend);

    @Query(value = SubexpensesQueryConstants.FIND_AFTER_SUBTRACTION_BY_MONTH_YEAR, nativeQuery = true)
    List<Subexpenses> findAllAfterSubtractionByMonthYear(long chatId, String category,
        String subtrahend, int month, int year);

    @Query(value = SubexpensesQueryConstants.FIND_SUM_BY_MONTH_YEAR, nativeQuery = true)
    Long findSumAfterSubtractionByMonthYear(long chatId, String category, String subtrahend, int month, int year);
}

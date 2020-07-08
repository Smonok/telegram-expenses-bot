package org.telegram.expensesbot.service;

import java.util.List;
import org.telegram.expensesbot.model.Subexpenses;

public interface DefaultSubexpensesService {

    Subexpenses add(Subexpenses report);

    Long deleteAllByChatIdAndCategory(long chatId, String category);

    Long deleteAllByChatId(long chatId);

    List<Subexpenses> findAllByChatIdOrderByCategory(long chatId);

    List<Subexpenses> findAllByChatIdAndCategory(long chatId, String category);

    List<Subexpenses> findAllAfterSubtraction(long chatId, String subtrahend);

    Long findSumAfterSubtraction(long chatId, String subtrahend);

    List<Subexpenses> findAllAfterSubtraction(long chatId, String category, String subtrahend);

    Long findSumAfterSubtraction(long chatId, String category, String subtrahend);

    List<Subexpenses> findAllAfterSubtractionByMonthYear(long chatId, String category,
        String subtrahend, int month, int year);

    Long findSumAfterSubtractionByMonthYear(long chatId, String category, String subtrahend, int month, int year);
}

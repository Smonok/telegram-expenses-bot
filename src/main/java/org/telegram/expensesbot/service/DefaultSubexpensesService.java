package org.telegram.expensesbot.service;

import java.util.List;
import org.telegram.expensesbot.model.Subexpenses;

public interface DefaultSubexpensesService {

    Subexpenses add(Subexpenses report);

    Long deleteAllByChatIdAndCategory(long chatId, String category);

    Long deleteAllByChatId(long chatId);

    List<Subexpenses> findAllByChatIdOrderByCategory(long chatId);
}

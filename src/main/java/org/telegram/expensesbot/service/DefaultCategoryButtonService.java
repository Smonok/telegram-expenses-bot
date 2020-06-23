package org.telegram.expensesbot.service;

import java.util.List;
import org.telegram.expensesbot.model.CategoryButton;

public interface DefaultCategoryButtonService {

    CategoryButton add(CategoryButton mainKeyboard);

    List<CategoryButton> findByChatIdOrderById(long chatId);

    long deleteByCategoryAndChatId(String category, long chatId);

    CategoryButton findByCategoryAndChatId(String category, long chatId);

    boolean existsCategoryButtonByCategoryAndChatId(String category, long chatId);

    long deleteAllByChatId(long chatId);

    int updateAllExpensesByChatId(int expenses, long chatId);

    int updateCategoryButtonExpenses(int expenses, String category, long chatId);

    long calculateSummaryExpenses(long chatId);
}

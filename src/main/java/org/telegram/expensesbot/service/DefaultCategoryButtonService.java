package org.telegram.expensesbot.service;

import java.util.List;
import org.telegram.expensesbot.model.CategoryButton;

public interface DefaultCategoryButtonService {

    CategoryButton add(CategoryButton categoryButton);

    List<CategoryButton> findByChatIdOrderById(long chatId);

    Long deleteByCategoryAndChatId(String category, long chatId);

    CategoryButton findByCategoryAndChatId(String category, long chatId);

    boolean existsCategoryButtonByCategoryAndChatId(String category, long chatId);

    Long deleteAllByChatId(long chatId);

    int updateAllExpensesByChatId(long expenses, long chatId);

    int updateCategoryButtonExpenses(long expenses, String category, long chatId);

    Long calculateSummaryExpenses(long chatId);
}

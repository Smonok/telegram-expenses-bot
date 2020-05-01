package org.telegram.expensesbot.service;

import java.util.List;
import org.telegram.expensesbot.model.CategoryButton;

public interface DefaultCategoryButtonService {

    CategoryButton add(CategoryButton mainKeyboard);

    List<CategoryButton> findByChatId(long userId);

    long deleteByCategoryAndChatId(String category, long chatId);

    boolean existsCategoryButtonByCategoryAndChatId(String category, long chatId);
}

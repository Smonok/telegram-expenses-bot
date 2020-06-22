package org.telegram.expensesbot.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.expensesbot.model.CategoryButton;
import org.telegram.expensesbot.repository.CategoryButtonRepository;

@Transactional
@Service
public class CategoryButtonService implements DefaultCategoryButtonService {

    @Autowired
    private CategoryButtonRepository categoryButtonRepository;

    @Override
    public CategoryButton add(CategoryButton mainKeyboard) {
        return categoryButtonRepository.save(mainKeyboard);
    }

    @Override
    public List<CategoryButton> findByChatId(long userId) {
        return categoryButtonRepository.findByChatId(userId);
    }

    @Override
    public long deleteByCategoryAndChatId(String category, long chatId) {
        return categoryButtonRepository.deleteByCategoryAndChatId(category, chatId);
    }

    @Override
    public boolean existsCategoryButtonByCategoryAndChatId(String category, long chatId) {
        return categoryButtonRepository.existsCategoryButtonByCategoryAndChatId(category, chatId);
    }

    @Override
    public long deleteAllByChatId(long chatId) {
        return categoryButtonRepository.deleteAllByChatId(chatId);
    }

    @Override
    public int updateExpenses(int expenses, long chatId) {
        return categoryButtonRepository.updateCategoryButtonExpenses(expenses, chatId);
    }
}

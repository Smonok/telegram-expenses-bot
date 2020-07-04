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
    public CategoryButton add(CategoryButton categoryButton) {
        return categoryButtonRepository.save(categoryButton);
    }

    @Override
    public List<CategoryButton> findByChatIdOrderById(long chatId) {
        return categoryButtonRepository.findByChatIdOrderById(chatId);
    }

    @Override
    public Long deleteByCategoryAndChatId(String category, long chatId) {
        return categoryButtonRepository.deleteByCategoryAndChatId(category, chatId);
    }

    @Override
    public CategoryButton findByCategoryAndChatId(String category, long chatId) {
        return categoryButtonRepository.findByCategoryAndChatId(category, chatId);
    }

    @Override
    public boolean existsCategoryButtonByCategoryAndChatId(String category, long chatId) {
        return categoryButtonRepository.existsCategoryButtonByCategoryAndChatId(category, chatId);
    }

    @Override
    public Long deleteAllByChatId(long chatId) {
        return categoryButtonRepository.deleteAllByChatId(chatId);
    }

    @Override
    public int updateAllExpensesByChatId(int expenses, long chatId) {
        return categoryButtonRepository.updateAllExpensesByChatId(expenses, chatId);
    }

    @Override
    public int updateCategoryButtonExpenses(int expenses, String category, long chatId) {
        return categoryButtonRepository.updateCategoryButtonExpenses(expenses, category, chatId);
    }

    @Override
    public Long calculateSummaryExpenses(long chatId) {
        return categoryButtonRepository.calculateSummaryExpenses(chatId);
    }
}

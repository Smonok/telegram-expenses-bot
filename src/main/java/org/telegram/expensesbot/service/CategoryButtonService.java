package org.telegram.expensesbot.service;

import java.util.Collections;
import java.util.List;
import javax.transaction.Transactional;
import org.apache.commons.lang.StringUtils;
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
        if (chatId < 1) {
            return Collections.emptyList();
        }

        return categoryButtonRepository.findByChatIdOrderById(chatId);
    }

    @Override
    public Long deleteByCategoryAndChatId(String category, long chatId) {
        if (StringUtils.isBlank(category) || chatId < 1) {
            return 0L;
        }

        return categoryButtonRepository.deleteByCategoryAndChatId(category, chatId);
    }

    @Override
    public CategoryButton findByCategoryAndChatId(String category, long chatId) {
        if (StringUtils.isBlank(category) || chatId < 1) {
            return new CategoryButton();
        }

        return categoryButtonRepository.findByCategoryAndChatId(category, chatId);
    }

    @Override
    public boolean existsCategoryButtonByCategoryAndChatId(String category, long chatId) {
        if (StringUtils.isBlank(category) || chatId < 1) {
            return false;
        }

        return categoryButtonRepository.existsCategoryButtonByCategoryAndChatId(category, chatId);
    }

    @Override
    public Long deleteAllByChatId(long chatId) {
        if (chatId < 1) {
            return 0L;
        }

        return categoryButtonRepository.deleteAllByChatId(chatId);
    }

    @Override
    public int updateAllExpensesByChatId(long expenses, long chatId) {
        if (expenses < 0 || chatId < 1) {
            return 0;
        }

        return categoryButtonRepository.updateAllExpensesByChatId(expenses, chatId);
    }

    @Override
    public int updateCategoryButtonExpenses(long expenses, String category, long chatId) {
        if (expenses < 0 || chatId < 1 || StringUtils.isBlank(category)) {
            return 0;
        }

        return categoryButtonRepository.updateCategoryButtonExpenses(expenses, category, chatId);
    }

    @Override
    public Long calculateSummaryExpenses(long chatId) {
        if (chatId < 1) {
            return 0L;
        }

        return categoryButtonRepository.calculateSummaryExpenses(chatId);
    }
}

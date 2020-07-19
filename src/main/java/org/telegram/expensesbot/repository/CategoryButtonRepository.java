package org.telegram.expensesbot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.telegram.expensesbot.constants.sql.CategoryButtonQueryConstants;
import org.telegram.expensesbot.model.CategoryButton;

@Repository
public interface CategoryButtonRepository extends CrudRepository<CategoryButton, Long> {

    List<CategoryButton> findByChatIdOrderById(long chatId);

    Long deleteByCategoryAndChatId(String category, long chatId);

    CategoryButton findByCategoryAndChatId(String category, long chatId);

    boolean existsCategoryButtonByCategoryAndChatId(String category, long chatId);

    Long deleteAllByChatId(long chatId);

    @Modifying
    @Query(CategoryButtonQueryConstants.UPDATE_EXPENSES_BY_CHAT_ID)
    int updateAllExpensesByChatId(long expenses, long chatId);

    @Modifying
    @Query(CategoryButtonQueryConstants.UPDATE_EXPENSES_BY_CHAT_ID_AND_CATEGORY)
    int updateCategoryButtonExpenses(long expenses, String category, long chatId);

    @Query(CategoryButtonQueryConstants.SELECT_EXPENSES_SUM_BY_CHAT_ID)
    Long calculateSummaryExpenses(long chatId);
}

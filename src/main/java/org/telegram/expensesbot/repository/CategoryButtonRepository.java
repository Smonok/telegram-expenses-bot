package org.telegram.expensesbot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.telegram.expensesbot.model.CategoryButton;

@Repository
public interface CategoryButtonRepository extends CrudRepository<CategoryButton, Long> {

    List<CategoryButton> findByChatIdOrderById(long chatId);

    long deleteByCategoryAndChatId(String category, long chatId);

    CategoryButton findByCategoryAndChatId(String category, long chatId);

    boolean existsCategoryButtonByCategoryAndChatId(String category, long chatId);

    long deleteAllByChatId(long chatId);

    @Modifying
    @Query("update CategoryButton button set button.expenses = ?1 where button.chatId = ?2")
    int updateAllExpensesByChatId(int expenses, long chatId);

    @Modifying
    @Query("update CategoryButton button set button.expenses = ?1 where button.category = ?2 and button.chatId = ?3")
    int updateCategoryButtonExpenses(int expenses, String category, long chatId);

    @Query("select sum(expenses) from CategoryButton where chatId = ?1")
    long calculateSummaryExpenses(long chatId);
}

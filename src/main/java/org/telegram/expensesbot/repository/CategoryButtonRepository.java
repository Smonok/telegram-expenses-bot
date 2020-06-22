package org.telegram.expensesbot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.telegram.expensesbot.model.CategoryButton;

@Repository
public interface CategoryButtonRepository extends CrudRepository<CategoryButton, Long> {

    List<CategoryButton> findByChatId(long chatId);
    long deleteByCategoryAndChatId(String category, long chatId);
    boolean existsCategoryButtonByCategoryAndChatId(String category, long chatId);
    long deleteAllByChatId(long chatId);

    @Modifying
    @Query("update CategoryButton button set button.expenses = ?1 where button.chatId = ?2")
    int updateCategoryButtonExpenses(int expenses, long chatId);
}

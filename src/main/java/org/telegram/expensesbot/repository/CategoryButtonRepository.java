package org.telegram.expensesbot.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.telegram.expensesbot.model.CategoryButton;

@Repository
public interface CategoryButtonRepository extends CrudRepository<CategoryButton, Long> {

    List<CategoryButton> findByChatId(long chatId);
    long deleteByCategoryAndChatId(String category, long chatId);
    boolean existsCategoryButtonByCategoryAndChatId(String category, long chatId);
}

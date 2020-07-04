package org.telegram.expensesbot.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.telegram.expensesbot.model.Subexpenses;

@Repository
public interface SubexpensesRepository extends CrudRepository<Subexpenses, Long> {

    Long deleteAllByChatIdAndCategory(long chatId, String category);

    Long deleteAllByChatId(long chatId);

    List<Subexpenses> findAllByChatIdOrderByCategory(long chatId);
}

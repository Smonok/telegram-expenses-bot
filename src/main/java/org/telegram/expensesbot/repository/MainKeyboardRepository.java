package org.telegram.expensesbot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.telegram.expensesbot.model.MainKeyboard;

@Repository
public interface MainKeyboardRepository extends JpaRepository<MainKeyboard, Long> {

    List<MainKeyboard> findByUserId(long userId);
}

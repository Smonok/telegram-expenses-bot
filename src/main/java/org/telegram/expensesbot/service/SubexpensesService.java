package org.telegram.expensesbot.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.expensesbot.model.Subexpenses;
import org.telegram.expensesbot.repository.SubexpensesRepository;

@Transactional
@Service
public class SubexpensesService implements DefaultSubexpensesService {
    @Autowired
    private SubexpensesRepository subexpensesRepository;

    @Override
    public Subexpenses add(Subexpenses subexpenses) {
        return subexpensesRepository.save(subexpenses);
    }

    @Override
    public Long deleteAllByChatIdAndCategory(long chatId, String category) {
        return subexpensesRepository.deleteAllByChatIdAndCategory(chatId, category);
    }

    @Override
    public Long deleteAllByChatId(long chatId){
        return subexpensesRepository.deleteAllByChatId(chatId);
    }

    @Override
    public List<Subexpenses> findAllByChatIdOrderByCategory(long chatId) {
        return subexpensesRepository.findAllByChatIdOrderByCategory(chatId);
    }
}

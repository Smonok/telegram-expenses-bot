package org.telegram.expensesbot.service;

import java.util.Collections;
import java.util.List;
import javax.transaction.Transactional;
import org.apache.commons.lang.StringUtils;
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
        if (subexpenses == null) {
            return new Subexpenses();
        }

        return subexpensesRepository.save(subexpenses);
    }

    @Override
    public Long deleteAllByChatIdAndCategory(long chatId, String category) {
        if (chatId < 1 || StringUtils.isBlank(category)) {
            return 0L;
        }

        return subexpensesRepository.deleteAllByChatIdAndCategory(chatId, category);
    }

    @Override
    public Long deleteAllByChatId(long chatId) {
        if (chatId < 1) {
            return 0L;
        }

        return subexpensesRepository.deleteAllByChatId(chatId);
    }

    @Override
    public List<Subexpenses> findAllByChatIdOrderByCategory(long chatId) {
        if (chatId < 1) {
            return Collections.emptyList();
        }

        return subexpensesRepository.findAllByChatIdOrderByCategory(chatId);
    }

    @Override
    public List<Subexpenses> findAllByChatIdAndCategory(long chatId, String category) {
        if (chatId < 1 || StringUtils.isBlank(category)) {
            return Collections.emptyList();
        }

        return subexpensesRepository.findAllByChatIdAndCategory(chatId, category);
    }

    @Override
    public List<Subexpenses> findAllAfterSubtraction(long chatId, String subtrahend) {
        if (chatId < 1 || StringUtils.isBlank(subtrahend)) {
            return Collections.emptyList();
        }

        return subexpensesRepository.findAllAfterSubtraction(chatId, subtrahend);
    }

    @Override
    public Long findSumAfterSubtraction(long chatId, String subtrahend) {
        if (chatId < 1 || StringUtils.isBlank(subtrahend)) {
            return 0L;
        }

        return subexpensesRepository.findSumAfterSubtraction(chatId, subtrahend);
    }

    @Override
    public List<Subexpenses> findAllAfterSubtraction(long chatId, String category, String subtrahend) {
        if (chatId < 1 || StringUtils.isBlank(category) || StringUtils.isBlank(subtrahend)) {
            return Collections.emptyList();
        }

        return subexpensesRepository.findAllAfterSubtraction(chatId, category, subtrahend);
    }

    @Override
    public Long findSumAfterSubtraction(long chatId, String category, String subtrahend) {
        if (chatId < 1 || StringUtils.isBlank(category) || StringUtils.isBlank(subtrahend)) {
            return 0L;
        }

        return subexpensesRepository.findSumAfterSubtraction(chatId, category, subtrahend);
    }

    @Override
    public List<Subexpenses> findAllAfterSubtractionByMonthYear(long chatId, String category,
        String subtrahend, int month, int year) {
        if (month < 1 || year < 1 || chatId < 1 || StringUtils.isBlank(category) || StringUtils.isBlank(subtrahend)) {
            return Collections.emptyList();
        }

        return subexpensesRepository.findAllAfterSubtractionByMonthYear(chatId, category, subtrahend, month, year);
    }

    @Override
    public Long findSumAfterSubtractionByMonthYear(long chatId, String category,
        String subtrahend, int month, int year) {
        if (month < 1 || year < 1 || chatId < 1 || StringUtils.isBlank(category) || StringUtils.isBlank(subtrahend)) {
            return 0L;
        }

        return subexpensesRepository.findSumAfterSubtractionByMonthYear(chatId, category, subtrahend, month, year);
    }
}

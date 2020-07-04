package org.telegram.expensesbot.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.expensesbot.ExpensesParser;
import org.telegram.expensesbot.model.CategoryButton;
import org.telegram.expensesbot.model.Subexpenses;
import org.telegram.expensesbot.service.CategoryButtonService;
import org.telegram.expensesbot.service.SubexpensesService;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Component
public class MainKeyboardController {
    private static final String CATEGORIES_CONTROL = "'Управление категориями'";
    private static final String SUMMARY = "Суммарно";
    private static final int HEADER_ROWS_NUMBER = 2;
    private static final Logger log = LoggerFactory.getLogger(MainKeyboardController.class);
    private long chatId = 0;
    private final Map<Long, List<KeyboardRow>> cache = new HashMap<>();

    @Autowired
    private CategoryButtonService buttonService;

    @Autowired
    private SubexpensesService subexpensesService;

    public List<KeyboardRow> addCategory(String name) {
        final int beginExpenses = 0;
        buttonService.add(new CategoryButton(chatId, name, beginExpenses));
        log.info("add category method: ->{}<- for chat: {}", name, chatId);

        if (cache.containsKey(chatId)) {
            log.info("add ->{}<- to cache for chat: {}", name, chatId);
            return addButtonToCacheKeyboard(name, beginExpenses);
        }

        log.info("fillEmptyKeyboard() in addCategory: ->{}<- for chat: {}", name, chatId);

        return fillEmptyKeyboard();
    }

    private List<KeyboardRow> addButtonToCacheKeyboard(String name, final int beginExpenses) {
        KeyboardController keyboardController = new KeyboardController(cache.get(chatId));
        String buttonName = combineButtonName(name, beginExpenses);
        keyboardController.addButton(buttonName);

        return cache.get(chatId);
    }

    public List<KeyboardRow> deleteCategory(String buttonName) {
        String category = ExpensesParser.parseCategoryName(buttonName);
        log.info("deleteCategory: ->{}<- for chat: {}", buttonName, chatId);
        buttonService.deleteByCategoryAndChatId(category, chatId);

        subexpensesService.deleteAllByChatIdAndCategory(chatId, category);
        log.info("delete all reports with category: ->{}<- for chat: {}", category, chatId);

        if (cache.containsKey(chatId)) {
            log.info("delete ->{}<- from cache for chat: {}", buttonName, chatId);
            return deleteButtonFromCacheKeyboard(buttonName);
        }

        log.info("fillEmptyKeyboard() in deleteCategory: ->{}<- for chat: {}", buttonName, chatId);
        return fillEmptyKeyboard();
    }

    private List<KeyboardRow> deleteButtonFromCacheKeyboard(String buttonName) {
        KeyboardController keyboardController = new KeyboardController(cache.get(chatId));

        keyboardController.deleteButton(buttonName);
        initHeader(cache.get(chatId));

        return cache.get(chatId);
    }

    public List<KeyboardRow> resetExpenses() {
        final int resultExpenses = 0;

        resetDBExpenses(resultExpenses);

        if (cache.containsKey(chatId)) {
            log.info("reset cache expenses for chat: {}", chatId);

            resetCacheExpenses(resultExpenses);
            return cache.get(chatId);
        }

        return fillEmptyKeyboard();
    }

    private void resetDBExpenses(final int resultExpenses) {
        log.info("reset DB expenses for chat: {}", chatId);
        buttonService.updateAllExpensesByChatId(resultExpenses, chatId);
    }

    private void resetCacheExpenses(final int resultExpenses) {
        List<KeyboardRow> cacheKeyboard = cache.get(chatId);

        for (int i = HEADER_ROWS_NUMBER; i < cacheKeyboard.size(); i++) {
            cacheKeyboard.get(i).forEach(button -> {
                String category = ExpensesParser.parseCategoryName(button.getText());
                String buttonName = combineButtonName(category, resultExpenses);

                button.setText(buttonName);
            });
        }
    }

    public List<KeyboardRow> removeUserExpenses() {
        buttonService.deleteAllByChatId(chatId);
        subexpensesService.deleteAllByChatId(chatId);
        cache.remove(chatId);

        List<KeyboardRow> keyboard = new ArrayList<>();
        initHeader(keyboard);

        return keyboard;
    }

    public List<KeyboardRow> changeButtonExpenses(String category, String expensesMessage) {
        CategoryButton categoryButton = buttonService.findByCategoryAndChatId(category, chatId);
        int currentExpenses = categoryButton.getExpenses();
        int resultExpenses = calculateResultExpenses(currentExpenses, expensesMessage);
        Subexpenses subexpenses = new Subexpenses();

        subexpenses.setChatId(chatId);
        subexpenses.setCategory(category);

        buttonService.updateCategoryButtonExpenses(resultExpenses, category, chatId);
        saveReport(category, expensesMessage);

        return fillEmptyKeyboard();
    }

    private void saveReport(String category, String expensesMessage) {
        String[] expensesLines = ExpensesParser.splitByNewLine(expensesMessage);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyy");
        String date = simpleDateFormat.format(new Date());

        Arrays.stream(expensesLines).forEach(line -> {
            int subexpenses = ExpensesParser.parseExpenses(line);
            String reasons = ExpensesParser.parseReasons(line);

            subexpensesService.add(new Subexpenses(chatId, category, subexpenses, reasons, date));
        });
    }

    private int calculateResultExpenses(int currentExpenses, String expensesMessage) {
        String[] expensesLines = ExpensesParser.splitByNewLine(expensesMessage);

        for (String line : expensesLines) {
            currentExpenses += ExpensesParser.parseExpenses(line);
        }

        return currentExpenses;
    }

    private List<KeyboardRow> fillEmptyKeyboard() {
        List<KeyboardRow> keyboard = new ArrayList<>();

        initHeader(keyboard);
        fillKeyboardFromDB(keyboard);

        cache.put(chatId, keyboard);
        return keyboard;
    }

    private void initHeader(List<KeyboardRow> keyboard) {
        addFirstRow(keyboard);
        addSecondRow(keyboard);
    }

    private void addFirstRow(List<KeyboardRow> keyboard) {
        KeyboardRow firstRow = new KeyboardRow();

        firstRow.add(CATEGORIES_CONTROL);
        firstRow.add("'Помощь'");

        if (keyboard.isEmpty()) {
            keyboard.add(firstRow);
        } else {
            keyboard.set(0, firstRow);
        }
    }

    private void addSecondRow(List<KeyboardRow> keyboard) {
        KeyboardRow secondRow = new KeyboardRow();
        Long summaryExpenses = Optional.ofNullable(buttonService.calculateSummaryExpenses(chatId)).orElse(0L);
        String summaryExpensesButtonName = String.format("'%s - %d'", SUMMARY, summaryExpenses);

        secondRow.add(summaryExpensesButtonName);

        if (keyboard.size() < 2) {
            keyboard.add(secondRow);
        } else {
            keyboard.set(1, secondRow);
        }
    }

    private void fillKeyboardFromDB(List<KeyboardRow> keyboard) {
        List<CategoryButton> keyboardButtons = buttonService.findByChatIdOrderById(chatId);
        KeyboardController keyboardController = new KeyboardController(keyboard);

        keyboardButtons.forEach(keyboardButton -> {
            String category = keyboardButton.getCategory();
            int expenses = keyboardButton.getExpenses();
            String buttonName = combineButtonName(category, expenses);

            keyboardController.addButton(buttonName);
        });
    }

    public boolean isSummaryButton(String message) {
        if (message.charAt(0) == '\'') {
            String buttonName = ExpensesParser.parseCategoryName(message);
            return buttonName.equals(SUMMARY);
        }
        return false;
    }

    public boolean isCategoryButton(String buttonName) {
        if (buttonName.charAt(0) == '\'') {
            String category = ExpensesParser.parseCategoryName(buttonName);

            return buttonService.existsCategoryButtonByCategoryAndChatId(category, chatId);
        }
        return false;
    }

    public boolean isCategoryExists(String category) {
        return buttonService.existsCategoryButtonByCategoryAndChatId(category, chatId);
    }

    public String combineButtonName(String name, int expenses) {
        return String.format("'%s - %d'", name, expenses);
    }

    public List<KeyboardRow> getOnlyHeadedKeyboard() {
        List<KeyboardRow> onlyHeadedKeyboard = new ArrayList<>();
        initHeader(onlyHeadedKeyboard);
        return onlyHeadedKeyboard;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}

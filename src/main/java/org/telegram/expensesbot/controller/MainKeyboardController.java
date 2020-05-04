package org.telegram.expensesbot.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.expensesbot.ExpensesCalculator;
import org.telegram.expensesbot.ExpensesParser;
import org.telegram.expensesbot.model.CategoryButton;
import org.telegram.expensesbot.service.CategoryButtonService;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Component
public class MainKeyboardController {
    private static final List<KeyboardRow> onlyHeaderdKeyboard = new ArrayList<>();
    private static final String CATEGORIES_CONTROL = "'Управление категориями'";
    private static final String SUMMARY = "Суммарно";
    private static int headerRowsNumber = 2;
    private static final Logger log = LoggerFactory.getLogger(MainKeyboardController.class);
    private final ExpensesCalculator expensesCalculator = new ExpensesCalculator();

    @Autowired
    private CategoryButtonService buttonService;

    private long chatId;

    private final Map<Long, List<KeyboardRow>> cache = new HashMap<>();

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public MainKeyboardController() {
        initHeader(onlyHeaderdKeyboard);
    }

    public List<KeyboardRow> addCategory(String name) {
        final int beginExpenses = 200;
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
        log.info("deleteCategory method: ->{}<- for chat: {}", buttonName, chatId);
        buttonService.deleteByCategoryAndChatId(category, chatId);

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

        return cache.get(chatId);
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
        keyboard.add(firstRow);
    }

    private void addSecondRow(List<KeyboardRow> keyboard) {
        KeyboardRow secondRow = new KeyboardRow();

        secondRow.add("'" + SUMMARY + " - 0'");
        keyboard.add(secondRow);
    }

    private void fillKeyboardFromDB(List<KeyboardRow> keyboard) {
        List<CategoryButton> keyboardButtons = buttonService.findByChatId(chatId);
        KeyboardController keyboardController = new KeyboardController(keyboard);

        keyboardButtons.forEach(keyboardButton -> {
            String category = keyboardButton.getCategory();
            int expenses = keyboardButton.getExpenses();
            String buttonName = combineButtonName(category, expenses);

            keyboardController.addButton(buttonName);
        });
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
        buttonService.updateExpenses(resultExpenses, chatId);
    }

    private void resetCacheExpenses(final int resultExpenses){
        List<KeyboardRow> cacheKeyboard = cache.get(chatId);

        for(int i = headerRowsNumber; i < cacheKeyboard.size(); i++) {
            cacheKeyboard.get(i).forEach(button -> {
                String category = ExpensesParser.parseCategoryName(button.getText());
                String buttonName = combineButtonName(category, resultExpenses);

                button.setText(buttonName);
            });
        }
    }

    /*==========================================================================================*/



    /*public void changeButtonExpenses(String category, String messageText) {
        if (expensesCalculator.isCategoryExists(category)) {
            int buttonIndex = expensesCalculator.findCategoryIndex(category);
            int rowIndex = expensesCalculator.computeRowIndex(buttonIndex);
            int columnIndex = expensesCalculator.computeColumnIndex(buttonIndex);
            int resultExpenses = expensesCalculator.computeResultExpenses(category, messageText);
            String headline = combineButtonName(category, resultExpenses);
            new StringBuilder("d").append("c");
            //System.out.println("row = " + rowIndex + ", col = " + columnIndex + " -> " + category);
            expensesCalculator.changeExpenses(category, resultExpenses);
            keyboard.get(rowIndex).get(columnIndex).setText(headline);

            headline = combineButtonName(SUMMARY, expensesCalculator.getSummaryExpenses());
            keyboard.get(1).get(0).setText(headline);
        }
    }*/

    public void changeButtonExpenses(String categoryMessage, String expensesMessage) {

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

    public List<KeyboardRow> getKeyboard() {
        return onlyHeaderdKeyboard;
    }
}

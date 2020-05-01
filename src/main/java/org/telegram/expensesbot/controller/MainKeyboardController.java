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
    private static final List<KeyboardRow> fieldKeyboard = new ArrayList<>();
    private static final String CATEGORIES_CONTROL = "'Управление категориями'";
    private static final String SUMMARY = "Суммарно";
    private static final Logger log = LoggerFactory.getLogger(MainKeyboardController.class);
    private final ExpensesCalculator expensesCalculator = new ExpensesCalculator();

    @Autowired
    private CategoryButtonService buttonService;

    private long chatId;

    private final Map<Long, List<KeyboardRow>> cache = new HashMap<>();

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public MainKeyboardController(){
        initHeader(fieldKeyboard);
    }

    public List<KeyboardRow> addCategory(String name) {
        final int beginExpenses = 0;
        buttonService.add(new CategoryButton(chatId, name, beginExpenses));

        if (cache.containsKey(chatId)) {
            KeyboardController keyboardController = new KeyboardController(cache.get(chatId));
            keyboardController.addButton(name);
            log.debug("add to cache for chat: {}", chatId);
            return cache.get(chatId);
        }

        return fillEmptyKeyboard();
    }

    public List<KeyboardRow> deleteCategory(String buttonName){
        String category = ExpensesParser.parseCategoryName(buttonName);

        buttonService.deleteByCategoryAndChatId(category, chatId);

        if (cache.containsKey(chatId)) {
            KeyboardController keyboardController = new KeyboardController(cache.get(chatId));
            keyboardController.deleteButton(buttonName);
            log.debug("delete from cache for chat: {}", chatId);
            return cache.get(chatId);
        }

        return fillEmptyKeyboard();
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

    /* public void resetExpenses() {
         int resultExpenses = 0;
         for (int i = 1; i < keyboard.size(); i++) {
             for (int j = 0; j < keyboard.get(i).size(); j++) {
                 String category = ExpensesParser.parseCategoryName(keyboard.get(i).get(j).getText());
                 String headline = combineButtonName(category, resultExpenses);

                 expensesCalculator.changeExpenses(category, resultExpenses);
                 keyboard.get(i).get(j).setText(headline);
             }
         }
     }
 */
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

    public String combineButtonName(String name, int expenses) {
        return String.format("'%s - %d'", name, expenses);
    }



    public List<KeyboardRow> getKeyboard() {
        return fieldKeyboard;
    }
}

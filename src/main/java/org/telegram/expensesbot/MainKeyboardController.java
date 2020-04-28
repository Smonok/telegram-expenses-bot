package org.telegram.expensesbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.expensesbot.model.MainKeyboard;
import org.telegram.expensesbot.repository.MainKeyboardRepository;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Service
public class MainKeyboardController {
    private static final int ROW_BUTTONS_NUMBER = 2;
    private static final int HEADER_ROWS_NUMBER = 2;
    private static final List<KeyboardRow> keyboard = new ArrayList<>();
    private static final String CATEGORIES_CONTROL = "'Управление категориями'";
    private static final String SUMMARY = "Суммарно";
    private final ExpensesCalculator expensesCalculator = new ExpensesCalculator();

    private long userId;
    private Map<Long, List<KeyboardRow>> cache = new HashMap<>();

    @Autowired
    private MainKeyboardRepository mainKeyboardRepository;

    public MainKeyboardController(long userId) {
        //super(keyboard, ROW_BUTTONS_NUMBER, HEADER_ROWS_NUMBER);
        this.userId = userId;
    }

    public MainKeyboardController() {
        //super(keyboard, ROW_BUTTONS_NUMBER, HEADER_ROWS_NUMBER);
    }

    public List<KeyboardRow> addCategory(String name) {
        //Initialization
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardController keyboardController = new KeyboardController(keyboard, ROW_BUTTONS_NUMBER, HEADER_ROWS_NUMBER);

        // Add new category to the DB
        final int beginExpenses = 0;
        if(name == null){
            name = "JOPA";
        }

       /* MainKeyboard main = new MainKeyboard(this.userId, name, beginExpenses);*/
        MainKeyboard main = new MainKeyboard();
        main.setUserId(this.userId);
        main.setCategory(name);
        main.setExpenses(beginExpenses);
        mainKeyboardRepository.save(main);

        // Fill Empty keyboard
        List<MainKeyboard> mainKeyboards = mainKeyboardRepository.findByUserId(this.userId);

        mainKeyboards.forEach(mainKeyboard -> {
            String category = mainKeyboard.getCategory();
            int expenses = mainKeyboard.getExpenses();
            String buttonName = combineButtonName(category, expenses);

            keyboardController.addButton(buttonName);
        });

        return keyboard;
    }

    private void initHeader(List<KeyboardRow> keyboard){
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

    public void resetExpenses() {
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

    public boolean isSummaryButton(String message) {
        if (message.charAt(0) == '\'') {
            String buttonName = ExpensesParser.parseCategoryName(message);
            return buttonName.equals(SUMMARY);
        }
        return false;
    }

    public boolean isCategoryButton(String message) {
        if (message.charAt(0) == '\'') {
            String buttonName = ExpensesParser.parseCategoryName(message);

            return expensesCalculator.isCategoryExists(buttonName);
        }
        return false;
    }

    public String combineButtonName(String name, int expenses) {
        return String.format("'%s - %d'", name, expenses);
    }

    public List<KeyboardRow> getKeyboard() {
        return keyboard;
    }
}

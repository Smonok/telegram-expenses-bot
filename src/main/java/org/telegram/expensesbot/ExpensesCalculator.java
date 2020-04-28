package org.telegram.expensesbot;

import java.util.*;

public class ExpensesCalculator {
    private static final Map<String, Integer> categoryExpenses = new LinkedHashMap<>();
    private int summaryExpenses = 0;

    public int computeResultExpenses(String category, String expensesMessage) {
        String[] lines = ExpensesParser.splitByNewLine(expensesMessage);
        int oldExpenses = categoryExpenses.get(category);
        int currentExpenses = 0;

        for (String line : lines) {
            currentExpenses += ExpensesParser.parseExpense(line);
        }

        summaryExpenses += currentExpenses;

        return oldExpenses + currentExpenses;
    }

    public void initCategory(String key) {
        categoryExpenses.put(key, 0);
    }

    public boolean isCategoryExists(String header) {
        return categoryExpenses.containsKey(header);
    }

    public void changeExpenses(String category, int newExpenses) {
        categoryExpenses.replace(category, newExpenses);
    }

    public boolean deleteExpenses(String category){
        categoryExpenses.remove(category);
        return true;
    }

    public int getSummaryExpenses(){
        return summaryExpenses;
    }
}

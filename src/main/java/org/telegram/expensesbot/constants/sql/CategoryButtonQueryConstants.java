package org.telegram.expensesbot.constants.sql;

public class CategoryButtonQueryConstants {
    public static final String UPDATE_EXPENSES_BY_CHAT_ID = "update CategoryButton button\n"
        + "set button.expenses = ?1 where button.chatId = ?2\n";

    public static final String UPDATE_EXPENSES_BY_CHAT_ID_AND_CATEGORY = "update CategoryButton button\n"
        + "set button.expenses = ?1 where button.category = ?2 and button.chatId = ?3\n";

    public static final String SELECT_EXPENSES_SUM_BY_CHAT_ID = "select sum(expenses) from CategoryButton where chatId = ?1";
}

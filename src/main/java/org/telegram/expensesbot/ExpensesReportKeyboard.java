package org.telegram.expensesbot;

import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class ExpensesReportKeyboard {
    private List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

    public ExpensesReportKeyboard(){
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        firstRow.add(addButton("Всё время", "allTime"));
        firstRow.add(addButton("Пол года", "halfYear"));

        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        secondRow.add(addButton("Месяц", "month"));
        secondRow.add(addButton("Неделя", "week"));

        keyboard.add(firstRow);
        keyboard.add(secondRow);
    }

    private InlineKeyboardButton addButton(String text, String callbackData){
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public List<List<InlineKeyboardButton>> getKeyboard(){
        return keyboard;
    }
}

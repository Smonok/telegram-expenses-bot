package org.telegram.expensesbot.keyboards;

import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class ExpensesReportKeyboard {

    public List<List<InlineKeyboardButton>> createTimeIntervalsKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        List<InlineKeyboardButton> secondRow = new ArrayList<>();

        firstRow.add(addButton("Всё время", "allTime"));
        firstRow.add(addButton("6 месяцев", "sixMonth"));
        secondRow.add(addButton("30 дней", "thirtyDays"));
        secondRow.add(addButton("7 дней", "sevenDays"));

        keyboard.add(firstRow);
        keyboard.add(secondRow);

        return keyboard;
    }

    public List<List<InlineKeyboardButton>> createReportFormatKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        List<InlineKeyboardButton> secondRow = new ArrayList<>();

        firstRow.add(addButton("Сообщение", "messageFormat"));
        firstRow.add(addButton("Файл", "fileFormat"));
        secondRow.add(addButton("Назад", "back"));

        keyboard.add(firstRow);
        keyboard.add(secondRow);

        return keyboard;
    }

    private InlineKeyboardButton addButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}

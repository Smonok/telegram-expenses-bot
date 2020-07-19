package org.telegram.expensesbot.keyboards;

import java.util.ArrayList;
import java.util.List;
import org.telegram.expensesbot.constants.callbackdata.ExpensesReportData;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class ExpensesReportKeyboard {

    public List<List<InlineKeyboardButton>> createTimeIntervalsKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        List<InlineKeyboardButton> secondRow = new ArrayList<>();

        firstRow.add(addButton("Всё время", ExpensesReportData.ALL_TIME));
        firstRow.add(addButton("6 месяцев", ExpensesReportData.SIX_MONTHS));
        secondRow.add(addButton("30 дней", ExpensesReportData.THIRTY_DAYS));
        secondRow.add(addButton("7 дней", ExpensesReportData.SEVEN_DAYS));

        keyboard.add(firstRow);
        keyboard.add(secondRow);

        return keyboard;
    }

    public List<List<InlineKeyboardButton>> createReportFormatKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        List<InlineKeyboardButton> secondRow = new ArrayList<>();

        firstRow.add(addButton("Сообщение\uD83D\uDCAC", ExpensesReportData.MESSAGE_FORMAT));
        firstRow.add(addButton("Файл\uD83D\uDCC4", ExpensesReportData.FILE_FORMAT));
        secondRow.add(addButton("Назад◀️", ExpensesReportData.BACK));

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

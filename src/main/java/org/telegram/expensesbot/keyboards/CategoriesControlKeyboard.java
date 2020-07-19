package org.telegram.expensesbot.keyboards;

import java.util.ArrayList;
import java.util.List;
import org.telegram.expensesbot.constants.callbackdata.CategoriesControlData;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class CategoriesControlKeyboard {
    private final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

    public CategoriesControlKeyboard() {
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        List<InlineKeyboardButton> thirdRow = new ArrayList<>();

        firstRow.add(addButton("Новая категория\uD83D\uDCDD", CategoriesControlData.NEW_CATEGORY));
        secondRow.add(addButton("Удалить категорию\uD83D\uDDD1", CategoriesControlData.DELETE_CATEGORY));
        thirdRow.add(addButton("Обнулить счета\uD83D\uDCB8", CategoriesControlData.RESET_BILLS));

        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboard.add(thirdRow);
    }

    private InlineKeyboardButton addButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public List<List<InlineKeyboardButton>> getKeyboard() {
        return keyboard;
    }
}

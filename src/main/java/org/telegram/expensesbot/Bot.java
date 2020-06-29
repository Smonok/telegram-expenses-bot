package org.telegram.expensesbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import org.telegram.expensesbot.controller.MainKeyboardController;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Bot extends TelegramLongPollingBot {
    private static final String CATEGORIES_CONTROL = "'Управление категориями'";
    private static final String START = "/start";
    private final ExpensesReportKeyboard expensesReportKeyboard = new ExpensesReportKeyboard();
    private final CategoriesControlKeyboard categoriesControlKeyboard = new CategoriesControlKeyboard();


    @Autowired
    private MainKeyboardController mainKeyboard;
    private String previousMessage = "";
    private Message message;
    String messageText = "";
    private String category = " ";

    @Override
    public void onUpdateReceived(Update update) {
        message = update.getMessage();

        if (update.hasMessage()) {
            if (message != null && message.hasText()) {
                messageText = message.getText();
                mainKeyboard.setChatId(message.getChatId());

                initStart();
                deleteButton();
                handleCategoriesControlButton();
                addButtonIfNewCategoryPrevious();
                handleCategoryButton();
                handleExpensesLines();
                //handleSummaryButton();  //For report

                previousMessage = messageText;
            }
        } else if (update.hasCallbackQuery()) {
            handleCategoriesControlKeyboard(update);
        }
    }

    private void initStart() {
        if (messageText.equals(START)) {
            sendReplyKeyboardMessage("Начинаем работать", mainKeyboard.removeUserExpenses());
        }
    }

    private void handleCategoriesControlButton() {
        if (messageText.equals(CATEGORIES_CONTROL)) {
            sendInlineKeyboardMessage("Выберите действие",
                categoriesControlKeyboard.getKeyboard());
        }
    }

    private void addButtonIfNewCategoryPrevious() {
        if (previousMessage.equals("Новая категория")) {
            if (messageText.charAt(0) != '\'') {
                if (mainKeyboard.isCategoryExists(messageText)) {
                    sendTextMessage("Извините, категория с таким\nназванием уже существует");
                } else {
                    sendReplyKeyboardMessage("Добавление успешно", mainKeyboard.addCategory(messageText));
                }
            } else {
                sendReplyKeyboardMessage("Недопустимый символ -> '", mainKeyboard.getOnlyHeadedKeyboard());
            }
        }
    }

    private void deleteButton() {
        if (previousMessage.equals("Удалить категорию")) {
            if (mainKeyboard.isCategoryButton(messageText)) {
                sendReplyKeyboardMessage("Удаление успешно", mainKeyboard.deleteCategory(messageText));
            }
        }
    }

    private void handleCategoryButton() {
        if (mainKeyboard.isCategoryButton(messageText)) {
            category = ExpensesParser.parseCategoryName(messageText);
            sendTextMessage("Для добавления расходов\nпришлите строку или строки\nв формате: сумма - название");
            sendInlineKeyboardMessage("Выберите период времени за который\nвы хотите получить отчёт",
                expensesReportKeyboard.getKeyboard());
        }
    }

    private void handleExpensesLines() {
        if (ExpensesParser.isExpensesLines(messageText)) {
            sendReplyKeyboardMessage("Изменено", mainKeyboard.changeButtonExpenses(category, messageText));
        }
    }

    private void handleSummaryButton() {
        if (mainKeyboard.isSummaryButton(messageText)) {
            sendInlineKeyboardMessage("Выберите период времени за который\n" +
                    "вы хотите получить отчёт по всем категориям",
                expensesReportKeyboard.getKeyboard());
        }
    }

    private void handleCategoriesControlKeyboard(Update update) {
        String buttonData = update.getCallbackQuery().getData();

        if (buttonData.equals("newCategory")) {
            sendTextMessageIfCallback("Пришлите имя категории\nНапример: Продукты", update);
            previousMessage = "Новая категория";
        } else if (buttonData.equals("deleteCategory")) {
            sendTextMessageIfCallback("Выберите категорию для удаления", update);
            previousMessage = "Удалить категорию";
        } else if (buttonData.equals("reset")) {
            sendKeyboardMessageIfCallback("Счета обнулены\nПоздравляем с новым периодом в жизни!",
                update, mainKeyboard.resetExpenses());
        }

        System.out.println(previousMessage);
    }

    /************* BOT Methods *****************/
    public void sendKeyboardMessageIfCallback(String text, Update update, List<KeyboardRow> keyboard) {
        SendMessage sendMessage = SendMessageFactory.initCallbackReplyKeyboardSendMessage(update, keyboard);

        try {
            execute(sendMessage.setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendTextMessageIfCallback(String text, Update update) {
        SendMessage sendMessage = SendMessageFactory.initCallbackSendMessage(update);

        try {
            execute(sendMessage.setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendInlineKeyboardMessage(String text, List<List<InlineKeyboardButton>> keyboard) {
        //List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.getKeyboard();
        SendMessage sendMessage = SendMessageFactory.initInlineKeyboardSendMessage(message, keyboard);

        try {
            execute(sendMessage.setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendTextMessage(String text) {
        SendMessage sendMessage = SendMessageFactory.initSendMessage(message);

        try {
            execute(sendMessage.setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendReplyKeyboardMessage(String text, List<KeyboardRow> keyboard) {
        SendMessage sendMessage = SendMessageFactory.initReplyKeyboardSendMessage(message, keyboard);

        try {
            execute(sendMessage.setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "e_x_p_enses_bot";
    }

    @Override
    public String getBotToken() {
        return "972039490:AAFf5aJUDInYnlVnlHQEwsMzSYTXr8sMZhY";
    }
}

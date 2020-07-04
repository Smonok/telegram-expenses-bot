package org.telegram.expensesbot;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import org.telegram.expensesbot.controller.MainKeyboardController;
import org.telegram.expensesbot.controller.ReportController;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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
    @Autowired
    private ReportController reportController = new ReportController();

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
                reportController.setChatId(message.getChatId());

                initStart();
                deleteButton();
                handleCategoriesControlButton();
                addButtonIfNewCategoryPrevious();
                handleCategoryButton();
                handleExpensesLines();
                handleSummaryButton();

                previousMessage = messageText;
            }
        } else if (update.hasCallbackQuery()) {
            handleCategoriesControlKeyboard(update);
            handleReportTimeKeyboard(update);
            handleReportFormatKeyboard(update);
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
            String text = "Для добавления расходов\nпришлите строку или строки\nв формате: сумма - название.\n"
                + "Или выберите период времени,\nдля получения отчёта";
            sendInlineKeyboardMessage(text, expensesReportKeyboard.createTimeIntervalsKeyboard());
        }
    }

    private void handleExpensesLines() {
        if (ExpensesParser.isExpensesLines(messageText)) {
            sendReplyKeyboardMessage("Изменено", mainKeyboard.changeButtonExpenses(category, messageText));
        }
    }

    private void handleSummaryButton() {
        if (mainKeyboard.isSummaryButton(messageText)) {
            sendInlineKeyboardMessage("Выберите период времени, за который\n" +
                    "вы хотите получить отчёт по всем категориям",
                expensesReportKeyboard.createTimeIntervalsKeyboard());
        }
    }

    private void handleCategoriesControlKeyboard(Update update) {
        String buttonData = update.getCallbackQuery().getData();

        if (buttonData.equals("newCategory")) {
            sendTextMessageIfCallback("Пришлите имя категории", update);
            previousMessage = "Новая категория";
        } else if (buttonData.equals("deleteCategory")) {
            sendTextMessageIfCallback("Выберите категорию для удаления", update);
            previousMessage = "Удалить категорию";
        } else if (buttonData.equals("reset")) {
            sendKeyboardMessageIfCallback("Счета обнулены\nПоздравляем с новым периодом в жизни!",
                update, mainKeyboard.resetExpenses());
        }
    }

    private void handleReportTimeKeyboard(Update update) {
        String buttonData = update.getCallbackQuery().getData();
        //TODO: Отдельно для суммарно, отдельно для каждой категории
        if (buttonData.equals("allTime")) {
            changeToReportFormatKeyboard(update);
            previousMessage = "Всё время";
        } else if (buttonData.equals("halfYear")) {
            changeToReportFormatKeyboard(update);
            previousMessage = "Пол года";
        } else if (buttonData.equals("month")) {
            changeToReportFormatKeyboard(update);
            previousMessage = "Месяц";
        } else if (buttonData.equals("week")) {
            changeToReportFormatKeyboard(update);
            previousMessage = "Неделя";
        }
    }

    private void changeToReportFormatKeyboard(Update update) {
        String text = "Выберете формат отчёта";
        List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createReportFormatKeyboard();
        changeInlineKeyboardMessage(update, keyboard, text);
    }

    private void handleReportFormatKeyboard(Update update) {
        String buttonData = update.getCallbackQuery().getData();

        if (buttonData.equals("messageFormat")) {
            sendTextMessageIfCallback(reportController.createAllTimeReportMessage(), update);
            previousMessage = "Сообщение";
        } else if (buttonData.equals("fileFormat")) {
            File document = null;
            //TODO: Вариации отчётов
            switch (previousMessage) {
                case "Всё время":
                    document = reportController.createAllTimeFileReport();
                    break;
            }

            String text = "Файл с отчётом";
            sendDocument(text, update, document);
            previousMessage = "Файл";
        } else if (buttonData.equals("back")) {
            String text = "Выберите период времени, за который\n" +
                "вы хотите получить отчёт по всем категориям";
            List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createTimeIntervalsKeyboard();
            changeInlineKeyboardMessage(update, keyboard, text);
            previousMessage = "Назад";
        }
    }

    public void sendKeyboardMessageIfCallback(String text, Update update, List<KeyboardRow> keyboard) {
        SendMessage sendMessage = MessageFactory.initCallbackReplyKeyboardSendMessage(update, keyboard);

        try {
            execute(sendMessage.setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendTextMessageIfCallback(String text, Update update) {
        SendMessage sendMessage = MessageFactory.initCallbackSendMessage(update);

        try {
            execute(sendMessage.setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendInlineKeyboardMessage(String text, List<List<InlineKeyboardButton>> keyboard) {
        SendMessage sendMessage = MessageFactory.initInlineKeyboardSendMessage(message, keyboard);

        try {
            execute(sendMessage.setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendTextMessage(String text) {
        SendMessage sendMessage = MessageFactory.initSendMessage(message);

        try {
            execute(sendMessage.setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendReplyKeyboardMessage(String text, List<KeyboardRow> keyboard) {
        SendMessage sendMessage = MessageFactory.initReplyKeyboardSendMessage(message, keyboard);

        try {
            execute(sendMessage.setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendDocument(String text, Update update, File document) {
        SendDocument sendDocument = MessageFactory.initCallbackSendDocument(update, text);

        try {
            execute(sendDocument.setDocument(document));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void changeInlineKeyboardMessage(Update update, List<List<InlineKeyboardButton>> keyboard, String text) {
        EditMessageText editedMessage = MessageFactory.initEditMessageText(update, keyboard);

        try {
            sendApiMethod(editedMessage.setText(text));
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

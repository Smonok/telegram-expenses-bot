package org.telegram.expensesbot;

import java.io.File;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import org.telegram.expensesbot.controller.MainKeyboardController;
import org.telegram.expensesbot.controller.ReportController;
import org.telegram.expensesbot.factory.MessageFactory;
import org.telegram.expensesbot.keyboards.CategoriesControlKeyboard;
import org.telegram.expensesbot.keyboards.ExpensesReportKeyboard;
import org.telegram.expensesbot.util.ExpensesParserUtil;
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
    private static final String NEW_CATEGORY = "Новая категория";
    private static final String START = "/start";
    private static final String SUMMARY = "Суммарно";
    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    private final ExpensesReportKeyboard expensesReportKeyboard = new ExpensesReportKeyboard();
    private final CategoriesControlKeyboard categoriesControlKeyboard = new CategoriesControlKeyboard();

    @Autowired
    private MainKeyboardController mainKeyboard;
    @Autowired
    private final ReportController reportController = new ReportController();

    private String previousMessage = "";
    private String timeInterval = "";
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
        if (previousMessage.equals(NEW_CATEGORY)) {
            if (messageText.charAt(0) != '\'') {
                if (mainKeyboard.isCategoryExists(messageText)) {
                    sendTextMessage("Извините, категория с таким\nназванием уже существует");
                } else {
                    sendReplyKeyboardMessage("Добавление успешно", mainKeyboard.addCategory(messageText));
                }
            } else {
                sendReplyKeyboardMessage("Недопустимый символ -> '", mainKeyboard.fillEmptyKeyboard());
            }
        }
    }

    private void deleteButton() {
        if (previousMessage.equals("Удалить категорию") && mainKeyboard.isCategoryButton(messageText)) {
            sendReplyKeyboardMessage("Удаление успешно", mainKeyboard.deleteCategory(messageText));
        }
    }

    private void handleCategoryButton() {
        if (mainKeyboard.isCategoryButton(messageText)) {
            category = ExpensesParserUtil.parseCategoryName(messageText);
            String text = "Для добавления расходов\nпришлите строку или строки\nв формате: сумма - название.\n"
                + "Или выберите период времени,\nдля получения отчёта";
            sendInlineKeyboardMessage(text, expensesReportKeyboard.createTimeIntervalsKeyboard());
        }
    }

    private void handleExpensesLines() {
        if (ExpensesParserUtil.isExpensesLines(messageText) && !previousMessage.equals(NEW_CATEGORY)) {
            sendReplyKeyboardMessage("Изменено", mainKeyboard.changeButtonExpenses(category, messageText));
        }
    }

    private void handleSummaryButton() {
        if (mainKeyboard.isSummaryButton(messageText)) {
            category = SUMMARY;
            sendInlineKeyboardMessage("Выберите период времени, за который\n" +
                    "вы хотите получить отчёт по всем категориям",
                expensesReportKeyboard.createTimeIntervalsKeyboard());
        }
    }

    private void handleCategoriesControlKeyboard(Update update) {
        String buttonData = update.getCallbackQuery().getData();

        if (buttonData.equals("newCategory")) {
            sendTextMessageIfCallback("Пришлите имя категории", update);
            previousMessage = NEW_CATEGORY;
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

        if (buttonData.equals("allTime") || buttonData.equals("sixMonth")
            || buttonData.equals("month") || buttonData.equals("sevenDays")) {
            changeToReportFormatKeyboard(update);
            timeInterval = buttonData;
        }
    }

    private void changeToReportFormatKeyboard(Update update) {
        String text = "Выберете формат отчёта";
        List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createReportFormatKeyboard();
        changeInlineKeyboardMessage(update, keyboard, text);
    }

    private void handleReportFormatKeyboard(Update update) {
        String buttonData = update.getCallbackQuery().getData();

        if (buttonData.equals("messageFormat") && !StringUtils.isBlank(timeInterval)) {
            String reportMessage = "error";

            if (timeInterval.equals("allTime")) {
                reportMessage = reportController.createAllTimeReportMessage(category);
            } else if (timeInterval.equals("sixMonth")) {
                reportMessage = reportController.createHalfYearReportMessage(category);
            } else if (timeInterval.equals("month")) {
                reportMessage = reportController.createMonthReportMessage(category);
            } else if (timeInterval.equals("sevenDays")) {
                reportMessage = reportController.createSevenDaysReportMessage(category);
            }

            sendTextMessageIfCallback(reportMessage, update);
            previousMessage = "Сообщение";
        } else if (buttonData.equals("fileFormat")) {
            File document = null;
            //TODO: After messages
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
        if (!StringUtils.isBlank(text) && update != null && keyboard != null) {
            SendMessage sendMessage = MessageFactory.initCallbackReplyKeyboardSendMessage(update, keyboard);

            try {
                execute(sendMessage.setText(text));
            } catch (TelegramApiException e) {
                log.error("Cannot send message after callback with keyboard: {}", keyboard.toString(), e);
            }
        }
    }

    public void sendTextMessageIfCallback(String text, Update update) {
        if (!StringUtils.isBlank(text) && update != null) {
            SendMessage sendMessage = MessageFactory.initCallbackSendMessage(update);

            try {
                execute(sendMessage.setText(text));
            } catch (TelegramApiException e) {
                log.error("Cannot send message after callback", e);
            }
        }
    }

    public void sendInlineKeyboardMessage(String text, List<List<InlineKeyboardButton>> keyboard) {
        if (!StringUtils.isBlank(text) && keyboard != null) {
            SendMessage sendMessage = MessageFactory.initInlineKeyboardSendMessage(message, keyboard);

            try {
                execute(sendMessage.setText(text));
            } catch (TelegramApiException e) {
                log.error("Cannot send message with inline keyboard: {}", keyboard.toString(), e);
            }
        }
    }

    private void sendTextMessage(String text) {
        if (!StringUtils.isBlank(text)) {
            SendMessage sendMessage = MessageFactory.initSendMessage(message);

            try {
                execute(sendMessage.setText(text));
            } catch (TelegramApiException e) {
                log.error("Cannot send text message", e);
            }
        }
    }

    private void sendReplyKeyboardMessage(String text, List<KeyboardRow> keyboard) {
        if (!StringUtils.isBlank(text) && keyboard != null) {
            SendMessage sendMessage = MessageFactory.initReplyKeyboardSendMessage(message, keyboard);

            try {
                execute(sendMessage.setText(text));
            } catch (TelegramApiException e) {
                log.error("Cannot send reply message with keyboard: {}", keyboard.toString(), e);
            }
        }
    }

    private void sendDocument(String text, Update update, File document) {
        if (!StringUtils.isBlank(text) && update != null && document != null) {
            SendDocument sendDocument = MessageFactory.initCallbackSendDocument(update, text);

            try {
                execute(sendDocument.setDocument(document));
            } catch (TelegramApiException e) {
                log.error("Cannot send document: {}", document.toString(), e);
            }
        }
    }

    private void changeInlineKeyboardMessage(Update update, List<List<InlineKeyboardButton>> keyboard, String text) {
        if (!StringUtils.isBlank(text) && update != null && keyboard != null) {
            EditMessageText editedMessage = MessageFactory.initEditMessageText(update, keyboard);

            try {
                sendApiMethod(editedMessage.setText(text));
            } catch (TelegramApiException e) {
                log.error("Cannot change message with inline keyboard", e);
            }
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

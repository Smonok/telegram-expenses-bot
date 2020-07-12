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
    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    private final ExpensesReportKeyboard expensesReportKeyboard = new ExpensesReportKeyboard();
    private final CategoriesControlKeyboard categoriesControlKeyboard = new CategoriesControlKeyboard();
    private final Map<Long, String> chatIdPreviousMessage = new HashMap<>();
    private final Map<Long, String> chatIdTimeInterval = new HashMap<>();
    private Message message;
    private Long chatId = 0L;
    String messageText = "";
    private String category = " ";

    @Autowired
    private MainKeyboardController mainKeyboard;
    @Autowired
    private ReportController reportController;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            message = update.getMessage();
            if (message != null && message.hasText()) {
                messageText = message.getText();
                chatId = message.getChatId();
                mainKeyboard.setChatId(chatId);

                initStart();
                deleteButton();
                handleCategoriesControlButton();
                addButtonIfNewCategoryPrevious();
                handleCategoryButton();
                handleExpensesLines();
                handleSummaryButton();

                chatIdPreviousMessage.put(chatId, messageText);
            }
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();

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
        if (chatIdPreviousMessage.get(chatId) != null &&
            chatIdPreviousMessage.get(chatId).equals(NEW_CATEGORY)) {
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
        if (chatIdPreviousMessage.get(chatId) != null &&
            chatIdPreviousMessage.get(chatId).equals("Удалить категорию") &&
            mainKeyboard.isCategoryButton(messageText)) {
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
        if (ExpensesParserUtil.isExpensesLines(messageText) &&
            chatIdPreviousMessage.get(chatId) != null &&
            !chatIdPreviousMessage.get(chatId).equals(NEW_CATEGORY)) {
            sendReplyKeyboardMessage("Изменено", mainKeyboard.changeButtonExpenses(category, messageText));
        }
    }

    private void handleSummaryButton() {
        if (mainKeyboard.isSummaryButton(messageText)) {
            category = "Суммарно";
            sendInlineKeyboardMessage("Выберите период времени, за который\n" +
                    "вы хотите получить отчёт по всем категориям",
                expensesReportKeyboard.createTimeIntervalsKeyboard());
        }
    }

    private void handleCategoriesControlKeyboard(Update update) {
        String buttonData = update.getCallbackQuery().getData();

        if (buttonData.equals("newCategory")) {
            sendTextMessageIfCallback("Пришлите имя категории", update);
            chatIdPreviousMessage.put(chatId, NEW_CATEGORY);
        } else if (buttonData.equals("deleteCategory")) {
            sendTextMessageIfCallback("Выберите категорию для удаления", update);
            chatIdPreviousMessage.put(chatId, "Удалить категорию");
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
            chatIdTimeInterval.put(chatId, buttonData);
        }
    }

    private void changeToReportFormatKeyboard(Update update) {
        String text = "Выберете формат отчёта";
        List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createReportFormatKeyboard();
        changeInlineKeyboardMessage(update, keyboard, text);
    }

    private void handleReportFormatKeyboard(Update update) {
        String buttonData = update.getCallbackQuery().getData();
        reportController.setChatId(chatId);

        if (buttonData.equals("messageFormat") && !StringUtils.isBlank(chatIdTimeInterval.get(chatId))) {
            String reportMessage = createReportMessage();

            sendTextMessageIfCallback(reportMessage, update);
            chatIdPreviousMessage.put(chatId, "Сообщение");
        } else if (buttonData.equals("fileFormat")) {
            File document = null;
            //TODO: After messages
            String text = "Файл с отчётом";
            sendDocument(text, update, document);
            chatIdPreviousMessage.put(chatId, "Файл");
        } else if (buttonData.equals("back")) {
            String text = "Выберите период времени, за который\n" +
                "вы хотите получить отчёт по всем категориям";
            List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createTimeIntervalsKeyboard();
            changeInlineKeyboardMessage(update, keyboard, text);
            chatIdPreviousMessage.put(chatId, "Назад");
        }
    }

    private String createReportMessage() {
        switch (chatIdTimeInterval.get(chatId)) {
            case "allTime":
                return reportController.createAllTimeReportMessage(category);
            case "sixMonth":
                return reportController.createHalfYearReportMessage(category);
            case "month":
                return reportController.createMonthReportMessage(category);
            case "sevenDays":
                return reportController.createSevenDaysReportMessage(category);
            default:
                return "error";
        }
    }

    private void sendKeyboardMessageIfCallback(String text, Update update, List<KeyboardRow> keyboard) {
        if (!StringUtils.isBlank(text) && update != null && keyboard != null) {
            SendMessage sendMessage = MessageFactory.initCallbackReplyKeyboardSendMessage(update, keyboard);

            try {
                execute(sendMessage.setText(text));
            } catch (TelegramApiException e) {
                log.error("Cannot send message after callback with keyboard: {}", keyboard.toString(), e);
            }
        }
    }

    private void sendTextMessageIfCallback(String text, Update update) {
        if (!StringUtils.isBlank(text) && update != null) {
            SendMessage sendMessage = MessageFactory.initCallbackSendMessage(update);

            try {
                execute(sendMessage.setText(text));
            } catch (TelegramApiException e) {
                log.error("Cannot send message after callback", e);
            }
        }
    }

    private void sendInlineKeyboardMessage(String text, List<List<InlineKeyboardButton>> keyboard) {
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

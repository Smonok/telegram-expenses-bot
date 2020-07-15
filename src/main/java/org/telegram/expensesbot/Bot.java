package org.telegram.expensesbot;

import java.io.File;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import org.telegram.expensesbot.constants.BotCommands;
import org.telegram.expensesbot.constants.callbackdata.CategoriesControlData;
import org.telegram.expensesbot.constants.callbackdata.ExpensesReportData;
import org.telegram.expensesbot.controller.FileReportController;
import org.telegram.expensesbot.controller.MainKeyboardController;
import org.telegram.expensesbot.controller.MessageReportController;
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
    private MessageReportController messageReportController;
    @Autowired
    private FileReportController fileReportController;

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
        if (messageText.equals(BotCommands.START_COMMAND)) {
            sendReplyKeyboardMessage("Начинаем работать", mainKeyboard.removeUserExpenses());
        }
    }

    private void handleCategoriesControlButton() {
        if (messageText.equals(BotCommands.CATEGORIES_CONTROL_BUTTON)) {
            sendInlineKeyboardMessage("Выберите действие",
                categoriesControlKeyboard.getKeyboard());
        }
    }

    private void addButtonIfNewCategoryPrevious() {
        if (chatIdPreviousMessage.get(chatId) != null &&
            chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.NEW_CATEGORY)) {
            if (mainKeyboard.isSuitableForAdding(messageText)) {
                sendReplyKeyboardMessage("Добавление успешно", mainKeyboard.addCategory(messageText));
            } else {
                sendTextMessage("Недопустимое имя категории");
            }
        }
    }

    private void deleteButton() {
        if (chatIdPreviousMessage.get(chatId) != null &&
            chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.DELETE_CATEGORY) &&
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
        if (!StringUtils.isBlank(chatIdPreviousMessage.get(chatId)) &&
            !chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.NEW_CATEGORY) &&
            !chatIdPreviousMessage.get(chatId).equals("Удалить категорию")) {
            if (ExpensesParserUtil.isExpensesLines(messageText)) {
                if (ExpensesParserUtil.isTooBigExpenses(messageText)) {
                    sendTextMessage("Расходы больше, чем 999999999\nне были добавлены");
                }
                sendReplyKeyboardMessage("Изменено", mainKeyboard.changeButtonExpenses(category, messageText));
            } else {
                sendErrorExpensesLinesMessage();
            }
        }
    }

    private void sendErrorExpensesLinesMessage() {
        if (!StringUtils.isBlank(chatIdPreviousMessage.get(chatId)) &&
            !chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.NEW_CATEGORY) &&
            !mainKeyboard.isKeyboardButton(messageText)) {
            String errorMessage = "";
            if (ExpensesParserUtil.countSeparators(messageText) == 0) {
                errorMessage = "Отсутствует разделитель: '-'";
            } else if (ExpensesParserUtil.isExpensesNonNaturalNumber(messageText)) {
                errorMessage = "Недопустимы значения расходов, меньше чем 1";
            } else if (ExpensesParserUtil.countSeparators(messageText) > 1) {
                errorMessage = "Недопустимо больше одного разделителя";
            }

            sendTextMessage("Неверный формат\n" + errorMessage);
        }
    }

    private void handleSummaryButton() {
        if (!StringUtils.isBlank(chatIdPreviousMessage.get(chatId)) &&
            !chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.NEW_CATEGORY) &&
            mainKeyboard.isSummaryButton(messageText)) {
            category = "Суммарно";
            sendInlineKeyboardMessage("Выберите период времени, за который\n" +
                    "вы хотите получить отчёт по всем категориям",
                expensesReportKeyboard.createTimeIntervalsKeyboard());
        }
    }

    private void handleCategoriesControlKeyboard(Update update) {
        String buttonData = update.getCallbackQuery().getData();

        switch (buttonData) {
            case CategoriesControlData.NEW_CATEGORY:
                sendTextMessageIfCallback("Пришлите имя категории", update);
                chatIdPreviousMessage.put(chatId, buttonData);
                break;
            case CategoriesControlData.DELETE_CATEGORY:
                sendTextMessageIfCallback("Выберите категорию для удаления", update);
                chatIdPreviousMessage.put(chatId, buttonData);
                break;
            case CategoriesControlData.RESET_BILLS:
                sendKeyboardMessageIfCallback("Счета обнулены\nПоздравляем с новым периодом в жизни!",
                    update, mainKeyboard.resetExpenses());
                chatIdPreviousMessage.put(chatId, buttonData);
                break;
        }
    }

    private void handleReportTimeKeyboard(Update update) {
        String buttonData = update.getCallbackQuery().getData();

        if (buttonData.equals(ExpensesReportData.ALL_TIME) || buttonData.equals(ExpensesReportData.SIX_MONTHS)
            || buttonData.equals(ExpensesReportData.THIRTY_DAYS) || buttonData.equals(ExpensesReportData.SEVEN_DAYS)) {
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
        messageReportController.setChatId(chatId);
        fileReportController.setChatId(chatId);

        if (buttonData.equals(ExpensesReportData.MESSAGE_FORMAT) &&
            !StringUtils.isBlank(chatIdTimeInterval.get(chatId))) {
            String reportMessage = createReportMessage();

            sendTextMessageIfCallback(reportMessage, update);
        } else if (buttonData.equals(ExpensesReportData.FILE_FORMAT)) {
            File document = createReportFile();
            String text = "Файл с отчётом";

            sendDocument(text, update, document);
        } else if (buttonData.equals("back")) {
            String text = "Выберите период времени, за который\n" +
                "вы хотите получить отчёт по всем категориям";
            List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createTimeIntervalsKeyboard();

            changeInlineKeyboardMessage(update, keyboard, text);
        }
    }

    private String createReportMessage() {
        switch (chatIdTimeInterval.get(chatId)) {
            case ExpensesReportData.ALL_TIME:
                return messageReportController.createAllTimeReportMessage(category);
            case ExpensesReportData.SIX_MONTHS:
                return messageReportController.createSixMonthsReportMessage(category);
            case ExpensesReportData.THIRTY_DAYS:
                return messageReportController.createThirtyDaysReportMessage(category);
            case ExpensesReportData.SEVEN_DAYS:
                return messageReportController.createSevenDaysReportMessage(category);
            default:
                return "error";
        }
    }

    private File createReportFile() {
        switch (chatIdTimeInterval.get(chatId)) {
            case ExpensesReportData.ALL_TIME:
                return fileReportController.createAllTimeFileReport(category);
            case ExpensesReportData.SIX_MONTHS:
                return fileReportController.createSixMonthsFileReport(category);
            case ExpensesReportData.THIRTY_DAYS:
                return fileReportController.createThirtyDaysFileReport(category);
            case ExpensesReportData.SEVEN_DAYS:
                return fileReportController.createSevenDaysFileReport(category);
            default:
                return null;
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

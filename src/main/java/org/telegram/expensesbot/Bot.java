package org.telegram.expensesbot;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.expensesbot.constants.BotCommandConstants;
import org.telegram.expensesbot.constants.BotResponseConstants;
import org.telegram.expensesbot.constants.callbackdata.CategoriesControlData;
import org.telegram.expensesbot.constants.callbackdata.ExpensesReportData;
import org.telegram.expensesbot.controller.FileReportController;
import org.telegram.expensesbot.controller.MainKeyboardController;
import org.telegram.expensesbot.controller.MessageReportController;
import org.telegram.expensesbot.factory.MessageFactory;
import org.telegram.expensesbot.factory.PropertiesFactory;
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
    private final Map<Long, String> chatIdCategory = new HashMap<>();
    private Message message;
    private Long chatId = 0L;
    String messageText = "";


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
                handleHelpButton();
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
        if (messageText.equals(BotCommandConstants.START_COMMAND)) {
            sendReplyKeyboardMessage(BotResponseConstants.START_WORK, mainKeyboard.removeUserExpenses());
        }
    }

    private void handleCategoriesControlButton() {
        if (messageText.equals(BotCommandConstants.CATEGORIES_CONTROL_BUTTON)) {
            sendInlineKeyboardMessage(BotResponseConstants.CHOOSE_ACTION,
                categoriesControlKeyboard.getKeyboard());
        }
    }

    private void handleHelpButton() {
        if (messageText.equals(BotCommandConstants.HELP_BUTTON)) {
            sendTextMessage(BotResponseConstants.HELP_INFO);
        }
    }

    private void addButtonIfNewCategoryPrevious() {
        if (chatIdPreviousMessage.get(chatId) != null &&
            chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.NEW_CATEGORY)) {
            if (mainKeyboard.isSuitableForAdding(messageText)) {
                sendReplyKeyboardMessage(BotResponseConstants.ADD_SUCCESSFUL, mainKeyboard.addCategory(messageText));
            } else {
                sendTextMessage(BotResponseConstants.WRONG_CATEGORY_NAME_ERROR);
            }
        }
    }

    private void deleteButton() {
        if (chatIdPreviousMessage.get(chatId) != null &&
            chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.DELETE_CATEGORY) &&
            mainKeyboard.isCategoryButton(messageText)) {
            sendReplyKeyboardMessage(BotResponseConstants.DELETE_SUCCESSFUL, mainKeyboard.deleteCategory(messageText));
        }
    }

    private void handleCategoryButton() {
        if (mainKeyboard.isCategoryButton(messageText)) {
            chatIdCategory.put(chatId, ExpensesParserUtil.parseCategoryName(messageText));
            sendInlineKeyboardMessage(BotResponseConstants.ADD_EXPENSES_OR_GET_REPORT_INFO,
                expensesReportKeyboard.createTimeIntervalsKeyboard());
        }
    }

    private void handleExpensesLines() {
        if (!StringUtils.isBlank(chatIdPreviousMessage.get(chatId)) &&
            mainKeyboard.isCategoryButton(chatIdPreviousMessage.get(chatId))) {
            if (ExpensesParserUtil.isExpensesLines(messageText)) {
                if (ExpensesParserUtil.isTooBigExpenses(messageText)) {
                    sendTextMessage(BotResponseConstants.TOO_BIG_EXPENSES_WARNING);
                }
                sendReplyKeyboardMessage(BotResponseConstants.CHANGED,
                    mainKeyboard.changeButtonExpenses(chatIdCategory.get(chatId), messageText));
            } else if (!mainKeyboard.isKeyboardButton(messageText)) {
                sendErrorExpensesLinesMessage();
            }
        }
    }

    private void sendErrorExpensesLinesMessage() {
        String errorMessage = "";
        if (ExpensesParserUtil.countSeparators(messageText) == 0) {
            errorMessage = BotResponseConstants.NO_SEPARATOR_ERROR;
        } else if (ExpensesParserUtil.isExpensesNonNaturalNumber(messageText)) {
            errorMessage = BotResponseConstants.NEGATIVE_EXPENSES_ERROR;
        } else if (ExpensesParserUtil.countSeparators(messageText) > 1) {
            errorMessage = BotResponseConstants.TOO_MANY_SEPARATORS_ERROR;
        }

        sendTextMessage(BotResponseConstants.WRONG_FORMAT_ERROR + errorMessage);
    }

    private void handleSummaryButton() {
        if (!StringUtils.isBlank(chatIdPreviousMessage.get(chatId)) &&
            !chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.NEW_CATEGORY) &&
            mainKeyboard.isSummaryButton(messageText)) {
            chatIdCategory.put(chatId, BotCommandConstants.SUMMARY_BUTTON);
            sendInlineKeyboardMessage(BotResponseConstants.SUMMARY_TIME_PERIOD_INFO,
                expensesReportKeyboard.createTimeIntervalsKeyboard());
        }
    }

    private void handleCategoriesControlKeyboard(Update update) {
        String buttonData = update.getCallbackQuery().getData();

        switch (buttonData) {
            case CategoriesControlData.NEW_CATEGORY:
                sendTextMessageIfCallback(BotResponseConstants.SEND_CATEGORY_NAME, update);
                chatIdPreviousMessage.put(chatId, buttonData);
                break;
            case CategoriesControlData.DELETE_CATEGORY:
                sendTextMessageIfCallback(BotResponseConstants.CHOOSE_CATEGORY_TO_DELETE, update);
                chatIdPreviousMessage.put(chatId, buttonData);
                break;
            case CategoriesControlData.RESET_BILLS:
                sendKeyboardMessageIfCallback(BotResponseConstants.BILLS_RESET, update, mainKeyboard.resetExpenses());
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
        List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createReportFormatKeyboard();
        changeInlineKeyboardMessage(update, keyboard, BotResponseConstants.CHOOSE_REPORT_FORMAT);
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
            sendTextMessage(BotResponseConstants.CREATING_FILE);

            File document = createReportFile();
            sendDocument(BotResponseConstants.REPORT_FILE, update, document);
        } else if (buttonData.equals("back")) {
            List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createTimeIntervalsKeyboard();

            if (chatIdCategory.get(chatId).equals(BotCommandConstants.SUMMARY_BUTTON)) {
                changeInlineKeyboardMessage(update, keyboard, BotResponseConstants.SUMMARY_TIME_PERIOD_INFO);
            } else {
                changeInlineKeyboardMessage(update, keyboard, BotResponseConstants.ADD_EXPENSES_OR_GET_REPORT_INFO);
            }
        }
    }

    private String createReportMessage() {
        String category = chatIdCategory.get(chatId);
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
        String category = chatIdCategory.get(chatId);
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
        return PropertiesFactory.getProperty("config.properties", "username");
    }

    @Override
    public String getBotToken() {
        return PropertiesFactory.getProperty("config.properties", "token");
    }
}

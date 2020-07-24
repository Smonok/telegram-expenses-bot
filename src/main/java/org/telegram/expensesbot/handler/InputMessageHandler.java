package org.telegram.expensesbot.handler;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.expensesbot.Bot;
import org.telegram.expensesbot.constants.BotCommandConstants;
import org.telegram.expensesbot.constants.BotResponseConstants;
import org.telegram.expensesbot.constants.callbackdata.CategoriesControlData;
import org.telegram.expensesbot.constants.callbackdata.ExpensesReportData;
import org.telegram.expensesbot.controller.FileReportController;
import org.telegram.expensesbot.controller.MainKeyboardController;
import org.telegram.expensesbot.controller.MessageReportController;
import org.telegram.expensesbot.factory.MessageFactory;
import org.telegram.expensesbot.keyboards.CategoriesControlKeyboard;
import org.telegram.expensesbot.keyboards.ExpensesReportKeyboard;
import org.telegram.expensesbot.util.ExpensesParserUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
public class InputMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(InputMessageHandler.class);
    private final ExpensesReportKeyboard expensesReportKeyboard = new ExpensesReportKeyboard();
    private final CategoriesControlKeyboard categoriesControlKeyboard = new CategoriesControlKeyboard();
    private final MainKeyboardController mainKeyboardController;
    private final MessageReportController messageReportController;
    private final FileReportController fileReportController;
    private final Bot telegramBot;
    private final Map<Long, String> chatIdPreviousMessage = new HashMap<>();
    private final Map<Long, String> chatIdTimeInterval = new HashMap<>();
    private final Map<Long, String> chatIdCategory = new HashMap<>();
    private Message message;
    private Long chatId = 0L;
    private String messageText = "";

    @Autowired
    public InputMessageHandler(MainKeyboardController mainKeyboardController,
        MessageReportController messageReportController,
        FileReportController fileReportController, Bot telegramBot) {
        this.mainKeyboardController = mainKeyboardController;
        this.messageReportController = messageReportController;
        this.fileReportController = fileReportController;
        this.telegramBot = telegramBot;
    }

    public SendMessage handleInput(Update update) {
        if (update.hasMessage()) {
            message = update.getMessage();
            if (message != null && message.hasText()) {
                messageText = message.getText();
                chatId = message.getChatId();
                mainKeyboardController.setChatId(chatId);
                initChatIdPreviousMessage();

                return sendReplyToMessage();
            }
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            mainKeyboardController.setChatId(chatId);
            messageReportController.setChatId(chatId);
            fileReportController.setChatId(chatId);
            initChatIdPreviousMessage();

            return sendReplyToCallback(update);
        }

        return new SendMessage();
    }

    private void initChatIdPreviousMessage() {
        if (!chatIdPreviousMessage.containsKey(chatId)) {
            chatIdPreviousMessage.put(chatId, chatId.toString());
        }
    }

    private SendMessage sendReplyToMessage() {
        SendMessage resultMessage = handleMessageText();
        if (resultMessage != null) {
            return resultMessage;
        }

        resultMessage = handleCategoryButtonsControl();

        return resultMessage == null ?
            MessageFactory.initSendMessage(message, BotResponseConstants.UNKNOWN_COMMAND) : resultMessage;
    }

    private SendMessage handleMessageText() {
        switch (messageText) {
            case BotCommandConstants.START_COMMAND:
                log.info("/start in chat: {}", chatId);
                chatIdPreviousMessage.put(chatId, messageText);
                return MessageFactory.initReplyKeyboardSendMessage(message, mainKeyboardController.removeUserExpenses(),
                    BotResponseConstants.START_WORK);
            case BotCommandConstants.CATEGORIES_CONTROL_BUTTON:
                log.info("Categories control button in chat: {}", chatId);
                chatIdPreviousMessage.put(chatId, messageText);
                return MessageFactory.initInlineKeyboardSendMessage(message,
                    categoriesControlKeyboard.getKeyboard(), BotResponseConstants.CHOOSE_ACTION);
            case BotCommandConstants.HELP_BUTTON:
                log.info("Help control button in chat: {}", chatId);
                chatIdPreviousMessage.put(chatId, messageText);
                return MessageFactory.initSendMessage(message, BotResponseConstants.HELP_INFO);
            case BotCommandConstants.ID_COMMAND:
                log.info("/id in chat: {}", chatId);
                chatIdPreviousMessage.put(chatId, messageText);
                return MessageFactory.initSendMessage(message, BotResponseConstants.YOUR_ID + chatId);
            default:
                return null;
        }
    }

    private SendMessage handleCategoryButtonsControl() {
        if (chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.DELETE_CATEGORY) &&
            mainKeyboardController.isCategoryButton(messageText)) {

            return sendSuccessfulDeleteMessage();
        } else if (chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.NEW_CATEGORY)) {

            return sendCategoryAddingMessage();
        } else if (mainKeyboardController.isCategoryButton(chatIdPreviousMessage.get(chatId)) &&
            !mainKeyboardController.isKeyboardButton(messageText)) {

            return sendExpensesLinesAddingMessage();
        } else if (mainKeyboardController.isSummaryButton(messageText)) {

            return sendSummaryButtonMessage();
        } else if (mainKeyboardController.isCategoryButton(messageText)) {

            return sendCategoryButtonMessage();
        }

        return null;
    }

    private SendMessage sendSuccessfulDeleteMessage() {
        chatIdPreviousMessage.put(chatId, messageText);
        log.info("Delete category: {}, from chat: {}", messageText, chatId);
        return MessageFactory.initReplyKeyboardSendMessage(message,
            mainKeyboardController.deleteCategory(messageText), BotResponseConstants.DELETE_SUCCESSFUL);
    }

    private SendMessage sendCategoryAddingMessage() {
        chatIdPreviousMessage.put(chatId, messageText);

        if (mainKeyboardController.isSuitableForAdding(messageText)) {
            log.info("Successfully added category: {} in chat: {}", messageText, chatId);
            return MessageFactory.initReplyKeyboardSendMessage(message,
                mainKeyboardController.addCategory(messageText), BotResponseConstants.ADD_SUCCESSFUL);
        }
        log.info("Wrong category name: {} in chat: {}", messageText, chatId);
        return MessageFactory
            .initSendMessage(message, BotResponseConstants.WRONG_CATEGORY_NAME_ERROR);
    }

    private SendMessage sendExpensesLinesAddingMessage() {
        chatIdPreviousMessage.put(chatId, messageText);

        if (!ExpensesParserUtil.isExpensesLines(messageText)) {
            log.info("Wrong expenses lines format: {} in chat: {}", messageText, chatId);
            return combineErrorExpensesLinesMessage();
        }

        if (ExpensesParserUtil.isTooBigExpenses(messageText)) {
            log.info("Too big expenses: {} in chat: {}", messageText, chatId);
            telegramBot.sendTextMessage(message,
                BotResponseConstants.TOO_BIG_EXPENSES_WARNING);
        }
        log.info("Add expenses: {}\nto category: {} in chat: {}", messageText, chatIdCategory.get(chatId), chatId);
        return MessageFactory.initReplyKeyboardSendMessage(message,
            mainKeyboardController.changeButtonExpenses(chatIdCategory.get(chatId), messageText),
            BotResponseConstants.CHANGED);
    }

    private SendMessage sendSummaryButtonMessage() {
        chatIdCategory.put(chatId, BotCommandConstants.SUMMARY_BUTTON);
        chatIdPreviousMessage.put(chatId, messageText);
        log.info("Summary button in chat: {}", chatId);
        return MessageFactory.initInlineKeyboardSendMessage(message,
            expensesReportKeyboard.createTimeIntervalsKeyboard(),
            BotResponseConstants.SUMMARY_TIME_PERIOD_INFO);
    }

    private SendMessage sendCategoryButtonMessage() {
        chatIdCategory.put(chatId, ExpensesParserUtil.parseCategoryName(messageText));
        chatIdPreviousMessage.put(chatId, messageText);
        log.info("Category button: {} in chat: {}", messageText, chatId);
        return MessageFactory.initInlineKeyboardSendMessage(message,
            expensesReportKeyboard.createTimeIntervalsKeyboard(),
            BotResponseConstants.ADD_EXPENSES_OR_GET_REPORT_INFO);
    }

    private SendMessage sendReplyToCallback(Update update) {
        final String buttonData = update.getCallbackQuery().getData();

        switch (buttonData) {
            case CategoriesControlData.NEW_CATEGORY:
                return sendCategoryNameRequestMessage(update);
            case CategoriesControlData.DELETE_CATEGORY:
                return sendChooseCategoryToDeleteMessage(update);
            case CategoriesControlData.RESET_EXPENSES:
                return sendExpensesResetMessage(update);
            case ExpensesReportData.MESSAGE_FORMAT:
                return sendReportMessage(update);
            case ExpensesReportData.FILE_FORMAT:
                sendReportFile(update);
                break;
            case ExpensesReportData.BACK:
                changeToTimeIntervalsKeyboard(update);
                break;
            case ExpensesReportData.ALL_TIME:
            case ExpensesReportData.SIX_MONTHS:
            case ExpensesReportData.THIRTY_DAYS:
            case ExpensesReportData.SEVEN_DAYS:
                changeToReportFormatKeyboard(update);
                break;
            default:
                return new SendMessage();
        }

        return null;
    }

    private SendMessage sendCategoryNameRequestMessage(Update update) {
        chatIdPreviousMessage.put(chatId, update.getCallbackQuery().getData());
        return MessageFactory.initCallbackSendMessage(update, BotResponseConstants.SEND_CATEGORY_NAME);
    }

    private SendMessage sendChooseCategoryToDeleteMessage(Update update) {
        chatIdPreviousMessage.put(chatId, update.getCallbackQuery().getData());
        return MessageFactory
            .initCallbackSendMessage(update, BotResponseConstants.CHOOSE_CATEGORY_TO_DELETE);
    }

    private SendMessage sendExpensesResetMessage(Update update) {
        chatIdPreviousMessage.put(chatId, update.getCallbackQuery().getData());
        return MessageFactory.initCallbackReplyKeyboardSendMessage(update, mainKeyboardController.resetExpenses(),
            BotResponseConstants.EXPENSES_RESET);
    }

    private SendMessage sendReportMessage(Update update) {
        if (StringUtils.isBlank(chatIdTimeInterval.get(chatId))) {
            return MessageFactory.initSendMessage(message, BotResponseConstants.CHOOSE_TIME_INTERVAL);
        }

        String reportMessage = createReportMessage();
        chatIdPreviousMessage.put(chatId, messageText);
        return MessageFactory.initCallbackSendMessage(update, reportMessage);
    }

    private void sendReportFile(Update update) {
        File document = createReportFile();

        chatIdPreviousMessage.put(chatId, messageText);
        telegramBot.sendTextMessage(message, BotResponseConstants.CREATING_FILE);
        telegramBot.sendDocument(update, BotResponseConstants.REPORT_FILE, document);
    }

    private void changeToTimeIntervalsKeyboard(Update update) {
        List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createTimeIntervalsKeyboard();
        chatIdPreviousMessage.put(chatId, messageText);
        if (chatIdCategory.get(chatId).equals(BotCommandConstants.SUMMARY_BUTTON)) {
            telegramBot
                .changeInlineKeyboardMessage(update, keyboard, BotResponseConstants.SUMMARY_TIME_PERIOD_INFO);
        } else {
            telegramBot.changeInlineKeyboardMessage(update, keyboard,
                BotResponseConstants.ADD_EXPENSES_OR_GET_REPORT_INFO);
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
                return "";
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

    private SendMessage combineErrorExpensesLinesMessage() {
        if (ExpensesParserUtil.countSeparators(messageText) == 0) {
            return MessageFactory.initSendMessage(message,
                BotResponseConstants.WRONG_FORMAT_ERROR + BotResponseConstants.NO_SEPARATOR_ERROR);
        } else if (ExpensesParserUtil.isExpensesNonNaturalNumber(messageText)) {
            return MessageFactory.initSendMessage(message,
                BotResponseConstants.WRONG_FORMAT_ERROR + BotResponseConstants.NEGATIVE_EXPENSES_ERROR);
        } else if (ExpensesParserUtil.countSeparators(messageText) > 1) {
            return MessageFactory.initSendMessage(message,
                BotResponseConstants.WRONG_FORMAT_ERROR + BotResponseConstants.TOO_MANY_SEPARATORS_ERROR);
        }

        return MessageFactory.initSendMessage(message, BotResponseConstants.WRONG_FORMAT_ERROR);
    }

    private void changeToReportFormatKeyboard(Update update) {
        List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createReportFormatKeyboard();

        chatIdTimeInterval.put(chatId, update.getCallbackQuery().getData());
        chatIdPreviousMessage.put(chatId, messageText);

        log.info("Change to report format keyboard in chat: {}", chatId);
        telegramBot.changeInlineKeyboardMessage(update, keyboard, BotResponseConstants.CHOOSE_REPORT_FORMAT);
    }
}

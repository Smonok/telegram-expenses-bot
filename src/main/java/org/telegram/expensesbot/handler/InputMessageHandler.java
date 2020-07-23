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
    private final Map<Long, String> chatIdPreviousMessage = new HashMap<>();
    private final Map<Long, String> chatIdTimeInterval = new HashMap<>();
    private final Map<Long, String> chatIdCategory = new HashMap<>();
    private Message message;
    private Long chatId = 0L;
    private String messageText = "";

    @Autowired
    private MainKeyboardController mainKeyboard;
    @Autowired
    private MessageReportController messageReportController;
    @Autowired
    private FileReportController fileReportController;
    @Autowired
    private Bot telegramBot;

    public SendMessage handleInput(Update update) {
        if (update.hasMessage()) {
            message = update.getMessage();
            if (message != null && message.hasText()) {
                messageText = message.getText();
                chatId = message.getChatId();
                mainKeyboard.setChatId(chatId);
                initChatIdPreviousMessage();

                return sendReplyToMessage();
            }
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            mainKeyboard.setChatId(chatId);
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
        switch (messageText) {
            case BotCommandConstants.START_COMMAND:

                chatIdPreviousMessage.put(chatId, messageText);
                return MessageFactory.initReplyKeyboardSendMessage(message, mainKeyboard.removeUserExpenses(),
                    BotResponseConstants.START_WORK);
            case BotCommandConstants.CATEGORIES_CONTROL_BUTTON:

                chatIdPreviousMessage.put(chatId, messageText);
                return MessageFactory.initInlineKeyboardSendMessage(message,
                    categoriesControlKeyboard.getKeyboard(), BotResponseConstants.CHOOSE_ACTION);
            case BotCommandConstants.HELP_BUTTON:

                chatIdPreviousMessage.put(chatId, messageText);
                return MessageFactory.initSendMessage(message, BotResponseConstants.HELP_INFO);
            case BotCommandConstants.ID_COMMAND:

                chatIdPreviousMessage.put(chatId, messageText);
                return MessageFactory.initSendMessage(message, BotResponseConstants.YOUR_ID + chatId);
        }

        if (chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.DELETE_CATEGORY) &&
            mainKeyboard.isCategoryButton(messageText)) {

            return sendSuccessfulDeleteMessage();
        } else if (chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.NEW_CATEGORY)) {

            return sendCategoryAddingMessage();
        } else if (mainKeyboard.isCategoryButton(chatIdPreviousMessage.get(chatId)) &&
            !mainKeyboard.isKeyboardButton(messageText)) {

            return sendExpensesLinesAddingMessage();
        } else if (mainKeyboard.isSummaryButton(messageText)) {

            return sendSummaryButtonMessage();
        } else if (mainKeyboard.isCategoryButton(messageText)) {

            return sendCategoryButtonMessage();
        }

        return MessageFactory.initSendMessage(message, BotResponseConstants.UNKNOWN_COMMAND);
    }

    private SendMessage sendSuccessfulDeleteMessage() {
        chatIdPreviousMessage.put(chatId, messageText);
        return MessageFactory.initReplyKeyboardSendMessage(message,
            mainKeyboard.deleteCategory(messageText), BotResponseConstants.DELETE_SUCCESSFUL);
    }

    private SendMessage sendCategoryAddingMessage() {
        chatIdPreviousMessage.put(chatId, messageText);

        if (mainKeyboard.isSuitableForAdding(messageText)) {
            return MessageFactory.initReplyKeyboardSendMessage(message,
                mainKeyboard.addCategory(messageText), BotResponseConstants.ADD_SUCCESSFUL);
        }

        return MessageFactory
            .initSendMessage(message, BotResponseConstants.WRONG_CATEGORY_NAME_ERROR);
    }

    private SendMessage sendExpensesLinesAddingMessage() {
        chatIdPreviousMessage.put(chatId, messageText);

        if (!ExpensesParserUtil.isExpensesLines(messageText)) {
            return combineErrorExpensesLinesMessage();
        }

        if (ExpensesParserUtil.isTooBigExpenses(messageText)) {
            telegramBot.sendTextMessage(message,
                BotResponseConstants.TOO_BIG_EXPENSES_WARNING);
        }

        return MessageFactory.initReplyKeyboardSendMessage(message,
            mainKeyboard.changeButtonExpenses(chatIdCategory.get(chatId), messageText),
            BotResponseConstants.CHANGED);
    }

    private SendMessage sendSummaryButtonMessage() {
        chatIdCategory.put(chatId, BotCommandConstants.SUMMARY_BUTTON);
        chatIdPreviousMessage.put(chatId, messageText);

        return MessageFactory.initInlineKeyboardSendMessage(message,
            expensesReportKeyboard.createTimeIntervalsKeyboard(),
            BotResponseConstants.SUMMARY_TIME_PERIOD_INFO);
    }

    private SendMessage sendCategoryButtonMessage() {
        chatIdCategory.put(chatId, ExpensesParserUtil.parseCategoryName(messageText));
        chatIdPreviousMessage.put(chatId, messageText);

        return MessageFactory.initInlineKeyboardSendMessage(message,
            expensesReportKeyboard.createTimeIntervalsKeyboard(),
            BotResponseConstants.ADD_EXPENSES_OR_GET_REPORT_INFO);
    }

    private SendMessage sendReplyToCallback(Update update) {
        String buttonData = update.getCallbackQuery().getData();

        switch (buttonData) {
            case CategoriesControlData.NEW_CATEGORY:
                chatIdPreviousMessage.put(chatId, buttonData);
                return MessageFactory.initCallbackSendMessage(update, BotResponseConstants.SEND_CATEGORY_NAME);
            case CategoriesControlData.DELETE_CATEGORY:
                chatIdPreviousMessage.put(chatId, buttonData);
                return MessageFactory
                    .initCallbackSendMessage(update, BotResponseConstants.CHOOSE_CATEGORY_TO_DELETE);
            case CategoriesControlData.RESET_BILLS:
                chatIdPreviousMessage.put(chatId, buttonData);
                return MessageFactory.initCallbackReplyKeyboardSendMessage(update, mainKeyboard.resetExpenses(),
                    BotResponseConstants.BILLS_RESET);
            case ExpensesReportData.MESSAGE_FORMAT:
                if (!StringUtils.isBlank(chatIdTimeInterval.get(chatId))) {
                    String reportMessage = createReportMessage();
                    chatIdPreviousMessage.put(chatId, messageText);
                    return MessageFactory.initCallbackSendMessage(update, reportMessage);
                }
                break;
            case ExpensesReportData.FILE_FORMAT:
                File document = createReportFile();
                chatIdPreviousMessage.put(chatId, messageText);
                telegramBot.sendDocument(update, BotResponseConstants.REPORT_FILE, document);
                break;
            case ExpensesReportData.BACK:
                List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createTimeIntervalsKeyboard();
                chatIdPreviousMessage.put(chatId, messageText);
                if (chatIdCategory.get(chatId).equals(BotCommandConstants.SUMMARY_BUTTON)) {
                    telegramBot
                        .changeInlineKeyboardMessage(update, keyboard, BotResponseConstants.SUMMARY_TIME_PERIOD_INFO);
                } else {
                    telegramBot.changeInlineKeyboardMessage(update, keyboard,
                        BotResponseConstants.ADD_EXPENSES_OR_GET_REPORT_INFO);
                }
                break;
            case ExpensesReportData.ALL_TIME:
                chatIdTimeInterval.put(chatId, buttonData);
                chatIdPreviousMessage.put(chatId, messageText);
                changeToReportFormatKeyboard(update);
                break;
            case ExpensesReportData.SIX_MONTHS:
                chatIdTimeInterval.put(chatId, buttonData);
                chatIdPreviousMessage.put(chatId, messageText);
                changeToReportFormatKeyboard(update);
                break;
            case ExpensesReportData.THIRTY_DAYS:
                chatIdTimeInterval.put(chatId, buttonData);
                chatIdPreviousMessage.put(chatId, messageText);
                changeToReportFormatKeyboard(update);
                break;
            case ExpensesReportData.SEVEN_DAYS:
                chatIdTimeInterval.put(chatId, buttonData);
                chatIdPreviousMessage.put(chatId, messageText);
                changeToReportFormatKeyboard(update);
                break;
            default:
                return new SendMessage();
        }

        return new SendMessage();
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
        log.info("Change to report format keyboard in chat: {}", chatId);
        telegramBot.changeInlineKeyboardMessage(update, keyboard, BotResponseConstants.CHOOSE_REPORT_FORMAT);
    }
}

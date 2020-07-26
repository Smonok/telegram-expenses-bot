package org.telegram.expensesbot.handler;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
public class InputHandler {
    private static final Logger log = LoggerFactory.getLogger(InputHandler.class);
    private final ExpensesReportKeyboard expensesReportKeyboard = new ExpensesReportKeyboard();
    private final CategoriesControlKeyboard categoriesControlKeyboard = new CategoriesControlKeyboard();
    private final MainKeyboardController mainKeyboardController;
    private final MessageReportController messageReportController;
    private final FileReportController fileReportController;
    private final Map<Long, String> chatIdPreviousMessage = new HashMap<>();
    private final Map<Long, String> chatIdTimeInterval = new HashMap<>();
    private final Map<Long, String> chatIdCategory = new HashMap<>();
    private final Bot telegramBot;
    private Message message;

    @Autowired
    public InputHandler(MainKeyboardController mainKeyboardController,
        MessageReportController messageReportController,
        FileReportController fileReportController, @Lazy Bot telegramBot) {
        this.mainKeyboardController = mainKeyboardController;
        this.messageReportController = messageReportController;
        this.fileReportController = fileReportController;
        this.telegramBot = telegramBot;
    }

    public SendMessage handleInput(Update update) {
        if (update.hasMessage()) {
            message = update.getMessage();
            if (message != null && message.hasText()) {
                Long chatId = message.getChatId();
                initChatIdPreviousMessage(chatId);

                return sendReplyToMessage(chatId);
            }
        } else if (update.hasCallbackQuery()) {
            message = update.getCallbackQuery().getMessage();
            Long chatId = message.getChatId();
            initChatIdPreviousMessage(chatId);

            return sendReplyToCallback(update, chatId);
        }

        return new SendMessage();
    }

    private void initChatIdPreviousMessage(Long chatId) {
        if (!chatIdPreviousMessage.containsKey(chatId)) {
            chatIdPreviousMessage.put(chatId, chatId.toString());
        }
    }

    private SendMessage sendReplyToMessage(Long chatId) {
        SendMessage resultMessage = handleMessageText(chatId);
        if (resultMessage != null) {
            return resultMessage;
        }

        resultMessage = handleCategoryButtonsControl(chatId);

        return resultMessage == null ?
            MessageFactory.initSendMessage(message, BotResponseConstants.UNKNOWN_COMMAND) : resultMessage;
    }

    private SendMessage handleMessageText(Long chatId) {
        mainKeyboardController.setChatId(chatId);

        switch (message.getText()) {
            case BotCommandConstants.START_COMMAND:
                log.info("/start in chat: {}", chatId);
                chatIdPreviousMessage.put(chatId, message.getText());
                return MessageFactory.initReplyKeyboardSendMessage(message, mainKeyboardController.removeUserExpenses(),
                    BotResponseConstants.START_WORK);
            case BotCommandConstants.CATEGORIES_CONTROL_BUTTON:
                log.info("Categories control button in chat: {}", chatId);
                chatIdPreviousMessage.put(chatId, message.getText());
                return MessageFactory.initInlineKeyboardSendMessage(message,
                    categoriesControlKeyboard.getKeyboard(), BotResponseConstants.CHOOSE_ACTION);
            case BotCommandConstants.HELP_COMMAND:
            case BotCommandConstants.HELP_BUTTON:
                log.info("Help control button in chat: {}", chatId);
                chatIdPreviousMessage.put(chatId, message.getText());
                return MessageFactory.initSendMessage(message, BotResponseConstants.HELP_INFO);
            case BotCommandConstants.ID_COMMAND:
                log.info("/id in chat: {}", chatId);
                chatIdPreviousMessage.put(chatId, message.getText());
                return MessageFactory.initSendMessage(message, BotResponseConstants.YOUR_ID + chatId);
            default:
                return null;
        }
    }

    private SendMessage handleCategoryButtonsControl(Long chatId) {
        mainKeyboardController.setChatId(chatId);

        if (chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.DELETE_CATEGORY) &&
            mainKeyboardController.isCategoryButton(message.getText())) {

            return sendSuccessfulDeleteMessage(chatId);
        } else if (chatIdPreviousMessage.get(chatId).equals(CategoriesControlData.NEW_CATEGORY)) {

            return sendCategoryAddingMessage(chatId);
        } else if (mainKeyboardController.isCategoryButton(chatIdPreviousMessage.get(chatId)) &&
            !mainKeyboardController.isKeyboardButton(message.getText())) {

            return sendExpensesLinesAddingMessage(chatId);
        } else if (mainKeyboardController.isSummaryButton(message.getText())) {

            return sendSummaryButtonMessage(chatId);
        } else if (mainKeyboardController.isCategoryButton(message.getText())) {

            return sendCategoryButtonMessage(chatId);
        }

        return null;
    }

    private SendMessage sendSuccessfulDeleteMessage(Long chatId) {
        chatIdPreviousMessage.put(chatId, message.getText());
        mainKeyboardController.setChatId(chatId);

        log.info("Delete category: {}, from chat: {}", message.getText(), chatId);
        return MessageFactory.initReplyKeyboardSendMessage(message,
            mainKeyboardController.deleteCategory(message.getText()), BotResponseConstants.DELETE_SUCCESSFUL);
    }

    private SendMessage sendCategoryAddingMessage(Long chatId) {
        chatIdPreviousMessage.put(chatId, message.getText());
        mainKeyboardController.setChatId(chatId);

        if (mainKeyboardController.isSuitableForAdding(message.getText())) {
            log.info("Successfully added category: {} in chat: {}", message.getText(), chatId);
            return MessageFactory.initReplyKeyboardSendMessage(message,
                mainKeyboardController.addCategory(message.getText()), BotResponseConstants.ADD_SUCCESSFUL);
        }
        log.info("Wrong category name: {} in chat: {}", message.getText(), chatId);
        return MessageFactory
            .initSendMessage(message, BotResponseConstants.WRONG_CATEGORY_NAME_ERROR);
    }

    private SendMessage sendExpensesLinesAddingMessage(Long chatId) {
        chatIdPreviousMessage.put(chatId, message.getText());
        mainKeyboardController.setChatId(chatId);

        if (!ExpensesParserUtil.isExpensesLines(message.getText())) {
            log.info("Wrong expenses lines format: {} in chat: {}", message.getText(), chatId);
            return combineErrorExpensesLinesMessage();
        }

        if (ExpensesParserUtil.isTooBigExpenses(message.getText())) {
            log.info("Too big expenses: {} in chat: {}", message.getText(), chatId);
            telegramBot.sendTextMessage(message,
                BotResponseConstants.TOO_BIG_EXPENSES_WARNING);
        }
        log.info("Add expenses: {} to category: {} in chat: {}", message.getText(), chatIdCategory.get(chatId), chatId);
        return MessageFactory.initReplyKeyboardSendMessage(message,
            mainKeyboardController.changeButtonExpenses(chatIdCategory.get(chatId), message.getText()),
            BotResponseConstants.CHANGED);
    }

    private SendMessage sendSummaryButtonMessage(Long chatId) {
        chatIdCategory.put(chatId, BotCommandConstants.SUMMARY_BUTTON);
        chatIdPreviousMessage.put(chatId, message.getText());
        log.info("Summary button in chat: {}", chatId);
        return MessageFactory.initInlineKeyboardSendMessage(message,
            expensesReportKeyboard.createTimeIntervalsKeyboard(),
            BotResponseConstants.SUMMARY_TIME_PERIOD_INFO);
    }

    private SendMessage sendCategoryButtonMessage(Long chatId) {
        chatIdCategory.put(chatId, ExpensesParserUtil.parseCategoryName(message.getText()));
        chatIdPreviousMessage.put(chatId, message.getText());
        log.info("Category button: {} in chat: {}", message.getText(), chatId);
        return MessageFactory.initInlineKeyboardSendMessage(message,
            expensesReportKeyboard.createTimeIntervalsKeyboard(),
            BotResponseConstants.ADD_EXPENSES_OR_GET_REPORT_INFO);
    }

    private SendMessage sendReplyToCallback(Update update, Long chatId) {
        final String buttonData = update.getCallbackQuery().getData();

        switch (buttonData) {
            case CategoriesControlData.NEW_CATEGORY:
                return sendCategoryNameRequestMessage(update, chatId);
            case CategoriesControlData.DELETE_CATEGORY:
                return sendChooseCategoryToDeleteMessage(update, chatId);
            case CategoriesControlData.RESET_EXPENSES:
                return sendExpensesResetMessage(update, chatId);
            case ExpensesReportData.MESSAGE_FORMAT:
                return sendReportMessage(update, chatId);
            case ExpensesReportData.FILE_FORMAT:
                sendReportFile(update, chatId);
                break;
            case ExpensesReportData.BACK:
                changeToTimeIntervalsKeyboard(update, chatId);
                break;
            case ExpensesReportData.ALL_TIME:
            case ExpensesReportData.SIX_MONTHS:
            case ExpensesReportData.THIRTY_DAYS:
            case ExpensesReportData.SEVEN_DAYS:
                changeToReportFormatKeyboard(update, chatId);
                break;
            default:
                return new SendMessage();
        }

        return null;
    }

    private SendMessage sendCategoryNameRequestMessage(Update update, Long chatId) {
        chatIdPreviousMessage.put(chatId, update.getCallbackQuery().getData());

        return MessageFactory.initCallbackSendMessage(update, BotResponseConstants.SEND_CATEGORY_NAME);
    }

    private SendMessage sendChooseCategoryToDeleteMessage(Update update, Long chatId) {
        chatIdPreviousMessage.put(chatId, update.getCallbackQuery().getData());

        return MessageFactory
            .initCallbackSendMessage(update, BotResponseConstants.CHOOSE_CATEGORY_TO_DELETE);
    }

    private SendMessage sendExpensesResetMessage(Update update, Long chatId) {
        chatIdPreviousMessage.put(chatId, update.getCallbackQuery().getData());
        mainKeyboardController.setChatId(chatId);

        return MessageFactory.initCallbackReplyKeyboardSendMessage(update, mainKeyboardController.resetExpenses(),
            BotResponseConstants.EXPENSES_RESET);
    }

    private SendMessage sendReportMessage(Update update, Long chatId) {
        if (StringUtils.isBlank(chatIdTimeInterval.get(chatId))) {
            return MessageFactory.initSendMessage(message, BotResponseConstants.CHOOSE_TIME_INTERVAL);
        }

        String reportMessage = createReportMessage(chatId);
        chatIdPreviousMessage.put(chatId, message.getText());
        return MessageFactory.initCallbackSendMessage(update, reportMessage);
    }

    private String createReportMessage(Long chatId) {
        String category = chatIdCategory.get(chatId);
        messageReportController.setChatId(chatId);

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

    private void sendReportFile(Update update, Long chatId) {
        telegramBot.sendTextMessage(update.getCallbackQuery().getMessage(), BotResponseConstants.CREATING_FILE);

        File document = createReportFile(chatId);

        chatIdPreviousMessage.put(chatId, message.getText());
        telegramBot.sendDocument(update, BotResponseConstants.REPORT_FILE, document);
    }

    private File createReportFile(Long chatId) {
        String category = chatIdCategory.get(chatId);
        fileReportController.setChatId(chatId);

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

    private void changeToTimeIntervalsKeyboard(Update update, Long chatId) {
        List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createTimeIntervalsKeyboard();
        chatIdPreviousMessage.put(chatId, message.getText());

        if (chatIdCategory.get(chatId).equals(BotCommandConstants.SUMMARY_BUTTON)) {
            telegramBot
                .changeInlineKeyboardMessage(update, keyboard, BotResponseConstants.SUMMARY_TIME_PERIOD_INFO);
        } else {
            telegramBot.changeInlineKeyboardMessage(update, keyboard,
                BotResponseConstants.ADD_EXPENSES_OR_GET_REPORT_INFO);
        }
    }

    private SendMessage combineErrorExpensesLinesMessage() {
        if (ExpensesParserUtil.countSeparators(message.getText()) == 0) {
            return MessageFactory.initSendMessage(message,
                BotResponseConstants.WRONG_FORMAT_ERROR + BotResponseConstants.NO_SEPARATOR_ERROR);
        } else if (ExpensesParserUtil.isExpensesNonNaturalNumber(message.getText())) {
            return MessageFactory.initSendMessage(message,
                BotResponseConstants.WRONG_FORMAT_ERROR + BotResponseConstants.NEGATIVE_EXPENSES_ERROR);
        } else if (ExpensesParserUtil.countSeparators(message.getText()) > 1) {
            return MessageFactory.initSendMessage(message,
                BotResponseConstants.WRONG_FORMAT_ERROR + BotResponseConstants.TOO_MANY_SEPARATORS_ERROR);
        }

        return MessageFactory.initSendMessage(message, BotResponseConstants.WRONG_FORMAT_ERROR);
    }

    private void changeToReportFormatKeyboard(Update update, Long chatId) {
        List<List<InlineKeyboardButton>> keyboard = expensesReportKeyboard.createReportFormatKeyboard();

        chatIdTimeInterval.put(chatId, update.getCallbackQuery().getData());
        chatIdPreviousMessage.put(chatId, message.getText());

        log.info("Change to report format keyboard in chat: {}", chatId);
        telegramBot.changeInlineKeyboardMessage(update, keyboard, BotResponseConstants.CHOOSE_REPORT_FORMAT);
    }
}

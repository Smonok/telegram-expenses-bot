package org.telegram.expensesbot.factory;

import java.util.List;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class MessageFactory {
    private static final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    private static final InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

    public static SendMessage initSendMessage(Message message) {
        SendMessage sendMessage = new SendMessage();

        if (message != null) {
            sendMessage.setChatId(message.getChatId());
            sendMessage.setReplyToMessageId(message.getMessageId());
            sendMessage.enableHtml(true);
        }
        return sendMessage;
    }

    public static SendMessage initReplyKeyboardSendMessage(Message message, List<KeyboardRow> keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);

        if (message != null) {
            sendMessage.setChatId(message.getChatId());
            sendMessage.setReplyToMessageId(message.getMessageId());
        }

        configureReplyKeyboardMarkup(keyboard);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    private static void configureReplyKeyboardMarkup(List<KeyboardRow> keyboard) {
        replyKeyboardMarkup.setSelective(false);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    public static SendMessage initInlineKeyboardSendMessage(Message message,
        List<List<InlineKeyboardButton>> keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());

        configureInlineKeyboardMarkup(keyboard);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }

    private static void configureInlineKeyboardMarkup(List<List<InlineKeyboardButton>> keyboard) {
        inlineKeyboardMarkup.setKeyboard(keyboard);
    }

    public static SendMessage initCallbackSendMessage(Update update) {
        return new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId()).enableHtml(true);
    }

    public static SendMessage initCallbackReplyKeyboardSendMessage(Update update, List<KeyboardRow> keyboard) {
        SendMessage sendMessage = new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId());
        configureReplyKeyboardMarkup(keyboard);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.enableHtml(true);

        return sendMessage;
    }

    public static SendDocument initCallbackSendDocument(Update update, String caption) {
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendDocumentRequest.setCaption(caption);
        return sendDocumentRequest;
    }

    public static EditMessageText initEditMessageText(Update update, List<List<InlineKeyboardButton>> keyboard) {
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String inlineMessageId = update.getCallbackQuery().getInlineMessageId();

        return new EditMessageText()
            .setChatId(chatId)
            .setMessageId(messageId)
            .setInlineMessageId(inlineMessageId)
            .setReplyMarkup(new InlineKeyboardMarkup(keyboard));
    }
}

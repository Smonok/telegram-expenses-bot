package org.telegram.expensesbot;

import java.io.File;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.expensesbot.factory.MessageFactory;
import org.telegram.expensesbot.handler.InputHandler;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramWebhookBot {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    private final InputHandler inputHandler;
    private String webHookPath;
    private String botUserName;
    private String botToken;

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return inputHandler.handleInput(update);
    }

    @Autowired
    public Bot(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public void sendTextMessage(Message message, String text) {
        if (!StringUtils.isBlank(text) && message != null) {
            SendMessage sendMessage = MessageFactory.initSendMessage(message, text);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Cannot send message: {}", sendMessage.toString(), e);
            }
        }
    }

    public void sendDocument(Update update, String text, File document) {
        if (!StringUtils.isBlank(text) && update != null && document != null) {
            SendDocument sendDocument = MessageFactory.initCallbackSendDocument(update, document, text);

            try {
                execute(sendDocument);
            } catch (TelegramApiException e) {
                log.error("Cannot send document: {}", document.toString(), e);
            }
        }
    }

    public void changeInlineKeyboardMessage(Update update, List<List<InlineKeyboardButton>> keyboard, String text) {
        if (!StringUtils.isBlank(text) && update != null && keyboard != null) {
            EditMessageText editedMessage = MessageFactory.initEditMessageText(update, keyboard, text);

            try {
                sendApiMethod(editedMessage);
            } catch (TelegramApiException e) {
                log.error("Cannot change message with inline keyboard", e);
            }
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotPath() {
        return webHookPath;
    }

    public void setWebHookPath(String webHookPath) {
        this.webHookPath = webHookPath;
    }

    public void setBotUserName(String botUserName) {
        this.botUserName = botUserName;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }
}

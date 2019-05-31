package com.github.kr328.webapi.session;

import com.github.kr328.webapi.Context;
import com.github.kr328.webapi.i18n.I18n;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class DefaultSession implements ISession {
    @Override
    public String getToken() {
        return "";
    }

    @Override
    public ISession handle(Context context, DefaultAbsSender sender, Message message) throws TelegramApiException {
        if (!message.hasText())
            return null;

        String text = message.getText();
        if (!text.startsWith("!") && !text.startsWith("/"))
            return null;

        String command = text.substring(1);

        switch (command) {
            case "start": //start command
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup()
                        .setKeyboard(Arrays.asList(
                                Collections.singletonList(new InlineKeyboardButton()
                                        .setCallbackData("generate_surge2ssd")
                                        .setText(I18n.get("message_button_generate_surge2ssd"))),
                                Collections.singletonList(new InlineKeyboardButton()
                                        .setCallbackData("generate_preclash")
                                        .setText(I18n.get("message_button_generate_preclash"))),
                                Collections.singletonList(new InlineKeyboardButton()
                                        .setCallbackData("join_feedback_group")
                                        .setText(I18n.get("message_button_join_group")))
                        ));
                SendMessage send = new SendMessage()
                        .setChatId(message.getChatId())
                        .enableHtml(true)
                        .enableMarkdown(true)
                        .setText(I18n.get("message_reply_user_start"))
                        .setReplyMarkup(markup);

                sender.execute(send);
        }

        return null;
    }

    @Override
    public ISession handle(Context context, AbsSender sender, CallbackQuery callbackQuery) throws TelegramApiException {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery()
                .setCallbackQueryId(callbackQuery.getId());

        sender.execute(answerCallbackQuery);

        switch (callbackQuery.getData()) {
            case "generate_surge2ssd":
                SendMessage sendMessage = new SendMessage()
                        .setChatId(callbackQuery.getMessage().getChatId())
                        .setText(I18n.get("message_reply_generate_surge2ssd"));

                sender.execute(answerCallbackQuery);
                sender.execute(sendMessage);

                return new Surge2SSDSession(callbackQuery.getMessage().getChatId());
            case "generate_preclash":
                SendMessage clashSend = new SendMessage()
                        .setChatId(callbackQuery.getMessage().getChatId())
                        .enableMarkdown(true)
                        .setText(I18n.get("message_reply_generate_preclash_file"));

                sender.execute(clashSend);

                return new ClashSession(callbackQuery.getMessage().getChatId());

            case "delete_preclash_data":
                I18n.Lazy lazy = I18n.lazy();

                new Thread(() -> {
                    try {
                        context.getStoreManager().delete(callbackQuery.getFrom().getId());
                    } catch (IOException ignored) {
                    }

                    try {
                        EditMessageText text = new EditMessageText()
                                .setMessageId(callbackQuery.getMessage().getMessageId())
                                .setText(lazy.get("message_reply_delete_preclash"))
                                .setChatId(callbackQuery.getMessage().getChatId());

                        sender.execute(text);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
            case "join_feedback_group":
                SendMessage message = new SendMessage()
                        .setChatId(callbackQuery.getMessage().getChatId())
                        .setText(String.format(I18n.get("message_reply_group_link"), context.getGroupLink()));

                sender.execute(message);
        }

        return null;
    }
}

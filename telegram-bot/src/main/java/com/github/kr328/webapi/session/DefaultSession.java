package com.github.kr328.webapi.session;

import com.github.kr328.webapi.i18n.I18n;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Collections;

public class DefaultSession implements ISession {
    @Override
    public String getToken() {
        return "";
    }

    @Override
    public ISession handle(AbsSender sender, Message message) throws TelegramApiException {
        if ( !message.hasText() )
            return null;

        String text = message.getText();
        if ( !text.startsWith("!") && !text.startsWith("/"))
            return null;

        String command = text.substring(1);

        switch (command) {
            case "start": //start command
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup()
                        .setKeyboard(Arrays.asList(
                                Collections.singletonList(new InlineKeyboardButton()
                                        .setCallbackData("generate_surge2ssd")
                                        .setText(I18n.text("message_button_generate_surge2ssd"))) ,
                                Collections.singletonList(new InlineKeyboardButton()
                                        .setCallbackData("generate_preclash")
                                        .setText("hhh"))
                        ));
                SendMessage send = new SendMessage()
                        .setChatId(message.getChatId())
                        .enableHtml(true)
                        .enableMarkdown(true)
                        .setText(I18n.text("message_reply_user_start"))
                        .setReplyMarkup(markup);

                sender.execute(send);
        }

        return null;
    }

    @Override
    public ISession handle(AbsSender sender, CallbackQuery callbackQuery) throws TelegramApiException {
        switch (callbackQuery.getData()) {
            case "generate_surge2ssd":
                SendMessage sendMessage = new SendMessage()
                        .setChatId(callbackQuery.getMessage().getChatId())
                        .setText(I18n.text("message_reply_generate_surge2ssd"));

                sender.execute(sendMessage);

                return new Surge2SSDSession(callbackQuery.getMessage().getChatId());
        }
        return null;
    }
}

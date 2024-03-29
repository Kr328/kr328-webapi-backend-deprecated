package com.github.kr328.webapi.session;

import com.github.kr328.webapi.Constants;
import com.github.kr328.webapi.Context;
import com.github.kr328.webapi.i18n.I18n;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Base64;

public class Surge2SSDSession implements ISession {
    private String token;

    public Surge2SSDSession(long chatId) {
        token = "chat:" + chatId;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public ISession handle(Context context, DefaultAbsSender sender, Message message) throws TelegramApiException {
        String url = message.getText().trim();

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            SendMessage reply = new SendMessage()
                    .setChatId(message.getChatId())
                    .setText(I18n.get("message_reply_invalid_surge_url"));
            sender.execute(reply);

            return null;
        }

        String newLink = Constants.SURGE2SSD_LINK_BEGIN + Base64.getUrlEncoder().encodeToString(url.getBytes());

        SendMessage reply = new SendMessage()
                .setChatId(message.getChatId())
                .setText(String.format(I18n.get("message_reply_surge2ssd_url"), newLink));

        sender.execute(reply);

        return null;
    }

    @Override
    public ISession handle(Context context, AbsSender sender, CallbackQuery callbackQuery) throws TelegramApiException {
        return null;
    }
}

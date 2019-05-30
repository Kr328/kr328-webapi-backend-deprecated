package com.github.kr328.webapi.session;

import com.github.kr328.webapi.Context;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface ISession {
    ISession DEFAULT = new DefaultSession();

    String getToken();
    ISession handle(Context context, DefaultAbsSender sender , Message message) throws TelegramApiException;
    ISession handle(Context context, AbsSender sender ,CallbackQuery callbackQuery) throws TelegramApiException;
}

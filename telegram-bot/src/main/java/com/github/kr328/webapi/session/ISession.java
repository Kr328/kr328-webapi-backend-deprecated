package com.github.kr328.webapi.session;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Function;

public interface ISession {
    ISession DEFAULT = new DefaultSession();

    String getToken();
    ISession handle(AbsSender sender ,Message message) throws TelegramApiException;
    ISession handle(AbsSender sender , CallbackQuery callbackQuery) throws TelegramApiException;
}

package com.github.kr328.webapi.session;

import com.github.kr328.webapi.Context;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Hashtable;
import java.util.Optional;

public class SessionManager {
    private Hashtable<String, ISession> sessions = new Hashtable<>();
    private DefaultAbsSender sender;
    private Context context;

    public SessionManager(Context context, DefaultAbsSender sender) {
        this.context = context;
        this.sender = sender;
    }

    public void handleMessage(Message message) throws TelegramApiException {
        ISession session = Optional.<ISession>empty()
                .or(() -> Optional.ofNullable(sessions.get("user:" + message.getFrom().getId()))) // user id based session
                .or(() -> Optional.ofNullable(sessions.get("chat:" + message.getChatId())))
                .orElse(ISession.DEFAULT);

        ISession nextSession = session.handle(context, sender, message);
        if (nextSession != null)
            sessions.put(nextSession.getToken(), nextSession);

        sessions.remove(session.getToken());
    }

    public void handleCallback(CallbackQuery callbackQuery) throws TelegramApiException {
        ISession session = Optional.<ISession>empty()
                .or(() -> Optional.ofNullable(sessions.get("callback:" + callbackQuery.getData()))) // user id based session
                .orElse(ISession.DEFAULT);

        ISession nextSession = session.handle(context, sender, callbackQuery);
        if (nextSession != null)
            sessions.put(nextSession.getToken(), nextSession);

        sessions.remove(session.getToken());
    }
}

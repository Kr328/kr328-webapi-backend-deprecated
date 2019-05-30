package com.github.kr328.webapi;

import com.github.kr328.webapi.i18n.I18n;
import com.github.kr328.webapi.session.SessionManager;
import com.github.kr328.webapi.store.StoreManager;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.Locale;

public class Kr328TelegramBot extends TelegramLongPollingBot {
    private SessionManager sessionManager;
    private String username;
    private String token;
    private Context context;

    public static void main(String[] args) throws TelegramApiRequestException {
        ApiContextInitializer.init();

        String username = System.getenv("BOT_USERNAME");
        String token = System.getenv("BOT_TOKEN");
        String dataPath = System.getenv("BOT_DATA_PATH");

        if ( username == null || token == null || dataPath == null )
            throw new Error("Invalid username ,token or data path");

        TelegramBotsApi botsApi = new TelegramBotsApi();

        botsApi.registerBot(new Kr328TelegramBot(username, token, dataPath));
    }

    public Kr328TelegramBot(String username, String token, String dataPath) {
        this.username = username;
        this.token = token;
        this.context = new Context(new StoreManager(dataPath));
        this.sessionManager = new SessionManager(context, this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if ( update.getMessage() != null ) {
                I18n.setCurrentLanguage(update.getMessage().getFrom().getLanguageCode());
                sessionManager.handleMessage(update.getMessage());
            }
            else if ( update.getCallbackQuery() != null ) {
                I18n.setCurrentLanguage(update.getCallbackQuery().getFrom().getLanguageCode());
                sessionManager.handleCallback(update.getCallbackQuery());
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}

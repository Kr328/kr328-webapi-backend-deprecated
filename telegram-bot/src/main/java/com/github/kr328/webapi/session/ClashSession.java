package com.github.kr328.webapi.session;

import com.github.kr328.webapi.Constants;
import com.github.kr328.webapi.Context;
import com.github.kr328.webapi.i18n.I18n;
import com.github.kr328.webapi.model.Metadata;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.DownloadFileCallback;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.io.IOException;
import java.util.Collections;

public class ClashSession implements ISession {
    private long chatId;

    public ClashSession(long chatId) {
        this.chatId = chatId;
    }

    @Override
    public String getToken() {
        return "chat:" + chatId;
    }

    @Override
    public ISession handle(Context context, DefaultAbsSender sender, Message message) throws TelegramApiException {
        Document document = message.getDocument();

        if (document == null) {
            SendMessage sendMessage = new SendMessage()
                    .setChatId(message.getChatId())
                    .setText(I18n.get("message_reply_invalid_file"));

            sender.execute(sendMessage);

            return null;
        }

        if (document.getFileSize() > 1024 * 1024) {
            SendMessage sendMessage = new SendMessage()
                    .setChatId(message.getChatId())
                    .setText(I18n.get("message_reply_large_file"));

            sender.execute(sendMessage);

            return null;
        }

        I18n.Lazy lazy = I18n.lazy();
        GetFile getFile = new GetFile().setFileId(document.getFileId());

        SendMessage sendMessage = new SendMessage()
                .setText(I18n.get("message_reply_downloading"))
                .setChatId(message.getChatId());

        Message downloading = sender.execute(sendMessage);

        sender.executeAsync(getFile, new SentCallback<>() {
            @Override
            public void onResult(BotApiMethod<File> method, File response) {
                SentCallback<File> sentCallback = this;

                try {
                    sender.downloadFileAsync(response, new DownloadFileCallback<>() {
                        @Override
                        public void onResult(File file, java.io.File output) {
                            Metadata metadata;

                            try {
                                metadata = context.getStoreManager()
                                        .save(message.getFrom().getUserName(),
                                                message.getFrom().getId(),
                                                message.getMessageId(), output);
                            } catch (IOException e) {
                                sentCallback.onException(method, e);
                                return;
                            }

                            InlineKeyboardMarkup markup = new InlineKeyboardMarkup()
                                    .setKeyboard(Collections.singletonList(Collections.singletonList(
                                            new InlineKeyboardButton().setCallbackData("delete_preclash_data").setText(lazy.get("message_button_delete_preclash"))
                                    )));

                            EditMessageText text = new EditMessageText()
                                    .setReplyMarkup(markup)
                                    .setMessageId(downloading.getMessageId())
                                    .setText(String.format(lazy.get("message_reply_preclash_url"), String.format(Constants.PRE_CLASH_LINK_FORMAT, metadata.getUserId(), metadata.getSecret())))
                                    .setChatId(message.getChatId());
                            try {
                                sender.execute(text);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onException(File file, Exception exception) {
                            sentCallback.onException(method, exception);
                        }
                    });
                } catch (TelegramApiException e) {
                    onException(method, e);
                }
            }

            @Override
            public void onError(BotApiMethod<File> method, TelegramApiRequestException apiException) {
                onException(method, apiException);
            }

            @Override
            public void onException(BotApiMethod<File> method, Exception exception) {
                SendMessage s = new SendMessage()
                        .setChatId(chatId)
                        .setText(lazy.get("message_reply_download_failure"));

                try {
                    sender.execute(s);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        });

        return null;
    }

    @Override
    public ISession handle(Context context, AbsSender sender, CallbackQuery callbackQuery) throws TelegramApiException {
        return null;
    }
}

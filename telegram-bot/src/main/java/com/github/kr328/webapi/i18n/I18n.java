package com.github.kr328.webapi.i18n;

import javax.annotation.Resource;
import java.util.*;

public class I18n {
    private static final ResourceBundle defaultLanguage = ResourceBundle.getBundle(I18n.class.getPackageName() + ".i18n");
    private static ThreadLocal<ResourceBundle> currentLanguage = new ThreadLocal<>();
    private static Hashtable<Locale, ResourceBundle> cache = new Hashtable<>();

    public static String text(String t) {
        return Optional.ofNullable(currentLanguage.get()).map(r -> r.getString(t)).orElseGet(() -> defaultLanguage.getString(t));
    }

    public static void setCurrentLanguage(String locale) {
        if ( locale == null )
            currentLanguage.set(null);
        else
            currentLanguage.set(cache.computeIfAbsent(Locale.forLanguageTag(locale), o -> ResourceBundle.getBundle(I18n.class.getPackageName() + ".i18n", Locale.forLanguageTag(locale))));
    }

    public static Thread thread(Runnable runnable) {
        ResourceBundle parent = currentLanguage.get();

        return new Thread(() -> {
            currentLanguage.set(parent);
            runnable.run();
        });
    }
}

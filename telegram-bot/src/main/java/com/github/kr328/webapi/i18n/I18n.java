package com.github.kr328.webapi.i18n;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class I18n {
    private static final ResourceBundle defaultLanguage = ResourceBundle.getBundle(I18n.class.getPackageName() + ".i18n");
    private static ThreadLocal<ResourceBundle> currentLanguage = new ThreadLocal<>();
    private static Hashtable<Locale, ResourceBundle> cache = new Hashtable<>();

    public static String get(String t) {
        return Optional.ofNullable(currentLanguage.get()).map(r -> r.getString(t)).orElseGet(() -> defaultLanguage.getString(t));
    }

    public static void setCurrentLanguage(String locale) {
        if (locale == null)
            currentLanguage.set(null);
        else
            currentLanguage.set(cache.computeIfAbsent(Locale.forLanguageTag(locale), o -> ResourceBundle.getBundle(I18n.class.getPackageName() + ".i18n", Locale.forLanguageTag(locale))));
    }

    public static Lazy lazy() {
        return new Lazy(currentLanguage.get());
    }

    public static class Lazy {
        private ResourceBundle resourceBundle;

        private Lazy(ResourceBundle resourceBundle) {
            this.resourceBundle = resourceBundle;
        }

        public String get(String text) {
            return Optional
                    .ofNullable(resourceBundle)
                    .map(r -> r.getString(text))
                    .orElseGet(() -> defaultLanguage.getString(text));
        }
    }
}

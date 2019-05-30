package com.github.kr328.webapi.utils;

import java.security.SecureRandom;

public class RandomUtils {
    private final static SecureRandom random = new SecureRandom();
    private final static String CHARACTER_SET = "abcdef0123456789";
    private final static int TOKEN_LENGTH = 32;

    public static String randomSecret() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < TOKEN_LENGTH; i++)
            builder.append(CHARACTER_SET.charAt(random.nextInt(CHARACTER_SET.length())));

        return builder.toString();
    }
}

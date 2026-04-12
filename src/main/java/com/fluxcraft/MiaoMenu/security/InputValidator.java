package com.fluxcraft.MiaoMenu.security;

import java.util.Locale;
import java.util.regex.Pattern;

public final class InputValidator {
    private static final int DEFAULT_MAX_LENGTH = 64;
    private static final int COMMAND_MAX_LENGTH = 256;
    private static final Pattern SAFE_TEXT = Pattern.compile("[A-Za-z0-9_:\\-./%{} ]+");
    private static final Pattern SAFE_MENU_NAME = Pattern.compile("[a-z0-9._-]+");

    private InputValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isSafeMenuName(String value) {
        return isWithinLimit(value, DEFAULT_MAX_LENGTH) && SAFE_MENU_NAME.matcher(value.toLowerCase(Locale.ROOT)).matches();
    }

    public static boolean isSafeCommandContent(String value) {
        return isWithinLimit(value, COMMAND_MAX_LENGTH)
                && SAFE_TEXT.matcher(value).matches()
                && !value.contains(";;")
                && !value.contains("&&")
                && !value.contains("||");
    }

    private static boolean isWithinLimit(String value, int maxLength) {
        return value != null && !value.isBlank() && value.length() <= maxLength;
    }
}

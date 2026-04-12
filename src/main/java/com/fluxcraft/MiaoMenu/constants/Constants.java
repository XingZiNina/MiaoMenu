package com.fluxcraft.MiaoMenu.constants;

public final class Constants {

    private Constants() {}

    public static class Config {
        public static final int INVENTORY_MAX_ROWS = 6;
        public static final int INVENTORY_MIN_ROWS = 1;
        public static final int INVENTORY_ROW_SIZE = 9;
        public static final int TITLE_MAX_LENGTH = 32;
        public static final int DEFAULT_MENU_ROWS = 3;
    }
    public static String stripLeadingSlash(String command) {
        if (command == null) return "";
        return command.startsWith("/") ? command.substring(1) : command;
    }
}
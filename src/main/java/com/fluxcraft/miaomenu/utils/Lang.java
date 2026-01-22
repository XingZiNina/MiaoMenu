package com.fluxcraft.miaomenu.utils;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

public final class Lang {
    private static Plugin plugin;

    public static void init(Plugin plugin) {
        Lang.plugin = plugin;
    }

    public static String get(String key) {
        if (plugin == null) return key;

        String message = plugin.getConfig().getString("messages." + key);

        if (message == null) {
            message = plugin.getConfig().getString(key);
        }

        if (message == null) {
            return "Missing translation: " + key;
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}

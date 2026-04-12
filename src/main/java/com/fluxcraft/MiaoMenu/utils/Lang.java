package com.fluxcraft.MiaoMenu.utils;

import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Lang {
    private static final LegacyComponentSerializer AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.legacySection();

    private static Plugin plugin;

    private Lang() {
    }

    public static void init(Plugin plugin) {
        Lang.plugin = plugin;
    }

    public static String get(String key) {
        if (plugin == null) {
            return key;
        }
        String message = plugin.getConfig().getString("messages." + key);
        if (message == null) {
            message = plugin.getConfig().getString(key);
        }
        if (message == null) {
            return key;
        }
        return SECTION.serialize(AMPERSAND.deserialize(message));
    }
}

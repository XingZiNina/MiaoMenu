package com.fluxcraft.MiaoMenu.utils;

import java.util.logging.Level;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class PlaceholderUtils {
    private static volatile boolean placeholderApiChecked = false;
    private static volatile boolean placeholderApiAvailable = false;

    private PlaceholderUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static boolean isPlaceholderApiAvailable() {
        if (!placeholderApiChecked) {
            placeholderApiAvailable = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
            placeholderApiChecked = true;
        }
        return placeholderApiAvailable;
    }

    public static String parse(Player player, String text, Plugin plugin) {
        if (text == null) {
            return "";
        }
        if (player != null && text.contains("%player")) {
            String playerName = player.getName();
            text = text.replace("%player_name%", playerName);
            text = text.replace("%player%", playerName);
        }
        text = text.replace('&', '§');
        if (player != null && isPlaceholderApiAvailable()) {
            try {
                text = PlaceholderAPI.setPlaceholders(player, text);
            } catch (RuntimeException e) {
                if (plugin.getLogger().isLoggable(Level.FINE)) {
                    plugin.getLogger().log(Level.FINE, plugin.getName() + " PlaceholderAPI parse failed", e);
                }
            }
        }
        return text;
    }
}

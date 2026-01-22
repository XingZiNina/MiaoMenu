package com.fluxcraft.miaomenu.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class PlaceholderUtils {

    private PlaceholderUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String parse(Player player, String text, Plugin plugin) {
        if (text == null) {
            return "";
        }

        if (player != null) {
            text = text.replace("%player_name%", player.getName());
            text = text.replace("%caonima%", player.getName());
            text = text.replace("%player%", player.getName());
        }

        text = text.replace('&', '§');

        if (player != null && plugin != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                text = PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
                if (plugin != null && plugin.getLogger().isLoggable(java.util.logging.Level.FINE)) {
                    plugin.getLogger().fine("PlaceholderAPI parse error: " + e.getMessage());
                }
            }
        }

        return text;
    }
}

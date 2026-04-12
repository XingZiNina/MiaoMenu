package com.fluxcraft.MiaoMenu.utils;

import java.util.logging.Level;

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
            text = text.replace("%player%", player.getName());
        }
        text = text.replace('&', '§');
        if (player != null && plugin != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
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

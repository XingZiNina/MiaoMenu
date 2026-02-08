package com.fluxcraft.miaomenu.bedrockmenu;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.utils.Lang;
import com.fluxcraft.miaomenu.utils.PlaceholderUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BedrockMenuManager {
    private final miaomenu plugin;
    private final Map<String, BedrockMenu> menus = new HashMap<>();

    public BedrockMenuManager(miaomenu plugin) {
        this.plugin = plugin;
    }

    public void loadAllMenus() {
        menus.clear();
        File dir = new File(plugin.getDataFolder(), "bedrock_menus");
        if (!dir.exists()) dir.mkdirs();

        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                String name = file.getName().replace(".yml", "");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                menus.put(name, new BedrockMenu(name, config, plugin));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load Bedrock menu: " + file.getName());
            }
        }
    }

    public void openMenu(Player player, String menuName) {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            player.sendMessage(Lang.get("message.players-only"));
            return;
        }

        BedrockMenu menu = menus.get(menuName);
        if (menu == null) {
            player.sendMessage(Lang.get("message.menu-not-found") + menuName);
            return;
        }

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), menu.buildForm(player).validResultHandler(response -> {
            int clickedIndex = response.clickedButtonId();
            if (clickedIndex >= 0 && clickedIndex < menu.getMenuItems().size()) {
                BedrockMenu.BedrockMenuItem item = menu.getMenuItems().get(clickedIndex);
                handleItemClick(player, item);
            }
        }));
    }
    private void handleItemClick(Player player, BedrockMenu.BedrockMenuItem item) {
        String rawCmd = item.getCommand();
        if (rawCmd == null || rawCmd.isEmpty()) {
            return;
        }
        String parsed = PlaceholderUtils.parse(player, rawCmd, plugin);
        parsed = parsed.replace("%player%", player.getName());
        String lowerCmd = parsed.toLowerCase();

        if (lowerCmd.startsWith("[player]")) {
            String command = parsed.substring(8).trim();
            player.performCommand(command);

        } else if (lowerCmd.startsWith("[console]")) {
            String command = parsed.substring(9).trim();
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            });

        } else if (lowerCmd.startsWith("[message]")) {
            String message = parsed.substring(9).trim();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

        } else if (lowerCmd.startsWith("[menu]")) {
            String menuName = parsed.substring(6).trim();
            openMenu(player, menuName);

        } else if (lowerCmd.startsWith("[close]")) {
            return;

        } else {
            player.performCommand(parsed);
        }
    }

    public Map<String, BedrockMenu> getMenus() { return menus; }
}

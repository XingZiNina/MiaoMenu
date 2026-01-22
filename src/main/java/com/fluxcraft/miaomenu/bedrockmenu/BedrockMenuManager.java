package com.fluxcraft.miaomenu.bedrockmenu;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.utils.Lang;
import com.fluxcraft.miaomenu.utils.PlaceholderUtils;
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
        String cmd = item.getCommand();
        if (cmd != null && !cmd.isEmpty()) {
            String parsed = PlaceholderUtils.parse(player, cmd, plugin);

            if (parsed.startsWith("[player]")) {
                player.performCommand(parsed.substring(8).trim());
            } else {
                player.performCommand(parsed);
            }
        }
    }

    public Map<String, BedrockMenu> getMenus() { return menus; }
}

package com.fluxcraft.miaomenu.javamenu;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.utils.Lang;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JavaMenuManager {
    private final miaomenu plugin;
    private final Map<String, JavaMenu> menus = new HashMap<>();

    public JavaMenuManager(miaomenu plugin) {
        this.plugin = plugin;
    }

    public void loadAllMenus() {
        menus.clear();
        File dir = new File(plugin.getDataFolder(), "java_menus");
        if (!dir.exists()) dir.mkdirs();

        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                String name = file.getName().replace(".yml", "");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                menus.put(name, new JavaMenu(name, config, plugin));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load menu: " + file.getName());
            }
        }
    }

    public void openMenu(Player player, String menuName) {
        JavaMenu menu = menus.get(menuName);
        if (menu == null) {
            player.sendMessage(Lang.get("message.menu-not-found") + menuName);
            return;
        }
        try {
            menu.open(player);
        } catch (Exception e) {
            player.sendMessage(Lang.get("message.open-error"));
            plugin.getLogger().severe("Error opening menu " + menuName);
        }
    }

    public Map<String, JavaMenu> getMenus() { return menus; }
}

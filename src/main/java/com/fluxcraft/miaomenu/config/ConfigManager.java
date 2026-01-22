package com.fluxcraft.miaomenu.config;

import com.fluxcraft.miaomenu.miaomenu;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigManager {
    private final miaomenu plugin;

    public ConfigManager(miaomenu plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        initDirectory("java_menus");
        initDirectory("bedrock_menus");
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    private void initDirectory(String dirName) {
        File dir = new File(plugin.getDataFolder(), dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}

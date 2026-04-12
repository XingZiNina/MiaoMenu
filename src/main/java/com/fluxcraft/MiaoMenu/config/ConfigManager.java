package com.fluxcraft.MiaoMenu.config;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import com.fluxcraft.MiaoMenu.MiaoMenu;
import com.fluxcraft.MiaoMenu.utils.Lang;

public class ConfigManager {
    private static final int CONFIG_VERSION = 12;
    private static final int MENU_VERSION = 3;
    private static final String JAVA_MENUS_DIR = "java_menus";
    private static final String BEDROCK_MENUS_DIR = "bedrock_menus";
    private static final String MENU_VERSION_KEY = "menu-version";
    private static final String FALLBACK_MATERIAL_KEY = "settings.item-resolver.fallback-material";

    private final MiaoMenu plugin;

    public ConfigManager(MiaoMenu plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (configFile.exists()) {
            FileConfiguration currentConfig = plugin.getConfig();
            int currentVersion = currentConfig.getInt("config-version", 0);
            if (currentVersion != CONFIG_VERSION) {
                File backupFile = new File(plugin.getDataFolder(), "config.yml.old");
                if (backupFile.exists() && !backupFile.delete()) {
                    plugin.getLogger().warning(Lang.get("log.config.backup-delete-failed").replace("{0}", backupFile.getName()));
                }
                if (!configFile.renameTo(backupFile)) {
                    plugin.getLogger().warning(Lang.get("log.config.backup-create-failed").replace("{0}", configFile.getName()));
                }
                plugin.saveResource("config.yml", true);
                plugin.getLogger().info(Lang.get("message.config-updated"));
            }
        } else {
            plugin.saveDefaultConfig();
        }
        plugin.reloadConfig();
        initDirectory(JAVA_MENUS_DIR);
        initDirectory(BEDROCK_MENUS_DIR);
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public Material getCraftEngineFallbackMaterial() {
        String materialName = getConfig().getString(FALLBACK_MATERIAL_KEY, "STONE");
        Material material = Material.matchMaterial(materialName);
        return material != null ? material : Material.STONE;
    }

    public void checkAndRefreshMenus() {
        FileConfiguration config = plugin.getConfig();
        int currentVersion = config.getInt(MENU_VERSION_KEY, 0);
        if (currentVersion != MENU_VERSION) {
            try {
                initDirectory(JAVA_MENUS_DIR);
                initDirectory(BEDROCK_MENUS_DIR);
                plugin.saveResource("bedrock_menus/test.yml", true);
                plugin.saveResource("java_menus/test.yml", true);
                config.set(MENU_VERSION_KEY, MENU_VERSION);
                config.save(new File(plugin.getDataFolder(), "config.yml"));
                plugin.reloadConfig();
                plugin.getLogger().info(Lang.get("message.reloaded"));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, Lang.get("log.config.menu-refresh-failed"), e);
            }
            return;
        }
        if (config.getBoolean("settings.auto-generate-examples", true)) {
            saveResourceIfNotExists("bedrock_menus/test.yml");
            saveResourceIfNotExists("java_menus/test.yml");
        }
    }

    private void saveResourceIfNotExists(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }
    }

    private void initDirectory(String dirName) {
        File dir = new File(plugin.getDataFolder(), dirName);
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().warning(Lang.get("message.io-error"));
        }
    }
}

package com.fluxcraft.dGeyserMenuFlux.config;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    private final DGeyserMenuFlux plugin;
    private FileConfiguration config;
    private final Map<String, FileConfiguration> javaMenus = new HashMap<>();
    private final Map<String, FileConfiguration> bedrockMenus = new HashMap<>();

    public ConfigManager(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        plugin.saveDefaultConfig();
        if (config == null) {
            plugin.reloadConfig();
        }
        this.config = plugin.getConfig();

        createMenuDirectories();
        loadAllMenuFiles();
    }

    private void createMenuDirectories() {
        File javaMenuDir = new File(plugin.getDataFolder(), "java_menus");
        File bedrockMenuDir = new File(plugin.getDataFolder(), "bedrock_menus");

        boolean javaDirCreated = false;
        boolean bedrockDirCreated = false;

        if (!javaMenuDir.exists()) {
            javaMenuDir.mkdirs();
            javaDirCreated = true;
        }

        if (!bedrockMenuDir.exists()) {
            bedrockMenuDir.mkdirs();
            bedrockDirCreated = true;
        }

        provideExampleMenus(javaMenuDir, bedrockMenuDir, javaDirCreated, bedrockDirCreated);
    }

    private void provideExampleMenus(File javaMenuDir, File bedrockMenuDir, boolean javaDirCreated, boolean bedrockDirCreated) {
        File[] javaFiles = javaMenuDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (javaFiles == null || javaFiles.length == 0) {
            saveExampleMenu("java_menus/example.yml", new File(javaMenuDir, "example.yml"));
            plugin.getLogger().info("Java菜单目录为空，已创建示例菜单");
        }

        File[] bedrockFiles = bedrockMenuDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (bedrockFiles == null || bedrockFiles.length == 0) {
            saveExampleMenu("bedrock_menus/main.yml", new File(bedrockMenuDir, "main.yml"));
            plugin.getLogger().info("基岩菜单目录为空，已创建示例菜单");
        }
    }

    private void saveExampleMenu(String resourcePath, File targetFile) {
        try {
            if (!targetFile.exists()) {
                InputStream inputStream = plugin.getResource(resourcePath);
                if (inputStream != null) {
                    Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().info("创建示例菜单: " + targetFile.getName());
                } else {
                    plugin.getLogger().warning("找不到资源文件: " + resourcePath);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "创建示例菜单失败: " + targetFile.getName(), e);
        }
    }

    private void loadAllMenuFiles() {
        javaMenus.clear();
        bedrockMenus.clear();

        loadMenuFiles(new File(plugin.getDataFolder(), "java_menus"), javaMenus, "Java");
        loadMenuFiles(new File(plugin.getDataFolder(), "bedrock_menus"), bedrockMenus, "基岩");

        plugin.getLogger().info("已加载 " + javaMenus.size() + " 个Java菜单和 " + bedrockMenus.size() + " 个基岩菜单");
    }

    private void loadMenuFiles(File menuDir, Map<String, FileConfiguration> menuMap, String menuType) {
        if (!menuDir.exists()) return;

        File[] files = menuDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String menuName = file.getName().replace(".yml", "");
            try {
                FileConfiguration menuConfig = YamlConfiguration.loadConfiguration(file);
                menuMap.put(menuName, menuConfig);
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("成功加载" + menuType + "菜单: " + menuName);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "加载" + menuType + "菜单失败: " + menuName, e);
            }
        }
    }

    public FileConfiguration getJavaMenu(String menuName) {
        return javaMenus.get(menuName);
    }

    public FileConfiguration getBedrockMenu(String menuName) {
        return bedrockMenus.get(menuName);
    }

    public void reloadAllMenus() {
        loadAllMenuFiles();
    }

    public boolean isHotReloadEnabled() {
        return config.getBoolean("settings.hot-reload.enabled", true);
    }

    public java.util.Set<String> getJavaMenuNames() {
        return javaMenus.keySet();
    }

    public java.util.Set<String> getBedrockMenuNames() {
        return bedrockMenus.keySet();
    }

    public java.util.Set<String> getAllMenuNames() {
        java.util.Set<String> allMenus = new java.util.HashSet<>();
        allMenus.addAll(javaMenus.keySet());
        allMenus.addAll(bedrockMenus.keySet());
        return allMenus;
    }
}
package com.fluxcraft.miaomenu.listeners;

import com.fluxcraft.miaomenu.miaomenu;
import java.io.File;

public class TestXiaFa {

    private final miaomenu plugin;

    public TestXiaFa(miaomenu plugin) {
        this.plugin = plugin;
    }
    public void distribute() {
        if (!plugin.getConfig().getBoolean("settings.auto-generate-examples", true)) {
            plugin.getLogger().info("配置已禁用示例文件自动生成");
            return;
        }
        saveResourceIfNotExists("bedrock_menus/test.yml");
        saveResourceIfNotExists("java_menus/test.yml");
    }
    private void saveResourceIfNotExists(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.saveResource(path, false);
            plugin.getLogger().info("已自动生成示例文件: " + path);
        }
    }
}

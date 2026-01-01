package com.fluxcraft.dGeyserMenuFlux.bedrockmenu;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class BedrockMenuManager {
    private final DGeyserMenuFlux plugin;
    private final Map<String, BedrockMenu> menus = new HashMap<>();

    public BedrockMenuManager(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    public void loadAllMenus() {
        menus.clear();

        File bedrockMenuDir = new File(plugin.getDataFolder(), "bedrock_menus");
        if (!bedrockMenuDir.exists()) {
            bedrockMenuDir.mkdirs();
        }

        File[] files = bedrockMenuDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            return;
        }

        int loadedCount = 0;
        int totalItems = 0;

        for (File file : files) {
            String menuName = file.getName().replace(".yml", "");
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                BedrockMenu menu = new BedrockMenu(menuName, config);
                menus.put(menuName, menu);
                loadedCount++;
                totalItems += menu.getMenuItemCount();

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "加载基岩菜单失败: " + menuName, e);
            }
        }

        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("基岩菜单加载完成: " + loadedCount + "/" + files.length +
                    " 个菜单, 总计 " + totalItems + " 个菜单项");
        }
    }

    public void openMenu(Player player, String menuName) {
        if (!isBedrockPlayer(player)) {
            return;
        }

        BedrockMenu menu = menus.get(menuName);
        if (menu == null) {
            player.sendMessage("§c菜单不存在: " + menuName);
            return;
        }

        if (!canOpenMenu(player, menuName)) {
            player.sendMessage("§c你没有权限打开这个菜单!");
            return;
        }

        try {
            menu.open(player);
        } catch (Exception e) {
            player.sendMessage("§c打开菜单时发生错误");
            plugin.getLogger().log(Level.SEVERE, "打开基岩菜单异常", e);
        }
    }

    private boolean isBedrockPlayer(Player player) {
        try {
            FloodgateApi api = FloodgateApi.getInstance();
            return api != null && api.isFloodgatePlayer(player.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean canOpenMenu(Player player, String menuName) {
        if (player.isOp() || player.hasPermission("dgeysermenu.admin") || player.hasPermission("dgeysermenu.*")) {
            return true;
        }
        String menuPermission = "dgeysermenu.menu." + menuName;
        return player.hasPermission(menuPermission) || player.hasPermission("dgeysermenu.use");
    }

    public void reloadMenus() {
        loadAllMenus();
    }

    public int getLoadedMenuCount() {
        return menus.size();
    }

    public boolean menuExists(String menuName) {
        return menus.containsKey(menuName);
    }

    public Map<String, BedrockMenu> getMenus() {
        return new HashMap<>(menus);
    }

    public Set<String> getMenuNames() {
        return menus.keySet();
    }
}

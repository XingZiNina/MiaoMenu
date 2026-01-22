package com.fluxcraft.miaomenu;

import com.fluxcraft.miaomenu.commands.CommandManager;
import com.fluxcraft.miaomenu.config.ConfigManager;
import com.fluxcraft.miaomenu.javamenu.JavaMenuListener;
import com.fluxcraft.miaomenu.javamenu.JavaMenuManager;
import com.fluxcraft.miaomenu.menu.action.ActionRegistry;
import com.fluxcraft.miaomenu.managers.HotReloadManager;
import com.fluxcraft.miaomenu.managers.MenuClockManager;
import com.fluxcraft.miaomenu.bedrockmenu.BedrockMenuListener;
import com.fluxcraft.miaomenu.bedrockmenu.BedrockMenuManager;
import com.fluxcraft.miaomenu.listeners.ClockInteractionListener;
import com.fluxcraft.miaomenu.listeners.PlayerLifecycleListener;
import com.fluxcraft.miaomenu.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class miaomenu extends JavaPlugin {

    private static final int CONFIG_VERSION = 1;

    private ConfigManager configManager;
    private JavaMenuManager javaMenuManager;
    private BedrockMenuManager bedrockMenuManager;
    private CommandManager commandManager;
    private ActionRegistry actionRegistry;
    private HotReloadManager hotReloadManager;
    private MenuClockManager clockManager;
    private NamespacedKey clockKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Lang.init(this);

        checkAndRefreshConfig();

        this.clockKey = new NamespacedKey(this, "menu_clock");

        this.configManager = new ConfigManager(this);
        this.javaMenuManager = new JavaMenuManager(this);
        this.bedrockMenuManager = new BedrockMenuManager(this);

        this.actionRegistry = new ActionRegistry(this, javaMenuManager);
        this.commandManager = new CommandManager(this);
        this.clockManager = new MenuClockManager(this, clockKey);
        this.hotReloadManager = new HotReloadManager(this);

        configManager.loadConfig();
        javaMenuManager.loadAllMenus();
        bedrockMenuManager.loadAllMenus();

        getServer().getPluginManager().registerEvents(new JavaMenuListener(this, actionRegistry), this);
        getServer().getPluginManager().registerEvents(new BedrockMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new ClockInteractionListener(clockManager), this);
        getServer().getPluginManager().registerEvents(new PlayerLifecycleListener(this, clockManager), this);

        if (getCommand("dgeysermenu") != null) {
            getCommand("dgeysermenu").setExecutor(commandManager);
            getCommand("dgeysermenu").setTabCompleter(commandManager);
        }

        if (configManager.getConfig().getBoolean("settings.hot-reload.enabled", true)) {
            hotReloadManager.initialize();
        }

        initializeBStats();
        getLogger().info("MiaoMenu v" + getDescription().getVersion() + " Enabled.");
    }

    @Override
    public void onDisable() {
        hotReloadManager.shutdown();
    }

    private void checkAndRefreshConfig() {
        int currentVersion = getConfig().getInt("config-version", 0);

        if (currentVersion < CONFIG_VERSION) {
            String warningMsg = Lang.get("message.config-update-warning");
            getLogger().warning(warningMsg);

            saveResource("config.yml", true);

            reloadConfig();
            Lang.init(this);

            getLogger().info(Lang.get("message.config-updated"));
        }
    }

    private void initializeBStats() {
        try {
            Class<?> metricsClass = Class.forName("com.fluxcraft.miaomenu.libs.bstats.bukkit.Metrics");
            Object metrics = metricsClass.getConstructor(JavaPlugin.class, int.class).newInstance(this,28979);

            metricsClass.getMethod("addCustomChart", Class.forName("com.fluxcraft.miaomenu.libs.bstats.charts.SimplePie"))
                    .invoke(metrics, Class.forName("com.fluxcraft.miaomenu.libs.bstats.charts.SimplePie")
                            .getConstructor(String.class, java.util.concurrent.Callable.class)
                            .newInstance("server_software", (java.util.concurrent.Callable<String>) () -> {
                                String version = Bukkit.getVersion();
                                String name = "Unknown";
                                if (version.contains("Paper")) name = "Paper";
                                else if (version.contains("Spigot")) name = "Spigot";
                                else if (version.contains("Purpur")) name = "Purpur";
                                else if (version.contains("Leaves")) name = "Leaves";
                                else if (version.contains("Lumina")) name = "Lumina";
                                else if (version.contains(" ")) name = "Other";
                                return name;
                            }));

            metricsClass.getMethod("addCustomChart", Class.forName("com.fluxcraft.miaomenu.libs.bstats.charts.SimplePie"))
                    .invoke(metrics, Class.forName("com.fluxcraft.miaomenu.libs.bstats.charts.SimplePie")
                            .getConstructor(String.class, java.util.concurrent.Callable.class)
                            .newInstance("minecraft_version", (java.util.concurrent.Callable<String>) () -> {
                                String v = Bukkit.getBukkitVersion().split("-")[0];
                                return v.substring(0, Math.min(4, v.length()));
                            }));
        } catch (Exception e) {
            if (getConfig().getBoolean("settings.debug")) {
                getLogger().warning("BStats init error: " + e.getMessage());
            }
        }
    }

    public ConfigManager getConfigManager() { return configManager; }
    public JavaMenuManager getJavaMenuManager() { return javaMenuManager; }
    public BedrockMenuManager getBedrockMenuManager() { return bedrockMenuManager; }
}

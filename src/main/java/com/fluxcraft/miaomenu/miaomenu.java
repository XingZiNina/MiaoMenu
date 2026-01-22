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
import java.util.List;

public final class miaomenu extends JavaPlugin {

    private static final int CONFIG_VERSION = 1;
    private static final int BSTATS_ID = 28979;
    public static final int JOIN_DELAY_TICKS = 20;
    private static final String BSTATS_METRICS_CLASS = "com.fluxcraft.miaomenu.libs.bstats.bukkit.Metrics";
    private static final String BSTATS_SIMPLE_PIE_CLASS = "com.fluxcraft.miaomenu.libs.bstats.charts.SimplePie";

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
        if (hotReloadManager != null) {
            hotReloadManager.shutdown();
        }
    }

    private void checkAndRefreshConfig() {
        int currentVersion = getConfig().getInt("config-version", 0);

        if (currentVersion < CONFIG_VERSION) {
            getLogger().warning(Lang.get("message.config-update-warning"));

            saveResource("config.yml", true);

            reloadConfig();
            Lang.init(this);

            getLogger().info(Lang.get("message.config-updated"));
        }
    }

    private void initializeBStats() {
        try {
            Class<?> metricsClass = Class.forName(BSTATS_METRICS_CLASS);
            Object metrics = metricsClass.getConstructor(JavaPlugin.class, int.class).newInstance(this, BSTATS_ID);

            addCustomChart(metrics, metricsClass, "server_software", this::detectServerSoftware);
            addCustomChart(metrics, metricsClass, "minecraft_version", this::detectMinecraftVersion);

        } catch (Exception e) {
            if (getConfig().getBoolean("settings.debug")) {
                getLogger().warning("BStats initialization failed: " + e.getMessage());
            }
        }
    }

    private void addCustomChart(Object metrics, Class<?> metricsClass, String chartId, java.util.concurrent.Callable<String> callable) throws Exception {
        Class<?> simplePieClass = Class.forName(BSTATS_SIMPLE_PIE_CLASS);
        Object simplePie = simplePieClass.getConstructor(String.class, java.util.concurrent.Callable.class).newInstance(chartId, callable);
        metricsClass.getMethod("addCustomChart", simplePieClass).invoke(metrics, simplePie);
    }

    private String detectServerSoftware() {
        String version = Bukkit.getVersion();
        List<String> knownTypes = List.of("Paper", "Spigot", "Purpur", "Leaves", "Lumina");

        for (String type : knownTypes) {
            if (version.contains(type)) {
                return type;
            }
        }
        return "Other";
    }

    private String detectMinecraftVersion() {
        String version = Bukkit.getBukkitVersion().split("-")[0];
        int maxLength = Math.min(4, version.length());
        return version.substring(0, maxLength);
    }

    public ConfigManager getConfigManager() { return configManager; }
    public JavaMenuManager getJavaMenuManager() { return javaMenuManager; }
    public BedrockMenuManager getBedrockMenuManager() { return bedrockMenuManager; }
}

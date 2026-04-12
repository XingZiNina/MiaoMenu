package com.fluxcraft.MiaoMenu;

import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.fluxcraft.MiaoMenu.bedrockmenu.BedrockMenuManager;
import com.fluxcraft.MiaoMenu.commands.CommandManager;
import com.fluxcraft.MiaoMenu.config.ConfigManager;
import com.fluxcraft.MiaoMenu.foliacall.FoliaFactory;
import com.fluxcraft.MiaoMenu.integration.ItemResolver;
import com.fluxcraft.MiaoMenu.javamenu.JavaMenuListener;
import com.fluxcraft.MiaoMenu.javamenu.JavaMenuManager;
import com.fluxcraft.MiaoMenu.listeners.ClockInteractionListener;
import com.fluxcraft.MiaoMenu.listeners.PlayerLifecycleListener;
import com.fluxcraft.MiaoMenu.listeners.PlayerLifecycleListener_Folia;
import com.fluxcraft.MiaoMenu.managers.HotReloadManager;
import com.fluxcraft.MiaoMenu.managers.MenuClockManager;
import com.fluxcraft.MiaoMenu.managers.SoundsClock;
import com.fluxcraft.MiaoMenu.menu.action.ActionRegistry;
import com.fluxcraft.MiaoMenu.menu.requirement.RequirementFeedbackHandler;
import com.fluxcraft.MiaoMenu.menu.requirement.RequirementService;
import com.fluxcraft.MiaoMenu.proxy.ProxyManager;
import com.fluxcraft.MiaoMenu.security.RateLimiter;
import com.fluxcraft.MiaoMenu.utils.Lang;

import cn.handyplus.lib.adapter.HandySchedulerUtil;

public final class MiaoMenu extends JavaPlugin {
    private static final int BSTATS_ID = 28979;
    public static final int JOIN_DELAY_TICKS = 20;
    private static final String MAIN_COMMAND = "dgeysermenu";

    private ConfigManager configManager;
    private JavaMenuManager javaMenuManager;
    private BedrockMenuManager bedrockMenuManager;
    private CommandManager commandManager;
    private HotReloadManager hotReloadManager;
    private ProxyManager proxyManager;
    private RequirementService requirementService;
    private RateLimiter interactionRateLimiter;
    private Class<?> floodgateApiClass;
    private Object floodgateApiInstance;

    @Override
    public void onEnable() {
        try {
            initializeCoreServices();
            initializeMenuSystems();
            registerListeners();
            registerCommands();
            initializeOptionalFeatures();
            getLogger().info(Lang.get("log.plugin.enabled").replace("{0}", getPluginMeta().getVersion()));
        } catch (RuntimeException e) {
            getLogger().log(Level.SEVERE, Lang.get("log.plugin.enable-failed"), e);
            throw e;
        }
    }

    @Override
    public void onDisable() {
        if (hotReloadManager != null) {
            hotReloadManager.shutdown();
        }
        if (interactionRateLimiter != null) {
            interactionRateLimiter.clearAll();
        }
        if (proxyManager != null) {
            if (getServer().getMessenger().isIncomingChannelRegistered(this, "BungeeCord")) {
                getServer().getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
            }
            if (getServer().getMessenger().isOutgoingChannelRegistered(this, "BungeeCord")) {
                getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
            }
            if (getServer().getMessenger().isOutgoingChannelRegistered(this, "velocity:player")) {
                getServer().getMessenger().unregisterOutgoingPluginChannel(this, "velocity:player");
            }
        }
    }

    private void initializeCoreServices() {
        saveDefaultConfig();
        Lang.init(this);
        HandySchedulerUtil.init(this);
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        configManager.checkAndRefreshMenus();
        requirementService = new RequirementService(this);
        interactionRateLimiter = new RateLimiter(java.time.Duration.ofSeconds(2), 10);
    }

    private void initializeMenuSystems() {
        NamespacedKey clockKey = new NamespacedKey(this, "menu_clock");
        RequirementFeedbackHandler requirementFeedbackHandler = new RequirementFeedbackHandler(this);
        ItemResolver itemResolver = new ItemResolver(this, configManager.getCraftEngineFallbackMaterial());
        SoundsClock soundsClock = new SoundsClock(this);
        ActionRegistry actionRegistry = new ActionRegistry(this);
        javaMenuManager = new JavaMenuManager(this, itemResolver, soundsClock, requirementService, requirementFeedbackHandler);
        bedrockMenuManager = new BedrockMenuManager(this, actionRegistry, soundsClock, requirementService, requirementFeedbackHandler);
        commandManager = new CommandManager(this);
        MenuClockManager clockManager = new MenuClockManager(this, clockKey);
        hotReloadManager = new HotReloadManager(this);
        proxyManager = new ProxyManager(this);
        proxyManager.initialize();
        javaMenuManager.loadAllMenus();
        bedrockMenuManager.loadAllMenus();
        registerClockListeners(clockManager);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new JavaMenuListener(this, bedrockMenuManager.getActionRegistry()), this);
    }

    private void registerClockListeners(MenuClockManager clockManager) {
        getServer().getPluginManager().registerEvents(new ClockInteractionListener(clockManager), this);
        if (FoliaFactory.isFolia()) {
            getServer().getPluginManager().registerEvents(new PlayerLifecycleListener_Folia(this, clockManager), this);
            return;
        }
        getServer().getPluginManager().registerEvents(new PlayerLifecycleListener(this, clockManager), this);
    }

    private void initializeOptionalFeatures() {
        if (configManager.getConfig().getBoolean("settings.hot-reload.enabled", true)) {
            initializeHotReload();
        }
        initializeBStats();
    }

    private void initializeHotReload() {
        try {
            hotReloadManager.initialize();
        } catch (java.io.IOException | IllegalStateException | SecurityException e) {
            getLogger().log(Level.SEVERE, Lang.get("log.hot-reload.initialize-failed"), e);
        }
    }

    private void registerCommands() {
        PluginCommand command = getCommand(MAIN_COMMAND);
        if (command != null) {
            command.setExecutor(commandManager);
            command.setTabCompleter(commandManager);
            return;
        }
        getLogger().severe(Lang.get("log.command.register-failed").replace("{0}", MAIN_COMMAND));
    }

    public void openSmartMenu(Player player, String menuName) {
        if (isBedrockPlayer(player)) {
            bedrockMenuManager.openMenu(player, menuName);
            return;
        }
        javaMenuManager.openMenu(player, menuName);
    }

    private boolean isBedrockPlayer(Player player) {
        if (Bukkit.getPluginManager().getPlugin("floodgate") == null) {
            return false;
        }
        try {
            if (floodgateApiClass == null) {
                floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            }
            if (floodgateApiInstance == null) {
                floodgateApiInstance = floodgateApiClass.getMethod("getInstance").invoke(null);
            }
            return (Boolean) floodgateApiClass.getMethod("isFloodgatePlayer", UUID.class)
                    .invoke(floodgateApiInstance, player.getUniqueId());
        } catch (ReflectiveOperationException e) {
            getLogger().log(Level.WARNING, Lang.get("log.floodgate.player-check-failed").replace("{0}", player.getName()), e);
            return false;
        }
    }

    private void initializeBStats() {
        try {
            Metrics metrics = new Metrics(this, BSTATS_ID);
            metrics.addCustomChart(new SimplePie("server_software", this::detectServerSoftware));
            metrics.addCustomChart(new SimplePie("minecraft_version", this::detectMinecraftVersion));
        } catch (RuntimeException e) {
            getLogger().log(Level.WARNING, Lang.get("log.bstats.initialize-failed"), e);
        }
    }

    private String detectServerSoftware() {
        String version = Bukkit.getVersion();
        return Stream.of("Paper", "Spigot", "Purpur", "Mint", "Leaves", "Leaf", "Luminol", "Folia")
                .filter(version::contains)
                .findFirst()
                .orElse("Other");
    }

    private String detectMinecraftVersion() {
        String version = Bukkit.getBukkitVersion().split("-")[0];
        int maxLength = Math.min(4, version.length());
        return version.substring(0, maxLength);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public JavaMenuManager getJavaMenuManager() {
        return javaMenuManager;
    }

    public BedrockMenuManager getBedrockMenuManager() {
        return bedrockMenuManager;
    }

    public ProxyManager getProxyManager() {
        return proxyManager;
    }

    public RequirementService getRequirementService() {
        return requirementService;
    }

    public RateLimiter getInteractionRateLimiter() {
        return interactionRateLimiter;
    }
}

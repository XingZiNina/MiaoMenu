package com.fluxcraft.MiaoMenu.managers;

import com.fluxcraft.MiaoMenu.MiaoMenu;
import com.fluxcraft.MiaoMenu.foliacall.FoliaFactory;
import com.fluxcraft.MiaoMenu.utils.Lang;
import org.bukkit.Bukkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HotReloadManager {
    private final MiaoMenu plugin;
    private WatchService watchService;
    private final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    private Thread watcherThread;
    private static final String JAVA_MENU_DIR = "java_menus";
    private static final String BEDROCK_MENU_DIR = "bedrock_menus";
    private static final String CONFIG_FILE = "config.yml";
    private static final String FILE_EXTENSION = ".yml";
    private static final long DEBOUNCE_DELAY_MS = 500;
    private long lastConfigReloadTime = 0;
    private final Map<String, Long> lastMenuReloadTimes = new ConcurrentHashMap<>();
    public HotReloadManager(MiaoMenu plugin) {
        this.plugin = plugin;
    }
    public void initialize() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        this.running = true;
        registerDirectory(plugin.getDataFolder().toPath());
        registerDirectory(new File(plugin.getDataFolder(), JAVA_MENU_DIR).toPath());
        registerDirectory(new File(plugin.getDataFolder(), BEDROCK_MENU_DIR).toPath());
        plugin.getLogger().info(Lang.get("hot-reload.initialized"));
        startWatcher();
    }
    private void registerDirectory(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                plugin.getLogger().warning(Lang.get("message.io-error"));
            }
        }
        try {
            WatchKey key = dir.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            keys.put(key, dir);
        } catch (IOException e) {
            plugin.getLogger().warning(Lang.get("message.io-error"));
        }
    }
    @SuppressWarnings("unchecked")
    private void startWatcher() {
        watcherThread = new Thread(() -> {
            while (running) {
                try {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) continue;
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();
                        Path dir = keys.get(key);
                        if (dir == null) continue;
                        if (filename.toString().equals(CONFIG_FILE) && dir.equals(plugin.getDataFolder().toPath())) {
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - lastConfigReloadTime < DEBOUNCE_DELAY_MS) continue;
                            lastConfigReloadTime = currentTime;
                            scheduleReload(() -> {
                                plugin.getConfigManager().loadConfig();
                                plugin.getConfigManager().checkAndRefreshMenus();
                                plugin.getJavaMenuManager().loadAllMenus();
                                plugin.getBedrockMenuManager().loadAllMenus();
                            });
                        } else if (filename.toString().endsWith(FILE_EXTENSION)) {
                            String fileName = filename.toString();
                            long currentTime = System.currentTimeMillis();
                            Long lastReload = lastMenuReloadTimes.get(fileName);
                            if (lastReload != null && currentTime - lastReload < DEBOUNCE_DELAY_MS) continue;
                            lastMenuReloadTimes.put(fileName, currentTime);
                            String logMsg = Lang.get("hot-reload.detected").replace("{0}", fileName);
                            plugin.getLogger().info(logMsg);
                            scheduleReload(() -> {
                                plugin.getJavaMenuManager().loadAllMenus();
                                plugin.getBedrockMenuManager().loadAllMenus();
                            });
                        }
                    }
                    if (!key.reset()) {
                        keys.remove(key);
                        if (keys.isEmpty()) break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (ClosedWatchServiceException e) {
                    break;
                } catch (Exception e) {
                    plugin.getLogger().warning(Lang.get("message.io-error"));
                }
            }
        }, "MiaoMenu-HotReload-Thread");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }
    private void scheduleReload(Runnable task) {
        if (FoliaFactory.isFolia()) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), 10L);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, 10L);
        }
    }
    public void shutdown() {
        running = false;
        if (watcherThread != null) {
            watcherThread.interrupt();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {
            }
        }
    }
}
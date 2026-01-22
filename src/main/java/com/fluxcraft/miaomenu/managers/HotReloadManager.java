package com.fluxcraft.miaomenu.managers;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.utils.Lang;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class HotReloadManager {
    private final miaomenu plugin;
    private WatchService watchService;
    private Map<WatchKey, Path> keys;
    private volatile boolean running = false;

    public HotReloadManager(miaomenu plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            keys = new HashMap<>();
            this.running = true;

            registerDirectory(new File(plugin.getDataFolder(), "java_menus").toPath());
            registerDirectory(new File(plugin.getDataFolder(), "bedrock_menus").toPath());

            plugin.getLogger().info(Lang.get("hot-reload.initialized"));
            startWatcher();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize HotReload: " + e.getMessage());
        }
    }

    private void registerDirectory(Path dir) {
        if (!Files.exists(dir)) return;
        try {
            WatchKey key = dir.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            keys.put(key, dir);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not watch directory " + dir + ": " + e.getMessage());
        }
    }

    private void startWatcher() {
        Thread t = new Thread(() -> {
            while (running) {
                try {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        if (filename.toString().endsWith(".yml")) {
                            String logMsg = Lang.get("hot-reload.detected").replace("{0}", filename.toString());
                            plugin.getLogger().info(logMsg);

                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                plugin.getJavaMenuManager().loadAllMenus();
                                plugin.getBedrockMenuManager().loadAllMenus();
                            }, 10L);
                        }
                    }
                    if (!key.reset()) break;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "DGeyserMenu-HotReload-Thread");
        t.setDaemon(true);
        t.start();
    }

    public void shutdown() {
        running = false;
        try {
            if (watchService != null) watchService.close();
        } catch (Exception ignored) {}
    }
}

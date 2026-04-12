package com.fluxcraft.MiaoMenu.foliacall;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class FoliaFactory {
    private static final boolean IS_FOLIA;
    private static final FoliaAdapter ADAPTER;
    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        IS_FOLIA = folia;
        ADAPTER = folia ? new FoliaSchedulerAdapter() : new BukkitSchedulerAdapter();
    }
    public static FoliaAdapter getAdapter() {
        return ADAPTER;
    }
    public static boolean isFolia() {
        return IS_FOLIA;
    }
    private static final class BukkitSchedulerAdapter implements FoliaAdapter {
        private final BukkitScheduler scheduler = org.bukkit.Bukkit.getScheduler();
        @Override
        public void runTaskLaterForEntity(Plugin plugin, Entity entity, Runnable task, long delay) {
            scheduler.runTaskLater(plugin, task, delay);
        }
    }
    private static final class FoliaSchedulerAdapter implements FoliaAdapter {
        @Override
        public void runTaskLaterForEntity(Plugin plugin, Entity entity, Runnable task, long delay) {
            entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, delay);
        }
    }
}

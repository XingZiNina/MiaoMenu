package com.fluxcraft.MiaoMenu.foliacall;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public interface FoliaAdapter {
    void runTaskLaterForEntity(Plugin plugin, Entity entity, Runnable task, long delay);
}

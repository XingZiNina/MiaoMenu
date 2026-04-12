package com.fluxcraft.MiaoMenu.listeners;

import com.fluxcraft.MiaoMenu.MiaoMenu;
import com.fluxcraft.MiaoMenu.foliacall.FoliaFactory;
import com.fluxcraft.MiaoMenu.managers.MenuClockManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerLifecycleListener_Folia implements Listener {
    private final MiaoMenu plugin;
    private final MenuClockManager clockManager;
    public PlayerLifecycleListener_Folia(MiaoMenu plugin, MenuClockManager clockManager) {
        this.plugin = plugin;
        this.clockManager = clockManager;
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.getConfig().getBoolean("settings.menu-clock.give-on-join", true)) {
            Player player = event.getPlayer();
            FoliaFactory.getAdapter().runTaskLaterForEntity(plugin, player, () -> {
                if (player.isOnline()) {
                    clockManager.giveClockToPlayer(player);
                }
            }, MiaoMenu.JOIN_DELAY_TICKS);
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        if (plugin.getConfig().getBoolean("settings.menu-clock.enabled", true)) {
            Player player = event.getPlayer();
            FoliaFactory.getAdapter().runTaskLaterForEntity(plugin, player, () -> {
                if (player.isOnline()) {
                    clockManager.giveClockToPlayer(player);
                }
            }, 45L);
        }
    }
}

package com.fluxcraft.miaomenu.listeners;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.managers.MenuClockManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerLifecycleListener implements Listener {
    private final miaomenu plugin;
    private final MenuClockManager clockManager;

    public PlayerLifecycleListener(miaomenu plugin, MenuClockManager clockManager) {
        this.plugin = plugin;
        this.clockManager = clockManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfig().getBoolean("settings.menu-clock.give-on-join", true)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    clockManager.giveClockToPlayer(player);
                }
            }, miaomenu.JOIN_DELAY_TICKS);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        clockManager.removeClockFromDrops(event.getEntity(), event.getDrops());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        clockManager.ensureClock(event.getPlayer());
    }
}

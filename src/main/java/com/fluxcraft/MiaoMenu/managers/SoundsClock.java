package com.fluxcraft.MiaoMenu.managers;

import com.fluxcraft.MiaoMenu.MiaoMenu;
import org.bukkit.entity.Player;

public class SoundsClock {
    private final MiaoMenu plugin;
    public SoundsClock(MiaoMenu plugin) {
        this.plugin = plugin;
    }
    public void playMenuOpenSound(Player player) {
        if (!plugin.getConfig().getBoolean("settings.open-menu-sound.enabled", false)) {
            return;
        }
        String soundName = plugin.getConfig().getString("settings.open-menu-sound.sound", "entity.experience_orb.pickup");
        float volume = (float) plugin.getConfig().getDouble("settings.open-menu-sound.volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble("settings.open-menu-sound.pitch", 1.0);
        player.getScheduler().run(plugin, task -> player.playSound(player.getLocation(), soundName, org.bukkit.SoundCategory.MASTER, volume, pitch), null);
    }
}
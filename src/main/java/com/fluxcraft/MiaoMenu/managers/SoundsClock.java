package com.fluxcraft.MiaoMenu.managers;

import com.fluxcraft.MiaoMenu.MiaoMenu;
import org.bukkit.entity.Player;

public class SoundsClock {
    private final MiaoMenu plugin;
    public SoundsClock(MiaoMenu plugin) {
        this.plugin = plugin;
    }
    public void playMenuOpenSound(Player player) {
        var config = plugin.getConfig();
        if (!config.getBoolean("settings.open-menu-sound.enabled", false)) {
            return;
        }
        String soundName = config.getString("settings.open-menu-sound.sound", "entity.experience_orb.pickup");
        float volume = (float) config.getDouble("settings.open-menu-sound.volume", 1.0);
        float pitch = (float) config.getDouble("settings.open-menu-sound.pitch", 1.0);
        player.getScheduler().run(plugin, _ -> player.playSound(player.getLocation(), soundName, org.bukkit.SoundCategory.MASTER, volume, pitch), null);
    }
}
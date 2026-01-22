package com.fluxcraft.miaomenu.menu.action;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface MenuAction {
    void execute(Player player, String content, Plugin plugin);
}

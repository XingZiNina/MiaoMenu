package com.fluxcraft.miaomenu.menu.action.impl;

import com.fluxcraft.miaomenu.menu.action.MenuAction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlayerCommandAction implements MenuAction {
    @Override
    public void execute(Player player, String content, Plugin plugin) {
        String cmd = content.startsWith("/") ? content.substring(1) : content;
        player.performCommand(cmd);
    }
}

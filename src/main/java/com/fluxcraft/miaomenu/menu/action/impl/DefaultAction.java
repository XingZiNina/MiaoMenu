package com.fluxcraft.miaomenu.menu.action.impl;

import com.fluxcraft.miaomenu.menu.action.MenuAction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DefaultAction implements MenuAction {
    private final PlayerCommandAction playerCommandAction = new PlayerCommandAction();

    @Override
    public void execute(Player player, String content, Plugin plugin) {
        playerCommandAction.execute(player, content, plugin);
    }
}

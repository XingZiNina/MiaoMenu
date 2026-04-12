package com.fluxcraft.MiaoMenu.menu.action.impl;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DefaultAction implements com.fluxcraft.MiaoMenu.menu.action.MenuAction {
    private final PlayerAction playerAction = new PlayerAction();
    @Override
    public void execute(Player player, String content, Plugin plugin) {
        playerAction.execute(player, content, plugin);
    }
}
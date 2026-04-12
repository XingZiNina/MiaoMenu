package com.fluxcraft.MiaoMenu.menu.action.impl;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CloseAction implements com.fluxcraft.MiaoMenu.menu.action.MenuAction {
    @Override
    public void execute(Player player, String content, Plugin plugin) {
        player.closeInventory();
    }
}

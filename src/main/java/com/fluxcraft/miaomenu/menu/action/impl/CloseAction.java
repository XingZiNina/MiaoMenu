package com.fluxcraft.miaomenu.menu.action.impl;

import com.fluxcraft.miaomenu.menu.action.MenuAction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CloseAction implements MenuAction {
    @Override
    public void execute(Player player, String content, Plugin plugin) {
        player.closeInventory();
    }
}

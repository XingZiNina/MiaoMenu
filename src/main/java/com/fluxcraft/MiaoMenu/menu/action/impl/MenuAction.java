package com.fluxcraft.MiaoMenu.menu.action.impl;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.fluxcraft.MiaoMenu.MiaoMenu;

public class MenuAction implements com.fluxcraft.MiaoMenu.menu.action.MenuAction {
    @Override
    public void execute(Player player, String content, Plugin plugin) {
        if (content == null || content.isEmpty()) return;
        String menuName = content.trim();
        if (plugin instanceof MiaoMenu miaoMenu) {
            miaoMenu.openSmartMenu(player, menuName);
        }
    }
}

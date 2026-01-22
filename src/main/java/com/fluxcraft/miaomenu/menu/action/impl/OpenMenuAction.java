package com.fluxcraft.miaomenu.menu.action.impl;

import com.fluxcraft.miaomenu.javamenu.JavaMenuManager;
import com.fluxcraft.miaomenu.menu.action.MenuAction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class OpenMenuAction implements MenuAction {
    private final JavaMenuManager menuManager;

    public OpenMenuAction(JavaMenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(Player player, String content, Plugin plugin) {
        menuManager.openMenu(player, content);
    }
}

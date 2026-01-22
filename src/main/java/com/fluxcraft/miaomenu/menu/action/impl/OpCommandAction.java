package com.fluxcraft.miaomenu.menu.action.impl;

import com.fluxcraft.miaomenu.menu.action.MenuAction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class OpCommandAction implements MenuAction {
    @Override
    public void execute(Player player, String content, Plugin plugin) {
        String cmd = content.startsWith("/") ? content.substring(1) : content;
        boolean wasOp = player.isOp();
        try {
            player.setOp(true);
            player.performCommand(cmd);
        } finally {
            player.setOp(wasOp);
        }
    }
}

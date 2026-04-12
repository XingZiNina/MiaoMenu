package com.fluxcraft.MiaoMenu.menu.action.impl;

import com.fluxcraft.MiaoMenu.constants.Constants;
import com.fluxcraft.MiaoMenu.foliacall.FoliaFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CmdAction implements com.fluxcraft.MiaoMenu.menu.action.MenuAction {
    @Override
    public void execute(Player player, String content, Plugin plugin) {
        if (content == null || content.isEmpty()) return;
        String cmd = Constants.stripLeadingSlash(content);
        if (FoliaFactory.isFolia()) {
            Bukkit.getGlobalRegionScheduler().execute(plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
            );
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }
}
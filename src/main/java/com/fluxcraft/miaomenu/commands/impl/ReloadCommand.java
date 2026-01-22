package com.fluxcraft.miaomenu.commands.impl;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.commands.PluginCommand;
import com.fluxcraft.miaomenu.utils.Lang;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements PluginCommand {
    private final miaomenu plugin;

    public ReloadCommand(miaomenu plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dgeysermenu.reload")) {
            sender.sendMessage(Lang.get("message.no-permission"));
            return;
        }
        plugin.getConfigManager().loadConfig();
        plugin.getJavaMenuManager().loadAllMenus();
        plugin.getBedrockMenuManager().loadAllMenus();

        sender.sendMessage(Lang.get("message.reloaded"));
    }
}

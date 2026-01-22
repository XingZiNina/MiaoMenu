package com.fluxcraft.miaomenu.commands.impl;

import com.fluxcraft.miaomenu.commands.PluginCommand;
import com.fluxcraft.miaomenu.utils.Lang;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpCommand implements PluginCommand {
    private final List<String> subCommands;

    public HelpCommand(List<String> subCommands) {
        this.subCommands = subCommands;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(Lang.get("message.help.header"));
        sender.sendMessage("§f可用子命令: " + String.join("§7, §f", subCommands));
        sender.sendMessage(Lang.get("message.help.usage"));
    }
}

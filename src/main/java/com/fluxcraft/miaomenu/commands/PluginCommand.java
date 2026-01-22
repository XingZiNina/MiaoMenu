package com.fluxcraft.miaomenu.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface PluginCommand {
    void execute(CommandSender sender, String[] args);

    default List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}

package com.fluxcraft.MiaoMenu.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PluginCommand {
    void execute(CommandSender sender, String[] args);

    default @Nullable List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}

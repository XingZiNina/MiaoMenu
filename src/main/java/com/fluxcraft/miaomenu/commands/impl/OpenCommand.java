package com.fluxcraft.miaomenu.commands.impl;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.commands.PluginCommand;
import com.fluxcraft.miaomenu.utils.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class OpenCommand implements PluginCommand {
    private final miaomenu plugin;

    public OpenCommand(miaomenu plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dgeysermenu.use")) {
            sender.sendMessage(Lang.get("message.no-permission"));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(Lang.get("message.usage-open"));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.get("message.players-only"));
            return;
        }

        Player player = (Player) sender;
        plugin.getJavaMenuManager().openMenu(player, args[0]);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return plugin.getJavaMenuManager().getMenus().keySet().stream()
                .filter(s -> s.startsWith(args[0]))
                .collect(Collectors.toList());
    }
}

package com.fluxcraft.miaomenu.commands;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.commands.impl.*;
import com.fluxcraft.miaomenu.utils.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final Map<String, PluginCommand> commands = new HashMap<>();

    public CommandManager(miaomenu plugin) {
        register("open", new OpenCommand(plugin));
        register("reload", new ReloadCommand(plugin));
        register("help", new HelpCommand(List.copyOf(commands.keySet())));
    }

    private void register(String name, PluginCommand command) {
        commands.put(name.toLowerCase(), command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            commands.get("help").execute(sender, new String[0]);
            return true;
        }

        PluginCommand target = commands.get(args[0].toLowerCase());
        if (target == null) {
            sender.sendMessage(Lang.get("command.unknown"));
            commands.get("help").execute(sender, new String[0]);
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        target.execute(sender, subArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return commands.keySet().stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length > 1) {
            PluginCommand target = commands.get(args[0].toLowerCase());
            if (target != null) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return target.tabComplete(sender, subArgs);
            }
        }

        return null;
    }
}

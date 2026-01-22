package com.fluxcraft.miaomenu.menu.action;

import com.fluxcraft.miaomenu.javamenu.JavaMenuManager;
import com.fluxcraft.miaomenu.menu.action.impl.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class ActionRegistry {
    private final Map<String, MenuAction> actions = new HashMap<>();
    private final Plugin plugin;
    private final MenuAction defaultAction;

    public ActionRegistry(Plugin plugin, JavaMenuManager menuManager) {
        this.plugin = plugin;
        this.defaultAction = new DefaultAction();
        registerDefaults(menuManager);
    }

    private void registerDefaults(JavaMenuManager menuManager) {
        register("player", new PlayerCommandAction());
        register("console", new ConsoleCommandAction());
        register("op", new OpCommandAction());
        register("message", new MessageAction());
        register("broadcast", new BroadcastAction());
        register("close", new CloseAction());
        register("menu", new OpenMenuAction(menuManager));
    }

    public void register(String prefix, MenuAction action) {
        actions.put(prefix.toLowerCase(), action);
    }

    public void dispatch(Player player, String rawCommand) {
        if (rawCommand == null || rawCommand.isEmpty()) return;

        String prefix = null;
        String content = rawCommand;

        if (rawCommand.startsWith("[") && rawCommand.contains("]")) {
            int endIndex = rawCommand.indexOf("]");
            prefix = rawCommand.substring(1, endIndex).toLowerCase();
            content = rawCommand.substring(endIndex + 1).trim();
        }

        MenuAction action = prefix != null ? actions.get(prefix) : defaultAction;
        if (action == null) {
            action = defaultAction;
        }

        try {
            action.execute(player, content, plugin);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to execute menu action: " + rawCommand);
            e.printStackTrace();
        }
    }
}

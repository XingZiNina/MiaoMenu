package com.fluxcraft.miaomenu.menu.action;

import com.fluxcraft.miaomenu.javamenu.JavaMenuManager;
import com.fluxcraft.miaomenu.menu.action.impl.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

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
        if (rawCommand == null || rawCommand.trim().isEmpty()) {
            return;
        }

        String prefix = null;
        String content = rawCommand;

        int bracketStart = rawCommand.indexOf("[");
        int bracketEnd = rawCommand.indexOf("]");

        if (bracketStart == 0 && bracketEnd > 0) {
            prefix = rawCommand.substring(1, bracketEnd).toLowerCase();
            content = rawCommand.substring(bracketEnd + 1).trim();
        }

        MenuAction action = (prefix != null) ? actions.get(prefix) : defaultAction;
        if (action == null) {
            action = defaultAction;
        }

        try {
            action.execute(player, content, plugin);
        } catch (Exception e) {
            String errorMsg = String.format("Failed to execute action '%s' for player '%s': %s",
                    rawCommand, player.getName(), e.getMessage());
            plugin.getLogger().log(Level.SEVERE, errorMsg, e);
        }
    }
}

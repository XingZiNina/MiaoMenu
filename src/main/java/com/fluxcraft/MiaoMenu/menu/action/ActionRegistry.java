package com.fluxcraft.MiaoMenu.menu.action;

import com.fluxcraft.MiaoMenu.menu.action.impl.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ActionRegistry {
    private final Map<String, MenuAction> actions = new HashMap<>();
    private final Plugin plugin;
    private final MenuAction defaultAction;
    public ActionRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.defaultAction = new DefaultAction();
        registerDefaults();
    }
    private void registerDefaults() {
        register("player", new PlayerAction());
        register("message", new MessageAction());
        register("close", new CloseAction());
        register("cmd", new CmdAction());
        register("menu", new com.fluxcraft.MiaoMenu.menu.action.impl.MenuAction());
    }
    public void register(String prefix, MenuAction action) {
        actions.put(prefix.toLowerCase(), action);
    }
    public void dispatch(Player player, String rawCommand) {
        if (rawCommand == null || rawCommand.trim().isEmpty()) {
            return;
        }
        rawCommand = rawCommand.trim();
        String prefix = null;
        String content = rawCommand;
        int bracketStart = rawCommand.indexOf("[");
        int bracketEnd = rawCommand.indexOf("]");
        if (bracketStart == 0 && bracketEnd > bracketStart) {
            prefix = rawCommand.substring(1, bracketEnd).toLowerCase().trim();
            content = rawCommand.substring(bracketEnd + 1).trim();
        }
        MenuAction action = (prefix != null ? actions.get(prefix) : null);
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
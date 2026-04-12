package com.fluxcraft.MiaoMenu.menu.action;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.fluxcraft.MiaoMenu.menu.action.impl.CloseAction;
import com.fluxcraft.MiaoMenu.menu.action.impl.CmdAction;
import com.fluxcraft.MiaoMenu.menu.action.impl.DefaultAction;
import com.fluxcraft.MiaoMenu.menu.action.impl.MessageAction;
import com.fluxcraft.MiaoMenu.menu.action.impl.PlayerAction;
import com.fluxcraft.MiaoMenu.utils.Lang;

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
        String trimmedCommand = rawCommand.trim();
        String prefix = null;
        String content = trimmedCommand;
        int bracketStart = trimmedCommand.indexOf('[');
        int bracketEnd = trimmedCommand.indexOf(']');
        if (bracketStart == 0 && bracketEnd > bracketStart) {
            prefix = trimmedCommand.substring(1, bracketEnd).toLowerCase().trim();
            content = trimmedCommand.substring(bracketEnd + 1).trim();
        }
        MenuAction action = prefix != null ? actions.get(prefix) : null;
        if (action == null) {
            action = defaultAction;
        }
        try {
            action.execute(player, content, plugin);
        } catch (RuntimeException e) {
            plugin.getLogger().log(Level.SEVERE, Lang.get("log.action.dispatch-failed")
                    .replace("{0}", trimmedCommand)
                    .replace("{1}", player.getName()), e);
        }
    }
}

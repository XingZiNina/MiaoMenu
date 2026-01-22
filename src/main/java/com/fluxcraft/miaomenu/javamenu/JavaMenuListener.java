package com.fluxcraft.miaomenu.javamenu;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.menu.action.ActionRegistry;
import com.fluxcraft.miaomenu.utils.PlaceholderUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.List;

public class JavaMenuListener implements Listener {

    private final miaomenu plugin;
    private final ActionRegistry actionRegistry;

    public JavaMenuListener(miaomenu plugin, ActionRegistry actionRegistry) {
        this.plugin = plugin;
        this.actionRegistry = actionRegistry;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getClickedInventory() == null) return;
        if (!(event.getClickedInventory().getHolder() instanceof JavaMenu.MenuHolder)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        JavaMenu.MenuHolder holder = (JavaMenu.MenuHolder) event.getClickedInventory().getHolder();
        JavaMenu.MenuItem item = holder.getMenu().getItem(event.getSlot());

        if (item != null) {
            executeActions(player, item.getCommands());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof JavaMenu.MenuHolder) {
            event.setCancelled(true);
        }
    }

    private void executeActions(Player player, List<String> rawCommands) {
        for (String rawCmd : rawCommands) {
            if (rawCmd == null || rawCmd.trim().isEmpty()) continue;

            String parsed = PlaceholderUtils.parse(player, rawCmd, plugin);
            actionRegistry.dispatch(player, parsed);
        }
    }
}

package com.fluxcraft.miaomenu.managers;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.utils.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuClockManager {
    private final miaomenu plugin;
    private final NamespacedKey clockKey;
    private final Map<UUID, Boolean> pendingRestore = new HashMap<>();

    public MenuClockManager(miaomenu plugin, NamespacedKey clockKey) {
        this.plugin = plugin;
        this.clockKey = clockKey;
    }

    public ItemStack createClock() {
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta meta = clock.getItemMeta();

        if (meta != null) {
            String rawName = Lang.get("menu.clock.name");
            Component nameComponent = Component.text(rawName)
                    .decoration(TextDecoration.ITALIC, false);
            meta.displayName(nameComponent);

            try {
                Enchantment unbreaking = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
                if (unbreaking != null) {
                    meta.addEnchant(unbreaking, 1, true);
                }
            } catch (Exception ignored) {}

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(clockKey, PersistentDataType.BYTE, (byte) 1);
            clock.setItemMeta(meta);
        }
        return clock;
    }

    public boolean isMenuClock(ItemStack item) {
        if (item == null || item.getType() != Material.CLOCK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(clockKey, PersistentDataType.BYTE);
    }

    public boolean playerHasClock(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isMenuClock(item)) return true;
        }
        return false;
    }

    public boolean giveClockToPlayer(Player player) {
        if (playerHasClock(player)) return false;

        player.getInventory().addItem(createClock());
        player.sendMessage(Lang.get("menu.clock.given"));
        return true;
    }

    public void openMenuWithClock(Player player) {
        String defaultMenu = this.plugin.getConfig().getString("settings.default-menu", "main");
        this.plugin.getJavaMenuManager().openMenu(player, defaultMenu);
    }

    public boolean removeClockFromDrops(Player player, java.util.List<ItemStack> drops) {
        if (drops == null) return false;
        boolean removed = false;
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (isMenuClock(drop)) {
                drops.remove(i);
                i--;
                removed = true;
                pendingRestore.put(player.getUniqueId(), true);
            }
        }
        return removed;
    }

    public void ensureClock(Player player) {
        ItemStack clock = createClock();

        boolean restoredFromDeath = false;

        if (pendingRestore.containsKey(player.getUniqueId())) {
            pendingRestore.remove(player.getUniqueId());
            if (!playerHasClock(player)) {
                player.getInventory().addItem(clock);
                player.sendMessage(Lang.get("menu.clock.restored"));
                restoredFromDeath = true;
            }
        }

        if (!playerHasClock(player)) {
            player.getInventory().addItem(clock);
            if (!restoredFromDeath) {
            }
        }
    }
}

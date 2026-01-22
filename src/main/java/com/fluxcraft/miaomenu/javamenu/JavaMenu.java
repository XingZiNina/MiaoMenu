package com.fluxcraft.miaomenu.javamenu;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.constants.Constants;
import com.fluxcraft.miaomenu.utils.PlaceholderUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class JavaMenu {
    private final String name;
    private final String title;
    private final int size;
    private final miaomenu plugin;
    private final List<MenuItem> items;

    public JavaMenu(String name, FileConfiguration config, miaomenu plugin) {
        this.plugin = plugin;
        this.name = name;
        this.title = config.getString("menu_title", config.getString("title", "&6Menu"));
        this.size = Math.min(
                Math.max(config.getInt("rows", 3), Constants.Config.INVENTORY_MIN_ROWS),
                Constants.Config.INVENTORY_MAX_ROWS
        ) * Constants.Config.INVENTORY_ROW_SIZE;

        this.items = new ArrayList<>();
        loadItems(config);
    }

    private void loadItems(FileConfiguration config) {
        if (!config.contains("items")) return;

        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + key;
            items.add(new MenuItem(
                    config.getInt(path + ".slot", 0),
                    config.getString(path + ".material", "STONE"),
                    config.getString(path + ".display_name", "&fItem"),
                    config.getStringList(path + ".lore"),
                    config.getStringList(path + ".left_click_commands")
            ));
        }
    }

    public void open(Player player) {
        String parsedTitle = PlaceholderUtils.parse(player, title, plugin);
        if (parsedTitle.length() > Constants.Config.TITLE_MAX_LENGTH) {
            parsedTitle = parsedTitle.substring(0, Constants.Config.TITLE_MAX_LENGTH);
        }

        Inventory inventory = Bukkit.createInventory(new MenuHolder(this), size, parsedTitle);

        items.forEach(item -> {
            ItemStack stack = item.createItemStack(player, plugin);
            if (stack != null) inventory.setItem(item.getSlot(), stack);
        });

        player.openInventory(inventory);
    }

    public MenuItem getItem(int slot) {
        return items.stream().filter(i -> i.getSlot() == slot).findFirst().orElse(null);
    }

    public static class MenuHolder implements org.bukkit.inventory.InventoryHolder {
        private final JavaMenu menu;
        public MenuHolder(JavaMenu menu) { this.menu = menu; }
        @Override public Inventory getInventory() { return null; }
        public JavaMenu getMenu() { return menu; }
    }

    public static class MenuItem {
        private final int slot;
        private final String material;
        private final String name;
        private final List<String> lore;
        private final List<String> commands;

        public MenuItem(int slot, String material, String name, List<String> lore, List<String> commands) {
            this.slot = slot;
            this.material = material;
            this.name = name;
            this.lore = lore;
            this.commands = commands;
        }

        public ItemStack createItemStack(Player player, miaomenu plugin) {
            Material mat = Material.matchMaterial(material);
            if (mat == null) mat = Material.STONE;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(PlaceholderUtils.parse(player, name, plugin));
                List<String> parsedLore = new ArrayList<>();
                lore.forEach(line -> parsedLore.add(PlaceholderUtils.parse(player, line, plugin)));
                meta.setLore(parsedLore);
                item.setItemMeta(meta);
            }
            return item;
        }

        public int getSlot() { return slot; }
        public List<String> getCommands() { return commands; }
    }
}

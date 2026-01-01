package com.fluxcraft.dGeyserMenuFlux.javamenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import com.fluxcraft.dGeyserMenuFlux.utils.ColorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaMenu {
    private final String name;
    private final FileConfiguration config;
    private final Map<Integer, MenuItem> items = new HashMap<>();

    private String title;
    private int rows;
    private int size;
    private final DGeyserMenuFlux plugin;

    public JavaMenu(String name, FileConfiguration config) {
        this.name = name;
        this.config = config;
        this.plugin = DGeyserMenuFlux.getInstance();
        loadMenuConfig();
        loadItems();
    }

    private void loadMenuConfig() {
        if (config.contains("menu_title")) {
            this.title = ColorUtils.parseHex(config.getString("menu_title", "&6菜单"));
        } else if (config.contains("title")) {
            this.title = ColorUtils.parseHex(config.getString("title", "&6菜单"));
        } else {
            this.title = "&6菜单";
        }

        this.rows = config.getInt("rows", 3);
        if (rows < 1) rows = 1;
        if (rows > 6) rows = 6;

        this.size = rows * 9;
    }

    private void loadItems() {
        if (!config.contains("items")) {
            return;
        }

        for (String itemKey : config.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + itemKey;

            try {
                Object slotObj = config.get(path + ".slot");
                List<Integer> slots = parseSlots(slotObj);

                String materialName = getMaterialName(config, path);
                String displayName = getDisplayName(config, path);
                List<String> lore = getLore(config, path);

                boolean isEnchanted = config.contains(path + ".enchanted") && config.getBoolean(path + ".enchanted");

                Map<String, List<String>> commands = new HashMap<>();
                loadDeluxeMenusCommands(config, path, commands);

                for (int slot : slots) {
                    MenuItem menuItem = new MenuItem(slot, materialName, displayName, lore, commands, isEnchanted);
                    items.put(slot, menuItem);
                }

            } catch (Exception e) {
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Integer> parseSlots(Object slotObj) {
        List<Integer> slots = new ArrayList<>();

        if (slotObj instanceof Integer) {
            slots.add((Integer) slotObj);
        } else if (slotObj instanceof String) {
            String slotStr = (String) slotObj;
            if (slotStr.contains("-")) {
                String[] range = slotStr.split("-");
                if (range.length == 2) {
                    try {
                        int start = Integer.parseInt(range[0]);
                        int end = Integer.parseInt(range[1]);
                        for (int i = start; i <= end; i++) {
                            if (i >= 0 && i < 54) slots.add(i);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else if (slotStr.contains(",")) {
                for (String s : slotStr.split(",")) {
                    try {
                        int n = Integer.parseInt(s.trim());
                        if (n >= 0 && n < 54) slots.add(n);
                    } catch (NumberFormatException ignored) {}
                }
            } else {
                try {
                    int n = Integer.parseInt(slotStr.trim());
                    if (n >= 0 && n < 54) slots.add(n);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (slots.isEmpty()) slots.add(0);
        return slots;
    }

    private String getMaterialName(FileConfiguration config, String path) {
        if (config.contains(path + ".material")) return config.getString(path + ".material");
        if (config.contains(path + ".type")) return config.getString(path + ".type");
        return "STONE";
    }

    private String getDisplayName(FileConfiguration config, String path) {
        if (config.contains(path + ".display_name")) return config.getString(path + ".display_name");
        return "&f未命名";
    }

    private List<String> getLore(FileConfiguration config, String path) {
        if (config.contains(path + ".lore")) return config.getStringList(path + ".lore");
        return new ArrayList<>();
    }

    private void loadDeluxeMenusCommands(FileConfiguration config, String path, Map<String, List<String>> commands) {
        if (config.contains(path + ".left_click_commands")) commands.put("LEFT", config.getStringList(path + ".left_click_commands"));
        else if (config.contains(path + ".left-click-commands")) commands.put("LEFT", config.getStringList(path + ".left-click-commands"));
        else if (config.contains(path + ".actions.LEFT")) commands.put("LEFT", config.getStringList(path + ".actions.LEFT"));

        if (config.contains(path + ".right_click_commands")) commands.put("RIGHT", config.getStringList(path + ".right_click_commands"));
        else if (config.contains(path + ".actions.RIGHT")) commands.put("RIGHT", config.getStringList(path + ".actions.RIGHT"));

        if (config.contains(path + ".commands")) commands.put("ALL", config.getStringList(path + ".commands"));
    }

    public void open(Player player) {
        try {
            String parsedTitle = parsePlaceholders(player, title);
            if (parsedTitle.length() > 32 && parsedTitle.indexOf("§") == -1) {
                parsedTitle = parsedTitle.substring(0, 32);
            }

            MenuHolder holder = new MenuHolder(this);
            Inventory inventory = Bukkit.createInventory(holder, size, parsedTitle);

            for (Map.Entry<Integer, MenuItem> entry : items.entrySet()) {
                ItemStack itemStack = entry.getValue().createItemStack(player);
                if (itemStack != null && entry.getKey() < size) {
                    inventory.setItem(entry.getKey(), itemStack);
                }
            }

            player.openInventory(inventory);
        } catch (Exception e) {
            player.sendMessage("§c打开菜单时发生错误");
            e.printStackTrace();
        }
    }

    private String parsePlaceholders(Player player, String text) {
        if (text == null) return "";
        text = ColorUtils.parseHex(text);
        text = text.replace('&', '§');
        text = text.replace("%player%", player.getName());
        if (plugin.isPlaceholderAPIEnabled()) {
            text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    public String getName() { return name; }
    public String getTitle() { return title; }
    public int getRows() { return rows; }
    public int getSize() { return size; }
    public Map<Integer, MenuItem> getItems() { return new HashMap<>(items); }

    public MenuItem getItemAtSlot(int slot) { return items.get(slot); }
    public String getRawTitle() { return title; }

    public static class MenuItem {
        private final int slot;
        private final String materialName;
        private final String displayName;
        private final List<String> lore;
        private final Map<String, List<String>> commands;
        private final boolean isEnchanted;

        public MenuItem(int slot, String materialName, String displayName, List<String> lore, Map<String, List<String>> commands, boolean isEnchanted) {
            this.slot = slot;
            this.materialName = materialName;
            this.displayName = displayName;
            this.lore = lore != null ? new ArrayList<>(lore) : new ArrayList<>();
            this.commands = commands != null ? new HashMap<>(commands) : new HashMap<>();
            this.isEnchanted = isEnchanted;
        }

        public ItemStack createItemStack(Player player) {
            try {
                Material material = Material.getMaterial(materialName.toUpperCase());
                if (material == null || material.isAir()) material = Material.STONE;

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    String parsedName = parsePlaceholders(player, displayName);
                    meta.setDisplayName(parsedName);

                    List<String> parsedLore = new ArrayList<>();
                    for (String line : lore) parsedLore.add(parsePlaceholders(player, line));
                    meta.setLore(parsedLore);

                    if (isEnchanted) {
                        // 修复 1.20.6+ 的兼容性问题：使用 NamespaceKey 获取 Unbreaking 附魔
                        org.bukkit.enchantments.Enchantment enchant = org.bukkit.enchantments.Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
                        if (enchant != null) {
                            try {
                                meta.addEnchant(enchant, 1, true);
                            } catch (Exception ignored) {}
                        }
                    }

                    item.setItemMeta(meta);
                }

                return item;
            } catch (Exception e) {
                return new ItemStack(Material.STONE);
            }
        }

        private String parsePlaceholders(Player player, String text) {
            if (text == null) return "";
            text = ColorUtils.parseHex(text);
            text = text.replace('&', '§');
            text = text.replace("%player%", player.getName());
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
            }
            return text;
        }

        public int getSlot() { return slot; }
        public List<String> getActions(String clickType) {
            return commands.getOrDefault(clickType.toUpperCase(), new ArrayList<>());
        }
        public boolean hasAnyActions() {
            for (List<String> actionList : commands.values()) {
                if (!actionList.isEmpty()) return true;
            }
            return false;
        }
    }

    public static class MenuHolder implements org.bukkit.inventory.InventoryHolder {
        private final JavaMenu menu;
        public MenuHolder(JavaMenu menu) { this.menu = menu; }
        @Override public Inventory getInventory() { return null; }
        public JavaMenu getMenu() { return menu; }
        public String getMenuName() { return menu.getName(); }
    }
}

package com.fluxcraft.dGeyserMenuFlux.bedrockmenu;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;
import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import com.fluxcraft.dGeyserMenuFlux.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BedrockMenu {
    private final String name;
    private final FileConfiguration config;
    private final List<BedrockMenuItem> menuItems = new ArrayList<>();
    private final DGeyserMenuFlux plugin;

    public BedrockMenu(String name, FileConfiguration config) {
        this.name = name;
        this.config = config;
        this.plugin = DGeyserMenuFlux.getInstance();
        loadMenuItems();
    }


    private void loadMenuItems() {
        menuItems.clear();

        if (config.contains("menu.items")) {
            loadNewFormat();
        } else if (config.contains("buttons")) {
            loadLegacyFormat();
        } else if (config.contains("items")) {
            loadSimpleFormat();
        }
    }

    private void loadNewFormat() {
        List<?> items = config.getList("menu.items");
        if (items == null) return;

        for (Object itemObj : items) {
            if (!(itemObj instanceof org.bukkit.configuration.ConfigurationSection)) continue;

            org.bukkit.configuration.ConfigurationSection itemSection = (org.bukkit.configuration.ConfigurationSection) itemObj;

            String text = itemSection.getString("text", "未命名");
            String icon = itemSection.getString("icon", "");
            String iconType = itemSection.getString("icon_type", "path");
            String command = itemSection.getString("command", "");
            String submenu = itemSection.getString("submenu", "");
            String executeAs = itemSection.getString("execute_as", "player");

            menuItems.add(new BedrockMenuItem(text, icon, iconType, command, submenu, executeAs));
        }
    }

    @SuppressWarnings("unchecked")
    private void loadLegacyFormat() {
        List<?> buttons = config.getList("buttons");
        if (buttons == null) return;

        for (Object buttonObj : buttons) {
            if (buttonObj instanceof org.bukkit.configuration.ConfigurationSection) {
                org.bukkit.configuration.ConfigurationSection buttonSection = (org.bukkit.configuration.ConfigurationSection) buttonObj;

                String text = buttonSection.getString("text", "按钮");
                String icon = "";
                String iconType = "path";

                if (buttonSection.contains("image")) {
                    icon = buttonSection.getString("image.data", "");
                    iconType = buttonSection.getString("image.type", "path");
                }

                String command = extractCommandFromActions(buttonSection.getStringList("actions"));

                menuItems.add(new BedrockMenuItem(text, icon, iconType, command, "", "player"));

            } else if (buttonObj instanceof Map) {
                try {
                    Map<?, ?> buttonMap = (Map<?, ?>) buttonObj;
                    String text = String.valueOf(buttonMap.get("text"));
                    String icon = "";
                    String iconType = "path";

                    if (buttonMap.containsKey("image")) {
                        Object imageObj = buttonMap.get("image");
                        if (imageObj instanceof Map) {
                            Map<?, ?> imageMap = (Map<?, ?>) imageObj;
                            icon = String.valueOf(imageMap.get("data"));
                            iconType = String.valueOf(imageMap.get("type"));
                        }
                    }

                    menuItems.add(new BedrockMenuItem(text, icon, iconType, "", "", "player"));
                } catch (Exception ignored) {}
            }
        }
    }

    private void loadSimpleFormat() {
        List<?> items = config.getList("items");
        if (items == null) return;

        for (Object itemObj : items) {
            if (itemObj instanceof org.bukkit.configuration.ConfigurationSection) {
                org.bukkit.configuration.ConfigurationSection itemSection = (org.bukkit.configuration.ConfigurationSection) itemObj;

                String text = itemSection.getString("text", "按钮");
                String icon = itemSection.getString("icon", "");
                String iconType = itemSection.getString("icon_type", "path");
                String command = itemSection.getString("command", "");
                String submenu = itemSection.getString("submenu", "");

                menuItems.add(new BedrockMenuItem(text, icon, iconType, command, submenu, "player"));
            }
        }
    }

    private String extractCommandFromActions(List<String> actions) {
        if (actions == null) return "";
        for (String action : actions) {
            if (action.startsWith("[command]")) {
                return action.substring(9).trim();
            } else if (action.startsWith("[menu]")) {
                return "dgeysermenu open " + action.substring(6).trim();
            }
        }
        return "";
    }


    public void open(Player player) {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            return;
        }

        try {
            String title = parsePlaceholders(player, getMenuTitle());
            String subtitle = parsePlaceholders(player, getMenuSubtitle());
            String footer = parsePlaceholders(player, getMenuFooter());

            StringBuilder contentBuilder = new StringBuilder();
            if (subtitle != null && !subtitle.isEmpty()) {
                contentBuilder.append(subtitle).append("\n");
            }
            String content = contentBuilder.toString();

            SimpleForm.Builder form = SimpleForm.builder().title(title).content(content);

            if (menuItems.isEmpty()) {
                form.button("§c没有可用的菜单项");
            } else {
                for (BedrockMenuItem menuItem : menuItems) {
                    String buttonText = parsePlaceholders(player, menuItem.getText());
                    if (menuItem.hasIcon()) {
                        form.button(buttonText, FormImage.of(getImageType(menuItem.getIconType()), menuItem.getIcon()));
                    } else {
                        form.button(buttonText);
                    }
                }
            }

            if (footer != null && !footer.isEmpty()) {
                form.content(content + "\n§8" + footer);
            }

            form.validResultHandler(response -> {
                int idx = response.clickedButtonId();
                if (idx >= 0 && idx < menuItems.size()) {
                    handleMenuItemClick(player, menuItems.get(idx));
                }
            });

            FloodgateApi.getInstance().sendForm(player.getUniqueId(), form.build());

        } catch (Exception e) {
            player.sendMessage("§c打开菜单时发生错误");
            e.printStackTrace();
        }
    }

    private void handleMenuItemClick(Player player, BedrockMenuItem menuItem) {
        try {
            if (menuItem.getCommand() != null && !menuItem.getCommand().isEmpty()) {
                String command = parsePlaceholders(player, menuItem.getCommand());
                if (command.startsWith("/")) command = command.substring(1);

                if ("console".equalsIgnoreCase(menuItem.getExecuteAs())) {
                    String lowerCmd = command.toLowerCase();
                    if (lowerCmd.startsWith("server ") || lowerCmd.equals("server") || lowerCmd.startsWith("send")) {
                        player.performCommand(command);
                    } else {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                } else {
                    player.performCommand(command);
                }
                return;
            }

            if (menuItem.getSubmenu() != null && !menuItem.getSubmenu().isEmpty()) {
                String nameWithoutExt = menuItem.getSubmenu().replace(".yml", "");
                plugin.getBedrockMenuManager().openMenu(player, nameWithoutExt);
            }
        } catch (Exception e) {
            player.sendMessage("§c执行命令时发生错误");
        }
    }

    private String parsePlaceholders(Player player, String text) {
        if (text == null) return "";
        text = ColorUtils.parseHex(text);
        text = text.replace('&', '§');
        text = text.replace("%player%", player != null ? player.getName() : "Unknown");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    private String getMenuTitle() { return config.contains("menu.title") ? config.getString("menu.title") : config.getString("title", "菜单"); }
    private String getMenuSubtitle() { return config.contains("menu.subtitle") ? config.getString("menu.subtitle") : ""; }
    private String getMenuFooter() { return config.contains("menu.footer") ? config.getString("menu.footer") : ""; }
    private FormImage.Type getImageType(String type) { return "url".equalsIgnoreCase(type) ? FormImage.Type.URL : FormImage.Type.PATH; }


    public String getName() { return name; }
    public List<BedrockMenuItem> getMenuItems() { return new ArrayList<>(menuItems); }

    public int getMenuItemCount() {
        return menuItems.size();
    }

    public boolean hasValidItems() { return !menuItems.isEmpty(); }


    public static class BedrockMenuItem {
        private final String text;
        private final String icon;
        private final String iconType;
        private final String command;
        private final String submenu;
        private final String executeAs;

        public BedrockMenuItem(String text, String icon, String iconType, String command, String submenu, String executeAs) {
            this.text = text != null ? text : "未命名";
            this.icon = icon != null ? icon : "";
            this.iconType = iconType != null ? iconType : "path";
            this.command = command != null ? command : "";
            this.submenu = submenu != null ? submenu : "";
            this.executeAs = executeAs != null ? executeAs : "player";
        }

        public String getText() { return text; }
        public String getIcon() { return icon; }
        public String getIconType() { return iconType; }
        public String getCommand() { return command; }
        public String getSubmenu() { return submenu; }
        public String getExecuteAs() { return executeAs; }
        public boolean hasIcon() { return icon != null && !icon.isEmpty() && !icon.equals("null"); }
    }
}

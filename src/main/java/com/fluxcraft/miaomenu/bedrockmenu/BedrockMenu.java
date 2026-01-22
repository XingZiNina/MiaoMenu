package com.fluxcraft.miaomenu.bedrockmenu;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.utils.PlaceholderUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;

import java.util.ArrayList;
import java.util.List;

public class BedrockMenu {
    private final String name;
    private final FileConfiguration config;
    private final List<BedrockMenuItem> menuItems = new ArrayList<>();
    private final miaomenu plugin;

    public BedrockMenu(String name, FileConfiguration config, miaomenu plugin) {
        this.name = name;
        this.config = config;
        this.plugin = plugin;
        loadMenuItems();
    }

    private void loadMenuItems() {
        menuItems.clear();
        if (config.contains("menu.items")) {
            List<?> items = config.getList("menu.items");
            if (items != null) {
                for (Object itemObj : items) {
                    if (itemObj instanceof org.bukkit.configuration.ConfigurationSection) {
                        org.bukkit.configuration.ConfigurationSection section = (org.bukkit.configuration.ConfigurationSection) itemObj;
                        menuItems.add(new BedrockMenuItem(
                                section.getString("text", "未命名"),
                                section.getString("icon", ""),
                                section.getString("icon_type", "path"),
                                section.getString("command", ""),
                                section.getString("submenu", "")
                        ));
                    }
                }
            }
        }
    }

    public SimpleForm.Builder buildForm(org.bukkit.entity.Player player) {
        String title = PlaceholderUtils.parse(player, getMenuTitle(), plugin);
        String content = "";

        SimpleForm.Builder form = SimpleForm.builder()
                .title(title)
                .content(content);

        for (BedrockMenuItem item : menuItems) {
            String buttonText = PlaceholderUtils.parse(player, item.getText(), plugin);
            if (item.hasIcon()) {
                form.button(buttonText, FormImage.of(getImageType(item.getIconType()), item.getIcon()));
            } else {
                form.button(buttonText);
            }
        }
        return form;
    }

    private String getMenuTitle() {
        return config.getString("menu.title", "菜单");
    }

    private FormImage.Type getImageType(String type) {
        if ("url".equalsIgnoreCase(type)) return FormImage.Type.URL;
        return FormImage.Type.PATH;
    }

    public String getName() { return name; }
    public List<BedrockMenuItem> getMenuItems() { return menuItems; }

    public static class BedrockMenuItem {
        private final String text;
        private final String icon;
        private final String iconType;
        private final String command;
        private final String submenu;

        public BedrockMenuItem(String text, String icon, String iconType, String command, String submenu) {
            this.text = text != null ? text : "未命名";
            this.icon = icon != null ? icon : "";
            this.iconType = iconType != null ? iconType : "path";
            this.command = command != null ? command : "";
            this.submenu = submenu != null ? submenu : "";
        }

        public String getText() { return text; }
        public String getIcon() { return icon; }
        public String getIconType() { return iconType; }
        public String getCommand() { return command; }
        public boolean hasIcon() { return !icon.isEmpty(); }
    }
}

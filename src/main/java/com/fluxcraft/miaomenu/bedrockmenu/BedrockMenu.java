package com.fluxcraft.miaomenu.bedrockmenu;

import com.fluxcraft.miaomenu.miaomenu;
import com.fluxcraft.miaomenu.utils.Lang;
import com.fluxcraft.miaomenu.utils.PlaceholderUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;

import java.util.ArrayList;
import java.util.List;

public class BedrockMenu {

    private static class ConfigKeys {
        public static final String MENU_ITEMS = "menu.items";
        public static final String MENU_TITLE = "menu.title";
        public static final String DEFAULT_TITLE = "菜单";
        public static final String TEXT = "text";
        public static final String ICON = "icon";
        public static final String ICON_TYPE = "icon_type";
        public static final String COMMAND = "command";
        public static final String SUBMENU = "submenu";
        public static final String DEFAULT_TEXT = Lang.get("default-item-name");
        public static final String DEFAULT_ICON_TYPE = "path";
        public static final String ICON_TYPE_URL = "url";
    }

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
        if (!config.contains(ConfigKeys.MENU_ITEMS)) {
            return;
        }

        List<?> items = config.getList(ConfigKeys.MENU_ITEMS);
        if (items == null) {
            return;
        }

        for (Object itemObj : items) {
            if (itemObj instanceof org.bukkit.configuration.ConfigurationSection) {
                org.bukkit.configuration.ConfigurationSection section = (org.bukkit.configuration.ConfigurationSection) itemObj;
                menuItems.add(new BedrockMenuItem(
                        section.getString(ConfigKeys.TEXT, ConfigKeys.DEFAULT_TEXT),
                        section.getString(ConfigKeys.ICON, ""),
                        section.getString(ConfigKeys.ICON_TYPE, ConfigKeys.DEFAULT_ICON_TYPE),
                        section.getString(ConfigKeys.COMMAND, ""),
                        section.getString(ConfigKeys.SUBMENU, "")
                ));
            }
        }
    }

    public SimpleForm.Builder buildForm(org.bukkit.entity.Player player) {
        String title = PlaceholderUtils.parse(player, getMenuTitle(), plugin);
        SimpleForm.Builder form = SimpleForm.builder()
                .title(title)
                .content("");

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
        return config.getString(ConfigKeys.MENU_TITLE, ConfigKeys.DEFAULT_TITLE);
    }

    private FormImage.Type getImageType(String type) {
        if (ConfigKeys.ICON_TYPE_URL.equalsIgnoreCase(type)) {
            return FormImage.Type.URL;
        }
        return FormImage.Type.PATH;
    }

    public String getName() {
        return name;
    }

    public List<BedrockMenuItem> getMenuItems() {
        return new ArrayList<>(menuItems); // 防御性拷贝，防止外部修改
    }

    public static class BedrockMenuItem {
        private final String text;
        private final String icon;
        private final String iconType;
        private final String command;
        private final String submenu;

        public BedrockMenuItem(String text, String icon, String iconType, String command, String submenu) {
            this.text = text != null ? text : ConfigKeys.DEFAULT_TEXT;
            this.icon = icon != null ? icon : "";
            this.iconType = iconType != null ? iconType : ConfigKeys.DEFAULT_ICON_TYPE;
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

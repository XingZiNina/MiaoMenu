package com.fluxcraft.MiaoMenu.bedrockmenu;

import com.fluxcraft.MiaoMenu.MiaoMenu;
import com.fluxcraft.MiaoMenu.utils.Lang;
import com.fluxcraft.MiaoMenu.utils.PlaceholderUtils;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BedrockMenu {

    private static class ConfigKeys {
        public static final String MENU_ITEMS = "menu.items";
        public static final String MENU_TITLE = "menu.title";
        public static final String DEFAULT_TITLE = "Menu";
        public static final String TEXT = "text";
        public static final String ICON = "icon";
        public static final String ICON_TYPE = "icon_type";
        public static final String COMMAND = "command";
        public static final String EXECUTE_AS = "execute_as";
        public static final String DEFAULT_ICON_TYPE = "path";
        public static final String ICON_TYPE_URL = "url";
    }
    private final String name;
    private final FileConfiguration config;
    private final List<BedrockMenuItem> menuItems = new ArrayList<>();
    private final MiaoMenu plugin;
    public BedrockMenu(String name, FileConfiguration config, MiaoMenu plugin) {
        this.name = name;
        this.config = config;
        this.plugin = plugin;
        loadMenuItems();
    }
    private String getDefaultText() {
        return Lang.get("default-item-name");
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
        String defaultText = getDefaultText();
        for (Object itemObj : items) {
            if (itemObj instanceof Map<?, ?> map) {
                String text = map.get(ConfigKeys.TEXT) != null ? map.get(ConfigKeys.TEXT).toString() : defaultText;
                String icon = map.get(ConfigKeys.ICON) != null ? map.get(ConfigKeys.ICON).toString() : "";
                String iconType = map.get(ConfigKeys.ICON_TYPE) != null ? map.get(ConfigKeys.ICON_TYPE).toString() : ConfigKeys.DEFAULT_ICON_TYPE;
                String command = map.get(ConfigKeys.COMMAND) != null ? map.get(ConfigKeys.COMMAND).toString() : "";
                String executeAs = map.get(ConfigKeys.EXECUTE_AS) != null ? map.get(ConfigKeys.EXECUTE_AS).toString() : "player";
                menuItems.add(new BedrockMenuItem(text, icon, iconType, command, executeAs));
            }
        }
    }
    public Object buildForm(org.bukkit.entity.Player player) {
        try {
            Class<?> simpleFormClass = Class.forName("org.geysermc.cumulus.form.SimpleForm");
            Class<?> formImageClass = Class.forName("org.geysermc.cumulus.util.FormImage");
            Object builder = simpleFormClass.getMethod("builder").invoke(null);
            String title = PlaceholderUtils.parse(player, getMenuTitle(), plugin);
            builder.getClass().getMethod("title", String.class).invoke(builder, title);
            builder.getClass().getMethod("content", String.class).invoke(builder, "");
            for (BedrockMenuItem item : menuItems) {
                String buttonText = PlaceholderUtils.parse(player, item.text(), plugin);
                if (item.hasIcon()) {
                    Object imageType = parseImageType(formImageClass, item.iconType());
                    Object formImage = formImageClass.getMethod("of", formImageClass.getDeclaredClasses()[0], String.class)
                            .invoke(null, imageType, item.icon());
                    builder.getClass().getMethod("button", String.class, formImageClass).invoke(builder, buttonText, formImage);
                } else {
                    builder.getClass().getMethod("button", String.class).invoke(builder, buttonText);
                }
            }
            return builder;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to build Bedrock form (is Floodgate/Cumulus installed?): " + e.getMessage());
            return null;
        }
    }
    private Object parseImageType(Class<?> formImageClass, String typeString) {
        try {
            Class<?> typeEnum = Class.forName("org.geysermc.cumulus.util.FormImage$Type");
            Object[] constants = typeEnum.getEnumConstants();
            if (ConfigKeys.ICON_TYPE_URL.equalsIgnoreCase(typeString)) {
                for (Object c : constants) {
                    if (c.toString().equals("URL")) return c;
                }
            }
            for (Object c : constants) {
                if (c.toString().equals("PATH")) return c;
            }
            return constants[0];
        } catch (Exception e) {
            return null;
        }
    }
    private String getMenuTitle() {
        return config.getString(ConfigKeys.MENU_TITLE, ConfigKeys.DEFAULT_TITLE);
    }
    public String getName() {
        return name;
    }
    public List<BedrockMenuItem> getMenuItems() {
        return new ArrayList<>(menuItems);
    }
    public record BedrockMenuItem(String text, String icon, String iconType, String command, String executeAs) {
        public BedrockMenuItem {
            if (text == null) text = "";
            if (icon == null) icon = "";
            if (iconType == null) iconType = ConfigKeys.DEFAULT_ICON_TYPE;
            if (command == null) command = "";
            if (executeAs == null) executeAs = "player";
        }
        public String getCommand() { return command; }
        public String getExecuteAs() { return executeAs; }
        public boolean hasIcon() {
            return !icon.isEmpty();
        }
    }
}

package com.fluxcraft.MiaoMenu.bedrockmenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.fluxcraft.MiaoMenu.MiaoMenu;
import com.fluxcraft.MiaoMenu.menu.requirement.ConditionGroup;
import com.fluxcraft.MiaoMenu.menu.requirement.RequirementBlock;
import com.fluxcraft.MiaoMenu.menu.requirement.RequirementResult;
import com.fluxcraft.MiaoMenu.menu.requirement.RequirementService;
import com.fluxcraft.MiaoMenu.utils.Lang;
import com.fluxcraft.MiaoMenu.utils.PlaceholderUtils;

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
    private final RequirementService requirementService;
    private final Map<String, RequirementBlock> requirementBlocks;
    private final List<Map<?, ?>> viewRequirements;
    private final String denyMessage;
    private final String fallbackMenu;

    public BedrockMenu(String name, FileConfiguration config, MiaoMenu plugin, RequirementService requirementService) {
        this.name = name;
        this.config = config;
        this.plugin = plugin;
        this.requirementService = requirementService;
        ConfigurationSection blocksSection = config.getConfigurationSection("requirement_blocks");
        requirementBlocks = requirementService.loadBlocks(blocksSection);
        viewRequirements = new ArrayList<>(config.getMapList("view_requirement.requirements"));
        denyMessage = config.getString("view_requirement.deny_message");
        fallbackMenu = config.getString("view_requirement.fallback_menu");
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
                String lockMessage = map.get("lock_message") != null ? map.get("lock_message").toString() : null;
                ConditionGroup conditionGroup = loadConditionGroup(map);
                menuItems.add(new BedrockMenuItem(text, icon, iconType, command, executeAs, conditionGroup, lockMessage));
            }
        }
    }

    private ConditionGroup loadConditionGroup(Map<?, ?> map) {
        if (map.get("conditions") instanceof Map<?, ?> conditionsMap) {
            return ConditionGroup.fromYaml(conditionsMap);
        }
        List<Map<?, ?>> legacyConditions = new ArrayList<>();
        Object conditionValue = map.get("item_conditions");
        if (conditionValue instanceof List<?> rawList) {
            for (Object element : rawList) {
                if (element instanceof Map<?, ?> conditionMap) {
                    legacyConditions.add(conditionMap);
                }
            }
        }
        return ConditionGroup.fromLegacyConditions(legacyConditions);
    }

    public List<BedrockMenuItem> getAllItems() {
        return new ArrayList<>(menuItems);
    }

    public Object buildForm(Player player) {
        try {
            Class<?> simpleFormClass = Class.forName("org.geysermc.cumulus.form.SimpleForm");
            Class<?> formImageClass = Class.forName("org.geysermc.cumulus.util.FormImage");
            Object builder = simpleFormClass.getMethod("builder").invoke(null);
            String title = PlaceholderUtils.parse(player, getMenuTitle(), plugin);
            builder.getClass().getMethod("title", String.class).invoke(builder, title);
            builder.getClass().getMethod("content", String.class).invoke(builder, "");
            for (BedrockMenuItem item : menuItems) {
                boolean locked = item.isLocked(player, requirementService, name, requirementBlocks);
                String buttonText;
                if (locked) {
                    String originalText = PlaceholderUtils.parse(player, item.text(), plugin);
                    buttonText = "§8[Locked] §7" + originalText.replaceAll("§[0-9a-fk-or]", "");
                } else {
                    buttonText = PlaceholderUtils.parse(player, item.text(), plugin);
                }
                if (!locked && item.hasIcon()) {
                    addIconButton(builder, formImageClass, buttonText, item);
                } else {
                    builder.getClass().getMethod("button", String.class).invoke(builder, buttonText);
                }
            }
            return builder;
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().log(Level.WARNING, Lang.get("log.bedrock-menu.form-build-failed"), e);
            return null;
        }
    }

    private void addIconButton(Object builder, Class<?> formImageClass, String buttonText, BedrockMenuItem item) throws ReflectiveOperationException {
        Object imageType = parseImageType(item.iconType());
        Object formImage = formImageClass.getMethod("of", formImageClass.getDeclaredClasses()[0], String.class)
                .invoke(null, imageType, item.icon());
        builder.getClass().getMethod("button", String.class, formImageClass)
                .invoke(builder, buttonText, formImage);
    }

    private Object parseImageType(String typeString) {
        try {
            Class<?> typeEnum = Class.forName("org.geysermc.cumulus.util.FormImage$Type");
            Object[] constants = typeEnum.getEnumConstants();
            if (ConfigKeys.ICON_TYPE_URL.equalsIgnoreCase(typeString)) {
                for (Object constant : constants) {
                    if (constant.toString().equals("URL")) {
                        return constant;
                    }
                }
            }
            for (Object constant : constants) {
                if (constant.toString().equals("PATH")) {
                    return constant;
                }
            }
            return constants[0];
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public RequirementResult checkViewRequirement(Player player) {
        return requirementService.checkViewRequirement(player, name, requirementBlocks, viewRequirements, denyMessage, fallbackMenu);
    }

    private String getMenuTitle() {
        return config.getString(ConfigKeys.MENU_TITLE, ConfigKeys.DEFAULT_TITLE);
    }

    public String getName() {
        return name;
    }

    public record BedrockMenuItem(
            String text,
            String icon,
            String iconType,
            String command,
            String executeAs,
            ConditionGroup conditionGroup,
            String lockMessage
    ) {
        public BedrockMenuItem {
            if (text == null) {
                text = "";
            }
            if (icon == null) {
                icon = "";
            }
            if (iconType == null) {
                iconType = ConfigKeys.DEFAULT_ICON_TYPE;
            }
            if (command == null) {
                command = "";
            }
            if (executeAs == null) {
                executeAs = "player";
            }
        }

        public boolean isLocked(Player player, RequirementService requirementService, String menuName, Map<String, RequirementBlock> requirementBlocks) {
            if (conditionGroup == null) {
                return false;
            }
            if (conditionGroup.requirements().isEmpty() && conditionGroup.children().isEmpty()) {
                return false;
            }
            RequirementResult result = requirementService.evaluateGroup(player, menuName, requirementBlocks, conditionGroup);
            return !result.allowed();
        }

        public String getCommand() {
            return command;
        }

        public String getExecuteAs() {
            return executeAs;
        }

        public boolean hasIcon() {
            return !icon.isEmpty();
        }
    }
}

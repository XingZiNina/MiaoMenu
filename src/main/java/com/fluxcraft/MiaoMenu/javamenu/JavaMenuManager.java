package com.fluxcraft.MiaoMenu.javamenu;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.fluxcraft.MiaoMenu.MiaoMenu;
import com.fluxcraft.MiaoMenu.integration.ItemResolver;
import com.fluxcraft.MiaoMenu.managers.SoundsClock;
import com.fluxcraft.MiaoMenu.menu.requirement.RequirementFeedbackHandler;
import com.fluxcraft.MiaoMenu.menu.requirement.RequirementResult;
import com.fluxcraft.MiaoMenu.menu.requirement.RequirementService;
import com.fluxcraft.MiaoMenu.utils.Lang;
import com.fluxcraft.MiaoMenu.utils.MenuUtils;

public class JavaMenuManager {
    private final MiaoMenu plugin;
    private final ItemResolver itemResolver;
    private final SoundsClock soundsClock;
    private final RequirementService requirementService;
    private final RequirementFeedbackHandler requirementFeedbackHandler;
    private volatile Map<String, JavaMenu> menus = Collections.emptyMap();

    public JavaMenuManager(
            MiaoMenu plugin,
            ItemResolver itemResolver,
            SoundsClock soundsClock,
            RequirementService requirementService,
            RequirementFeedbackHandler requirementFeedbackHandler
    ) {
        this.plugin = plugin;
        this.itemResolver = itemResolver;
        this.soundsClock = soundsClock;
        this.requirementService = requirementService;
        this.requirementFeedbackHandler = requirementFeedbackHandler;
    }

    public void loadAllMenus() {
        Map<String, JavaMenu> newMenus = new ConcurrentHashMap<>();
        File dir = new File(plugin.getDataFolder(), "java_menus");
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().warning(Lang.get("log.java-menu.directory-create-failed"));
            return;
        }
        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            try {
                String name = file.getName().replace(".yml", "");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                newMenus.put(name, new JavaMenu(name, config, plugin, itemResolver, requirementService));
            } catch (RuntimeException e) {
                plugin.getLogger().log(Level.WARNING, Lang.get("log.java-menu.load-failed").replace("{0}", file.getName()), e);
            }
        }
        this.menus = newMenus;
    }

    public void openMenu(Player player, String menuName) {
        JavaMenu menu = menus.get(menuName);
        if (MenuUtils.handleMenuNotFound(player, menu, menuName)) {
            return;
        }
        RequirementResult requirementResult = menu.checkViewRequirement(player);
        if (MenuUtils.handleRequirementDenied(requirementFeedbackHandler, player, requirementResult)) {
            return;
        }
        soundsClock.playMenuOpenSound(player);
        try {
            menu.open(player);
        } catch (RuntimeException e) {
            player.sendMessage(Lang.get("open.error"));
            plugin.getLogger().log(Level.SEVERE, Lang.get("log.java-menu.open-failed").replace("{0}", menuName), e);
        }
    }

    public Map<String, JavaMenu> getMenus() {
        return Collections.unmodifiableMap(menus);
    }
}

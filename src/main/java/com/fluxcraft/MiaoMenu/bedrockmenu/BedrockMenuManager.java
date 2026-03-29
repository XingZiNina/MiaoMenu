package com.fluxcraft.MiaoMenu.bedrockmenu;

import com.fluxcraft.MiaoMenu.MiaoMenu;
import com.fluxcraft.MiaoMenu.menu.action.ActionRegistry;
import com.fluxcraft.MiaoMenu.utils.Lang;
import com.fluxcraft.MiaoMenu.utils.PlaceholderUtils;
import com.fluxcraft.MiaoMenu.managers.SoundsClock;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class BedrockMenuManager {
    private final MiaoMenu plugin;
    private final ActionRegistry actionRegistry;
    private final SoundsClock soundsClock;
    private volatile Map<String, BedrockMenu> menus = Collections.emptyMap();
    public BedrockMenuManager(MiaoMenu plugin, ActionRegistry actionRegistry, SoundsClock soundsClock) {
        this.plugin = plugin;
        this.actionRegistry = actionRegistry;
        this.soundsClock = soundsClock;
    }
    public void loadAllMenus() {
        Map<String, BedrockMenu> newMenus = new ConcurrentHashMap<>();
        File dir = new File(plugin.getDataFolder(), "bedrock_menus");
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().severe("Failed to create bedrock_menus directory! Please check file permissions.");
            return;
        }
        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            try {
                String name = file.getName().replace(".yml", "");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                newMenus.put(name, new BedrockMenu(name, config, plugin));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load Bedrock menu: " + file.getName() + " | Reason: " + e.getMessage());
            }
        }
        this.menus = newMenus;
    }
    public void openMenu(Player player, String menuName) {
        BedrockMenu menu = menus.get(menuName);
        if (menu == null) {
            player.sendMessage(Lang.get("message.menu-not-found") + menuName);
            return;
        }
        soundsClock.playMenuOpenSound(player);
        try {
            sendFloodgateForm(plugin, player, menu, actionRegistry);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to send form '" + menuName + "' to Bedrock player " + player.getName(), e);
            player.sendMessage(Lang.get("open.error"));
        }
    }
    private static void sendFloodgateForm(MiaoMenu plugin, Player player, BedrockMenu menu, ActionRegistry actionRegistry) {
        try {
            Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object api = floodgateApiClass.getMethod("getInstance").invoke(null);
            Object formBuilder = menu.buildForm(player);
            if (formBuilder == null) {
                plugin.getLogger().warning("Failed to build form for Bedrock player: " + player.getName());
                return;
            }
            Object builtForm = formBuilder.getClass().getMethod("build").invoke(formBuilder);
            Class<?> formClass = Class.forName("org.geysermc.cumulus.form.Form");
            Class<?> simpleFormClass = Class.forName("org.geysermc.cumulus.form.SimpleForm");
            Object responseHandler = java.lang.reflect.Proxy.newProxyInstance(
                    simpleFormClass.getClassLoader(),
                    new Class<?>[]{ Class.forName("org.geysermc.cumulus.response.result.ValidFormResponseResult$ValidFormResponseHandler") },
                    (proxy, method, args) -> {
                        if (method.getName().equals("onValidResult")) {
                            Object response = args[0];
                            int clickedIndex = (int) response.getClass().getMethod("clickedButtonId").invoke(response);
                            if (clickedIndex < menu.getMenuItems().size()) {
                                BedrockMenu.BedrockMenuItem item = menu.getMenuItems().get(clickedIndex);
                                String cmd = item.getCommand();
                                if (cmd != null && !cmd.isEmpty()) {
                                    String parsed = PlaceholderUtils.parse(player, cmd, plugin);
                                    String executeAs = item.getExecuteAs();
                                    String wrappedCommand = "[" + executeAs + "] " + parsed;
                                    actionRegistry.dispatch(player, wrappedCommand);
                                }
                            }
                        }
                        return null;
                    }
            );
            floodgateApiClass.getMethod("sendForm", java.util.UUID.class, formClass, Class.forName("org.geysermc.cumulus.response.result.ValidFormResponseResult$ValidFormResponseHandler"))
                    .invoke(api, player.getUniqueId(), builtForm, responseHandler);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Floodgate/Cumulus classes not found. Cannot send Bedrock form.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to send Floodgate form: " + e.getMessage(), e);
        }
    }
    public Map<String, BedrockMenu> getMenus() {
        return Collections.unmodifiableMap(menus);
    }
}

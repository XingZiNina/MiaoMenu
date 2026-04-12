package com.fluxcraft.MiaoMenu.menu.requirement;

import org.bukkit.entity.Player;

import com.fluxcraft.MiaoMenu.MiaoMenu;
import com.fluxcraft.MiaoMenu.security.InputValidator;
import com.fluxcraft.MiaoMenu.utils.Lang;
import com.fluxcraft.MiaoMenu.utils.PlaceholderUtils;

public class RequirementFeedbackHandler {
    private final MiaoMenu plugin;

    public RequirementFeedbackHandler(MiaoMenu plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, RequirementResult result) {
        if (result.allowed()) {
            return;
        }
        if (result.denyMessage() != null && !result.denyMessage().isBlank()) {
            player.sendMessage(PlaceholderUtils.parse(player, result.denyMessage(), plugin));
        } else {
            player.sendMessage(Lang.get("message.requirement-denied"));
        }
        if (result.fallbackMenu() != null && InputValidator.isSafeMenuName(result.fallbackMenu())) {
            plugin.openSmartMenu(player, result.fallbackMenu());
        }
    }
}

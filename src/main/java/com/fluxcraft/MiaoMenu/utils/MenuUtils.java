package com.fluxcraft.MiaoMenu.utils;

import org.bukkit.entity.Player;

import com.fluxcraft.MiaoMenu.menu.requirement.RequirementFeedbackHandler;
import com.fluxcraft.MiaoMenu.menu.requirement.RequirementResult;

public final class MenuUtils {
    private MenuUtils() {
    }

    public static boolean handleMenuNotFound(Player player, Object menu, String menuName) {
        if (menu == null) {
            player.sendMessage(Lang.get("message.menu-not-found").replace("{0}", menuName));
            return true;
        }
        return false;
    }

    public static boolean handleRequirementDenied(RequirementFeedbackHandler handler, Player player, RequirementResult result) {
        if (!result.allowed()) {
            handler.handle(player, result);
            return true;
        }
        return false;
    }
}

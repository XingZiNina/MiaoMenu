package com.fluxcraft.MiaoMenu.menu.requirement;

import org.bukkit.entity.Player;

public interface RequirementEvaluator {
    RequirementResult evaluate(Player player, RequirementResult.RequirementContext context);
}

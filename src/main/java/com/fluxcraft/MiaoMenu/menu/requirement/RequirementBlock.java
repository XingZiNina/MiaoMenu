package com.fluxcraft.MiaoMenu.menu.requirement;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record RequirementBlock(String key, List<Map<?, ?>> requirements, String denyMessage, String fallbackMenu) {
    public RequirementBlock {
        requirements = requirements == null ? Collections.emptyList() : List.copyOf(requirements);
    }
}

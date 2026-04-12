package com.fluxcraft.MiaoMenu.menu.requirement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.fluxcraft.MiaoMenu.MiaoMenu;
import com.fluxcraft.MiaoMenu.utils.PlaceholderUtils;

public class RequirementService {
    private final MiaoMenu plugin;
    private final Map<String, RequirementEvaluator> evaluators = new HashMap<>();

    public RequirementService(MiaoMenu plugin) {
        this.plugin = plugin;
        registerDefaults();
    }

    private void registerDefaults() {
        register("permission", this::evaluatePermission);
        register("placeholder_equals", this::evaluatePlaceholderEquals);
        register("placeholder-not-equals", this::evaluatePlaceholderNotEquals);
        register("placeholder_contains", this::evaluatePlaceholderContains);
        register("advancement", this::evaluateAdvancement);
        register("progress", this::evaluateProgress);
        register("score_gte", this::evaluateScoreGreaterOrEqual);
        register("score_lte", this::evaluateScoreLessOrEqual);
        register("score_equals", this::evaluateScoreEquals);
        register("score_range", this::evaluateScoreRange);
        register("block", this::evaluateBlockReference);
    }

    public void register(String type, RequirementEvaluator evaluator) {
        evaluators.put(type.toLowerCase(Locale.ROOT), evaluator);
    }

    public Map<String, RequirementBlock> loadBlocks(ConfigurationSection section) {
        if (section == null) {
            return Collections.emptyMap();
        }
        Map<String, RequirementBlock> blocks = new HashMap<>();
        for (String key : section.getKeys(false)) {
            List<Map<?, ?>> requirements = asMapList(section.getMapList(key + ".requirements"));
            String denyMessage = section.getString(key + ".deny_message");
            String fallbackMenu = section.getString(key + ".fallback_menu");
            if (requirements.isEmpty()) {
                requirements = asMapList(section.getMapList(key));
            }
            blocks.put(key, new RequirementBlock(key, requirements, denyMessage, fallbackMenu));
        }
        return Collections.unmodifiableMap(blocks);
    }

    public RequirementResult evaluate(Player player, String menuName, Map<String, RequirementBlock> blocks, List<Map<?, ?>> requirements) {
        RequirementResult.RequirementContext context = new RequirementResult.RequirementContext(menuName, blocks, new HashSet<>(), requirements);
        return evaluateInternal(player, context);
    }

    public RequirementResult evaluateGroup(Player player, String menuName, Map<String, RequirementBlock> blocks, ConditionGroup group) {
        if (group.requirements().isEmpty() && group.children().isEmpty()) {
            return RequirementResult.allow();
        }

        ConditionGroup.Operator operator = group.operator();

        for (Map<?, ?> rawRequirement : group.requirements()) {
            String type = getString(rawRequirement, "type", "permission");
            RequirementEvaluator evaluator = evaluators.get(type.toLowerCase(Locale.ROOT));
            if (evaluator == null) {
                continue;
            }
            RequirementResult.RequirementContext ctx = new RequirementResult.RequirementContext(
                    menuName, blocks, new HashSet<>(), Collections.singletonList(rawRequirement)
            );
            RequirementResult result = evaluator.evaluate(player, ctx);
            switch (operator) {
                case AND -> {
                    if (!result.allowed()) {
                        String denyMessage = firstNonBlank(
                                getString(rawRequirement, "deny_message", null),
                                getString(rawRequirement, "lock_message", null)
                        );
                        return RequirementResult.locked(denyMessage);
                    }
                }
                case OR -> {
                    if (result.allowed()) {
                        return RequirementResult.allow();
                    }
                }
            }
        }

        for (ConditionGroup child : group.children()) {
            RequirementResult result = evaluateGroup(player, menuName, blocks, child);
            switch (operator) {
                case AND -> {
                    if (!result.allowed()) {
                        return result;
                    }
                }
                case OR -> {
                    if (result.allowed()) {
                        return result;
                    }
                }
            }
        }

        return operator == ConditionGroup.Operator.AND ? RequirementResult.allow() : RequirementResult.locked(null);
    }

    public RequirementResult evaluateBlock(Player player, RequirementResult.RequirementContext parentContext, String blockKey) {
        RequirementBlock block = parentContext.blocks().get(blockKey);
        if (block == null) {
            return RequirementResult.deny(null, null);
        }
        if (!parentContext.visitedBlocks().add(blockKey)) {
            return RequirementResult.deny(null, null);
        }
        RequirementResult.RequirementContext blockContext = new RequirementResult.RequirementContext(
                parentContext.menuName(),
                parentContext.blocks(),
                parentContext.visitedBlocks(),
                block.requirements()
        );
        RequirementResult result = evaluateInternal(player, blockContext);
        parentContext.visitedBlocks().remove(blockKey);
        if (!result.allowed()) {
            String denyMessage = result.denyMessage() != null ? result.denyMessage() : block.denyMessage();
            String fallbackMenu = result.fallbackMenu() != null ? result.fallbackMenu() : block.fallbackMenu();
            return RequirementResult.deny(denyMessage, fallbackMenu);
        }
        return result;
    }

    private RequirementResult evaluateInternal(Player player, RequirementResult.RequirementContext context) {
        for (Map<?, ?> rawRequirement : context.requirements()) {
            String type = getString(rawRequirement, "type", "permission");
            RequirementEvaluator evaluator = evaluators.get(type.toLowerCase(Locale.ROOT));
            if (evaluator == null) {
                continue;
            }
            RequirementResult result = evaluator.evaluate(player, withCurrentRequirement(context, rawRequirement));
            if (!result.allowed()) {
                String denyMessage = firstNonBlank(result.denyMessage(), getString(rawRequirement, "deny_message", null));
                String fallbackMenu = firstNonBlank(result.fallbackMenu(), getString(rawRequirement, "fallback_menu", null));
                return RequirementResult.deny(denyMessage, fallbackMenu);
            }
        }
        return RequirementResult.allow();
    }

    private RequirementResult.RequirementContext withCurrentRequirement(RequirementResult.RequirementContext context, Map<?, ?> rawRequirement) {
        return new RequirementResult.RequirementContext(
                context.menuName(),
                context.blocks(),
                context.visitedBlocks(),
                Collections.singletonList(rawRequirement)
        );
    }

    private RequirementResult evaluatePermission(Player player, RequirementResult.RequirementContext context) {
        String permission = getString(context.requirements().getFirst(), "permission", null);
        if (permission == null || player.hasPermission(permission)) {
            return RequirementResult.allow();
        }
        return RequirementResult.deny(null, null);
    }

    private RequirementResult evaluatePlaceholderEquals(Player player, RequirementResult.RequirementContext context) {
        return comparePlaceholder(player, context, Comparison.EQUALS);
    }

    private RequirementResult evaluatePlaceholderNotEquals(Player player, RequirementResult.RequirementContext context) {
        return comparePlaceholder(player, context, Comparison.NOT_EQUALS);
    }

    private RequirementResult evaluatePlaceholderContains(Player player, RequirementResult.RequirementContext context) {
        Map<?, ?> requirement = context.requirements().getFirst();
        String placeholder = getString(requirement, "placeholder", "");
        String expected = getString(requirement, "value", "");
        String resolved = PlaceholderUtils.parse(player, placeholder, plugin);
        return resolved.contains(expected) ? RequirementResult.allow() : RequirementResult.deny(null, null);
    }

    private RequirementResult comparePlaceholder(Player player, RequirementResult.RequirementContext context, Comparison comparison) {
        Map<?, ?> requirement = context.requirements().getFirst();
        String placeholder = getString(requirement, "placeholder", "");
        String expected = getString(requirement, "value", "");
        String resolved = PlaceholderUtils.parse(player, placeholder, plugin);
        boolean matched = Objects.equals(resolved, expected);
        if (comparison == Comparison.NOT_EQUALS) {
            matched = !matched;
        }
        return matched ? RequirementResult.allow() : RequirementResult.deny(null, null);
    }

    private RequirementResult evaluateAdvancement(Player player, RequirementResult.RequirementContext context) {
        String advancementKey = getString(context.requirements().getFirst(), "advancement", null);
        if (advancementKey == null) {
            return RequirementResult.deny(null, null);
        }
        NamespacedKey namespacedKey = NamespacedKey.fromString(advancementKey);
        if (namespacedKey == null) {
            return RequirementResult.deny(null, null);
        }
        Advancement advancement = plugin.getServer().getAdvancement(namespacedKey);
        if (advancement == null) {
            return RequirementResult.deny(null, null);
        }
        boolean completed = player.getAdvancementProgress(advancement).isDone();
        return completed ? RequirementResult.allow() : RequirementResult.deny(null, null);
    }

    private RequirementResult evaluateProgress(Player player, RequirementResult.RequirementContext context) {
        Map<?, ?> requirement = context.requirements().getFirst();
        String objectiveName = getString(requirement, "objective", null);
        if (objectiveName == null) {
            return RequirementResult.deny(null, null);
        }
        Integer threshold = getInteger(requirement, "value");
        Integer currentScore = getScore(player, requirement);
        if (currentScore == null) {
            return RequirementResult.deny(null, null);
        }
        if (threshold == null) {
            return currentScore > 0 ? RequirementResult.allow() : RequirementResult.deny(null, null);
        }
        return currentScore >= threshold ? RequirementResult.allow() : RequirementResult.deny(null, null);
    }

    private RequirementResult evaluateScoreComparison(Player player, RequirementResult.RequirementContext context, ScoreComparator comparator) {
        Map<?, ?> req = context.requirements().getFirst();
        Integer currentScore = getScore(player, req);
        Integer expected = getInteger(req, "value");
        if (currentScore == null || expected == null) {
            return RequirementResult.deny(null, null);
        }
        return comparator.test(currentScore, expected) ? RequirementResult.allow() : RequirementResult.deny(null, null);
    }

    private RequirementResult evaluateScoreGreaterOrEqual(Player player, RequirementResult.RequirementContext context) {
        return evaluateScoreComparison(player, context, (current, expected) -> current >= expected);
    }

    private RequirementResult evaluateScoreLessOrEqual(Player player, RequirementResult.RequirementContext context) {
        return evaluateScoreComparison(player, context, (current, expected) -> current <= expected);
    }

    private RequirementResult evaluateScoreEquals(Player player, RequirementResult.RequirementContext context) {
        return evaluateScoreComparison(player, context, Objects::equals);
    }

    private RequirementResult evaluateScoreRange(Player player, RequirementResult.RequirementContext context) {
        Map<?, ?> req = context.requirements().getFirst();
        Integer currentScore = getScore(player, req);
        Integer min = getInteger(req, "min");
        Integer max = getInteger(req, "max");
        if (currentScore == null || min == null || max == null) {
            return RequirementResult.deny(null, null);
        }
        return currentScore >= min && currentScore <= max ? RequirementResult.allow() : RequirementResult.deny(null, null);
    }

    private Integer getScore(Player player, Map<?, ?> requirement) {
        String objectiveName = getString(requirement, "objective", null);
        if (objectiveName == null) {
            return null;
        }
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            objective = plugin.getServer().getScoreboardManager().getMainScoreboard().getObjective(objectiveName);
        }
        if (objective == null) {
            return null;
        }
        return objective.getScore(player.getName()).getScore();
    }

    private Integer getInteger(Map<?, ?> requirement, String key) {
        return switch (requirement.get(key)) {
            case Number number -> number.intValue();
            case String s -> {
                try {
                    yield Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            case null, default -> null;
        };
    }

    private RequirementResult evaluateBlockReference(Player player, RequirementResult.RequirementContext context) {
        String blockKey = getString(context.requirements().getFirst(), "block", null);
        if (blockKey == null) {
            return RequirementResult.deny(null, null);
        }
        return evaluateBlock(player, context, blockKey);
    }

    private List<Map<?, ?>> asMapList(List<Map<?, ?>> source) {
        if (source == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(source);
    }

    private String getString(Map<?, ?> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    public RequirementResult checkViewRequirement(Player player, String menuName,
            Map<String, RequirementBlock> blocks, List<Map<?, ?>> viewRequirements,
            String denyMessage, String fallbackMenu) {
        RequirementResult result = evaluate(player, menuName, blocks, viewRequirements);
        if (!result.allowed()) {
            return RequirementResult.deny(
                    result.denyMessage() != null ? result.denyMessage() : denyMessage,
                    result.fallbackMenu() != null ? result.fallbackMenu() : fallbackMenu
            );
        }
        return result;
    }

    private enum Comparison {
        EQUALS,
        NOT_EQUALS
    }

    @FunctionalInterface
    private interface ScoreComparator {
        boolean test(int current, int expected);
    }
}

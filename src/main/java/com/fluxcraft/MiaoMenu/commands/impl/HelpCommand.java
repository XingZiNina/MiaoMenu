package com.fluxcraft.MiaoMenu.commands.impl;

import java.util.Map;

import org.bukkit.command.CommandSender;

import com.fluxcraft.MiaoMenu.commands.PluginCommand;
import com.fluxcraft.MiaoMenu.utils.Lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class HelpCommand implements PluginCommand {
    private final Map<String, String> commandDescriptions;
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();
    public HelpCommand(Map<String, String> commandDescriptions) {
        this.commandDescriptions = commandDescriptions;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String headerRaw = Lang.get("message.help.header");
        Component header = LEGACY_SERIALIZER.deserialize(headerRaw);
        sender.sendMessage(header);
        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            String cmdName = entry.getKey();
            String descRaw = entry.getValue();
            Component cmdComponent = Component.text(cmdName)
                    .color(NamedTextColor.YELLOW)
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.suggestCommand("/dgm " + cmdName))
                    .hoverEvent(Component.text(descRaw).asHoverEvent());
            Component descComponent = LEGACY_SERIALIZER.deserialize(descRaw);
            sender.sendMessage(
                    Component.empty()
                            .append(Component.text(" ☛ ").color(NamedTextColor.DARK_GRAY))
                            .append(cmdComponent)
                            .append(Component.text(" : ").color(NamedTextColor.GRAY))
                            .append(descComponent)
            );
        }
        String usageRaw = Lang.get("message.help.usage");
        Component usage = LEGACY_SERIALIZER.deserialize(usageRaw);
        sender.sendMessage(Component.empty());
        sender.sendMessage(usage);
    }
}
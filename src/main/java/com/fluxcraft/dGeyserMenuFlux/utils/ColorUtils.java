package com.fluxcraft.dGeyserMenuFlux.utils;

import net.md_5.bungee.api.ChatColor;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:#([A-Fa-f0-9]{6}):#([A-Fa-f0-9]{6})>(.*?)</gradient>");

    public static void init() {}

    public static String parseHex(String text) {
        if (text == null || text.isEmpty()) return text;

        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (gradientMatcher.find()) {
            String start = gradientMatcher.group(1);
            String end = gradientMatcher.group(2);
            String content = gradientMatcher.group(3);
            gradientMatcher.appendReplacement(buffer, applyGradient(content, "#" + start, "#" + end));
        }
        gradientMatcher.appendTail(buffer);
        text = buffer.toString();

        Matcher hexMatcher = HEX_PATTERN.matcher(text);
        while (hexMatcher.find()) {
            String color = hexMatcher.group(1);
            try {
                String replacement = ChatColor.of("#" + color).toString();
                text = text.replaceFirst("&#" + color, replacement);
            } catch (Exception ignored) {}
        }

        return text;
    }

    private static String applyGradient(String text, String fromHex, String toHex) {
        try {
            Color start = new Color(Integer.parseInt(fromHex.substring(1), 16));
            Color end = new Color(Integer.parseInt(toHex.substring(1), 16));
            StringBuilder result = new StringBuilder();
            int length = text.length();

            for (int i = 0; i < length; i++) {
                float ratio = (float) i / (length - 1 == 0 ? 1 : length - 1);
                int red = (int) (start.getRed() + (end.getRed() - start.getRed()) * ratio);
                int green = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * ratio);
                int blue = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * ratio);

                String hex = String.format("#%02x%02x%02x", red, green, blue);
                result.append(ChatColor.of(hex).toString()).append(text.charAt(i));
            }
            return result.toString();
        } catch (Exception e) {
            return text;
        }
    }
}

package com.fluxcraft.MiaoMenu.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Lang {
    private static final LegacyComponentSerializer AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.legacySection();
    private static final String DEFAULT_LANG = "en-us";
    private static final String LANG_DIR = "lang";

    private static Plugin plugin;
    private static volatile YamlConfiguration langConfig;
    private static volatile YamlConfiguration defaultConfig;

    private Lang() {
    }

    public static void init(Plugin plugin) {
        Lang.plugin = plugin;
        loadLanguage();
    }

    private static void loadLanguage() {
        if (plugin == null) return;
        String langCode = plugin.getConfig().getString("settings.lang", DEFAULT_LANG).toLowerCase();
        File langFolder = new File(plugin.getDataFolder(), LANG_DIR);
        if (!langFolder.exists() && !langFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create language directory: " + langFolder.getPath());
        }
        File langFile = new File(langFolder, langCode + ".yml");
        if (!langFile.exists()) {
            copyDefaultLangFile(langCode, langFile);
        }
        if (!langFile.exists()) {
            File defaultFile = new File(langFolder, DEFAULT_LANG + ".yml");
            if (!defaultFile.exists()) {
                copyDefaultLangFile(DEFAULT_LANG, defaultFile);
            }
            langFile = defaultFile;
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        // Load bundled default as fallback
        try (InputStream stream = plugin.getResource(LANG_DIR + "/" + DEFAULT_LANG + ".yml")) {
            if (stream != null) {
                defaultConfig = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load bundled default language file", e);
        }
    }

    private static void copyDefaultLangFile(String langCode, File target) {
        String resourcePath = LANG_DIR + "/" + langCode + ".yml";
        try (InputStream stream = plugin.getResource(resourcePath)) {
            if (stream == null) {
                if (!langCode.equals(DEFAULT_LANG)) {
                    copyDefaultLangFile(DEFAULT_LANG, target);
                }
                return;
            }
            Files.copy(stream, target.toPath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to copy language file: " + langCode, e);
        }
    }

    public static void reload() {
        loadLanguage();
    }

    public static String get(String key) {
        if (langConfig == null) {
            return key;
        }
        String message = langConfig.getString(key);
        if (message == null && defaultConfig != null) {
            message = defaultConfig.getString(key);
        }
        if (message == null) {
            return key;
        }
        return SECTION.serialize(AMPERSAND.deserialize(message));
    }

    public static Map<String, String> getStringSection(String prefix) {
        Map<String, String> result = new LinkedHashMap<>();
        collectSection(langConfig, prefix, result);
        if (result.isEmpty()) {
            collectSection(defaultConfig, prefix, result);
        }
        return result;
    }

    private static void collectSection(YamlConfiguration config, String prefix, Map<String, String> target) {
        if (config == null) {
            return;
        }
        ConfigurationSection section = config.getConfigurationSection(prefix);
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            if (section.isString(key)) {
                target.put(key, section.getString(key));
            }
        }
    }

    @SuppressWarnings("unused")
    private static void saveDefaultStream(InputStream stream, File target) {
        try (OutputStream out = Files.newOutputStream(target.toPath())) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save language file: " + target.getName(), e);
        }
    }
}

package de.clientinfo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

/**
 * Reads message strings from config.yml, replaces placeholders and converts
 * legacy "&" color codes into Adventure components.
 */
public final class MessageUtil {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private final ClientInfoPlugin plugin;
    private FileConfiguration config;

    public MessageUtil(ClientInfoPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reload() {
        this.config = plugin.getConfig();
    }

    /** Returns the raw string for a config path (without prefix), or the path itself if missing. */
    public String raw(String path) {
        String value = config.getString(path);
        if (value == null) {
            plugin.getLogger().warning("Missing message in config.yml: " + path);
            return path;
        }
        return value;
    }

    /** Returns the raw string with placeholders replaced. */
    public String raw(String path, Map<String, String> placeholders) {
        return applyPlaceholders(raw(path), placeholders);
    }

    /** Builds a component from a config path (no prefix). */
    public Component get(String path) {
        return SERIALIZER.deserialize(raw(path));
    }

    /** Builds a component from a config path with placeholders (no prefix). */
    public Component get(String path, Map<String, String> placeholders) {
        return SERIALIZER.deserialize(raw(path, placeholders));
    }

    /** Builds a component from a config path with the configured prefix in front. */
    public Component prefixed(String path) {
        return SERIALIZER.deserialize(raw("messages.prefix") + raw(path));
    }

    /** Builds a prefixed component with placeholders. */
    public Component prefixed(String path, Map<String, String> placeholders) {
        return SERIALIZER.deserialize(raw("messages.prefix") + raw(path, placeholders));
    }

    public void send(CommandSender sender, String path) {
        sender.sendMessage(get(path));
    }

    public void send(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(get(path, placeholders));
    }

    public void sendPrefixed(CommandSender sender, String path) {
        sender.sendMessage(prefixed(path));
    }

    public void sendPrefixed(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(prefixed(path, placeholders));
    }

    private static String applyPlaceholders(String text, Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) {
            return text;
        }
        String result = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}

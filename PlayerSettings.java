package de.clientinfo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Stores per-player settings persistently in players.yml.
 *
 * Currently the only setting is whether the join notification is enabled.
 * Layout:
 *   players:
 *     <uuid>:
 *       join-notification: true
 */
public final class PlayerSettings {

    private static final String FILE_NAME = "players.yml";
    private static final String KEY_JOIN = "join-notification";

    private final ClientInfoPlugin plugin;
    private final File file;
    private FileConfiguration data;

    public PlayerSettings(ClientInfoPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
    }

    /** Loads players.yml from disk (creates an empty one if missing). */
    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder.");
        }
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    plugin.getLogger().info("Created " + FILE_NAME);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create " + FILE_NAME + ": " + e.getMessage());
            }
        }
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    /** Writes players.yml to disk. */
    public void save() {
        if (data == null) {
            return;
        }
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + FILE_NAME + ": " + e.getMessage());
        }
    }

    /**
     * Returns whether the join notification is enabled for the player.
     * Falls back to config value "join-notification.enabled-by-default" if the player never toggled it.
     */
    public boolean isJoinNotificationEnabled(UUID uuid) {
        String path = path(uuid, KEY_JOIN);
        if (data.contains(path)) {
            return data.getBoolean(path);
        }
        return plugin.getConfig().getBoolean("join-notification.enabled-by-default", false);
    }

    public void setJoinNotificationEnabled(UUID uuid, boolean enabled) {
        data.set(path(uuid, KEY_JOIN), enabled);
        save();
    }

    /** Toggles the join notification and returns the new state. */
    public boolean toggleJoinNotification(UUID uuid) {
        boolean newState = !isJoinNotificationEnabled(uuid);
        setJoinNotificationEnabled(uuid, newState);
        return newState;
    }

    private static String path(UUID uuid, String key) {
        return "players." + uuid + "." + key;
    }
}

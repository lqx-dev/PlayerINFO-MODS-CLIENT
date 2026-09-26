package de.clientinfo;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Receives mod lists from compatible client mods on the "clientinfo:modlist" channel.
 *
 * Protocol (see PROTOCOL.md):
 *   - payload is a UTF-8 string
 *   - first line: "v1"
 *   - every following line: one mod, format "modid|Display Name|version" (name and version optional)
 *
 * The data is untrusted client input, so it is validated, sanitized and limited in size.
 * It is only ever stored for the player who sent it.
 */
public final class ModListMessageListener implements PluginMessageListener {

    private static final String PROTOCOL_VERSION = "v1";
    private static final int MAX_PAYLOAD_BYTES = 32_000;
    private static final int MAX_MODS = 500;
    private static final int MAX_LINE_LENGTH = 128;
    private static final int MAX_DISPLAY_LENGTH = 64;

    private final ClientInfoPlugin plugin;

    public ModListMessageListener(ClientInfoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!ClientInfoPlugin.MODLIST_CHANNEL.equals(channel)) {
            return;
        }
        if (message.length == 0 || message.length > MAX_PAYLOAD_BYTES) {
            plugin.getLogger().warning("Ignored mod list from " + player.getName() + ": invalid payload size " + message.length);
            return;
        }

        String payload = new String(message, StandardCharsets.UTF_8);
        String[] lines = payload.split("\n");
        if (lines.length == 0 || !PROTOCOL_VERSION.equals(lines[0].trim())) {
            plugin.getLogger().warning("Ignored mod list from " + player.getName() + ": unsupported protocol version");
            return;
        }

        List<String> mods = new ArrayList<>();
        for (int i = 1; i < lines.length && mods.size() < MAX_MODS; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.length() > MAX_LINE_LENGTH) {
                continue;
            }
            String display = parseModLine(line);
            if (display != null) {
                mods.add(display);
            }
        }

        plugin.getModListManager().setModList(player.getUniqueId(), mods);
        plugin.getLogger().info("Received mod list from " + player.getName() + " (" + mods.size() + " mods).");
    }

    /**
     * Turns "modid|Display Name|version" into a display string.
     * Uses the display name if present, otherwise the mod id. Appends the version if present.
     */
    private static String parseModLine(String line) {
        String[] parts = line.split("\\|", -1);
        String id = sanitize(parts[0]);
        String name = parts.length > 1 ? sanitize(parts[1]) : "";
        String version = parts.length > 2 ? sanitize(parts[2]) : "";

        String base = name.isEmpty() ? id : name;
        if (base.isEmpty()) {
            return null;
        }
        return version.isEmpty() ? base : base + " (" + version + ")";
    }

    /** Strips color/control characters and limits length so client data cannot break chat formatting. */
    private static String sanitize(String input) {
        StringBuilder builder = new StringBuilder();
        for (char c : input.trim().toCharArray()) {
            if (c == '§' || c == '&' || Character.isISOControl(c)) {
                continue;
            }
            builder.append(c);
            if (builder.length() >= MAX_DISPLAY_LENGTH) {
                break;
            }
        }
        return builder.toString().trim();
    }
}

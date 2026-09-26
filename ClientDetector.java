package de.clientinfo;

import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

/**
 * Detects the client a player is using, based on the client brand the client sends
 * to the server shortly after joining (the "minecraft:brand" channel).
 *
 * Paper exposes this value via Player#getClientBrandName(). The brand is a free-form
 * string chosen by the client, so it can be spoofed; this class only reports what
 * the client actually sent and never invents information.
 */
public final class ClientDetector {

    private static final int MAX_BRAND_DISPLAY_LENGTH = 32;

    private final ClientInfoPlugin plugin;

    public ClientDetector(ClientInfoPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the raw brand string sent by the client, or null if the client
     * has not (yet) sent one.
     */
    public String getRawBrand(Player player) {
        String brand = player.getClientBrandName();
        if (brand == null) {
            return null;
        }
        brand = brand.trim();
        return brand.isEmpty() ? null : brand;
    }

    /**
     * Returns a human-readable, display-safe client name for the player.
     * Unknown brands are shown using the configured "unknown" messages.
     */
    public String getDisplayName(Player player) {
        String raw = getRawBrand(player);
        MessageUtil messages = plugin.getMessages();

        if (raw == null) {
            return messages.raw("messages.unknown-client");
        }

        String known = matchKnownBrand(raw);
        if (known != null) {
            return known;
        }

        String safe = sanitize(raw);
        return messages.raw("messages.unknown-client-with-brand", Map.of("%brand%", safe));
    }

    /**
     * Maps common client brands to friendly names. Returns null if the brand is not recognized.
     * Order matters: more specific brands are checked before generic ones (e.g. neoforge before forge).
     */
    private static String matchKnownBrand(String raw) {
        String brand = raw.toLowerCase(Locale.ROOT);

        if (brand.equals("vanilla")) return "Vanilla";
        if (brand.contains("neoforge")) return "NeoForge";
        if (brand.contains("forge")) return "Forge";
        if (brand.contains("fabric")) return "Fabric";
        if (brand.contains("quilt")) return "Quilt";
        if (brand.contains("lunarclient") || brand.contains("lunar")) return "Lunar Client";
        if (brand.contains("feather")) return "Feather";
        if (brand.contains("badlion")) return "Badlion";
        if (brand.contains("labymod")) return "LabyMod";
        if (brand.contains("optifine")) return "OptiFine";
        if (brand.contains("pvplounge")) return "PvPLounge";
        if (brand.contains("cheatbreaker")) return "CheatBreaker";
        if (brand.contains("geyser")) return "Geyser (Bedrock)";

        return null;
    }

    /** Removes color codes/control characters and limits the length, so untrusted client data can't break chat formatting. */
    private static String sanitize(String input) {
        StringBuilder builder = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '§' || c == '&' || Character.isISOControl(c)) {
                continue;
            }
            builder.append(c);
            if (builder.length() >= MAX_BRAND_DISPLAY_LENGTH) {
                break;
            }
        }
        String result = builder.toString().trim();
        return result.isEmpty() ? "?" : result;
    }
}

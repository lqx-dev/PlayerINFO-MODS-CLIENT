package de.clientinfo;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Reads the plugin messaging channels a client registered with the server
 * (sent by the client via "minecraft:register" shortly after joining).
 *
 * Many client mods register their own channels, so the channel list gives HINTS about
 * installed mods. It is NOT a complete mod list: mods without networking (e.g. Sodium,
 * OptiFine, most cheat clients) never show up here. Nothing is guessed beyond a
 * best-effort friendly name for well-known channel namespaces.
 */
public final class ChannelDetector {

    /** One detected namespace, e.g. "xaerominimap" -> "Xaero's Minimap" with 2 channels. */
    public record Entry(String namespace, String displayName, List<String> channels) {}

    /** Well-known channel namespaces mapped to a friendly (guessed) mod name. */
    private static final Map<String, String> KNOWN_NAMESPACES = Map.ofEntries(
            Map.entry("xaerominimap", "Xaero's Minimap"),
            Map.entry("xaeroworldmap", "Xaero's World Map"),
            Map.entry("journeymap", "JourneyMap"),
            Map.entry("voxelmap", "VoxelMap"),
            Map.entry("jei", "JEI (Just Enough Items)"),
            Map.entry("roughlyenoughitems", "REI (Roughly Enough Items)"),
            Map.entry("emi", "EMI"),
            Map.entry("appleskin", "AppleSkin"),
            Map.entry("jade", "Jade"),
            Map.entry("wthit", "WTHIT"),
            Map.entry("waila", "WAILA"),
            Map.entry("modmenu", "Mod Menu"),
            Map.entry("inventorytweaks", "Inventory Tweaks"),
            Map.entry("iris", "Iris Shaders"),
            Map.entry("litematica", "Litematica"),
            Map.entry("malilib", "MaLiLib"),
            Map.entry("minihud", "MiniHUD"),
            Map.entry("tweakeroo", "Tweakeroo"),
            Map.entry("itemscroller", "Item Scroller"),
            Map.entry("carpet", "Carpet"),
            Map.entry("bettersprinting", "Better Sprinting"),
            Map.entry("simple-voice-chat", "Simple Voice Chat"),
            Map.entry("voicechat", "Simple Voice Chat"),
            Map.entry("plasmovoice", "Plasmo Voice"),
            Map.entry("replaymod", "Replay Mod"),
            Map.entry("essential", "Essential"),
            Map.entry("skinlayers3d", "3D Skin Layers"),
            Map.entry("viafabric", "ViaFabric"),
            Map.entry("viafabricplus", "ViaFabricPlus"),
            Map.entry("worldedit", "WorldEdit CUI / WorldEdit"),
            Map.entry("worldeditcui", "WorldEdit CUI"),
            Map.entry("labymod3", "LabyMod"),
            Map.entry("labymod", "LabyMod"),
            Map.entry("lunarclient", "Lunar Client"),
            Map.entry("feather", "Feather Client"),
            Map.entry("badlion", "Badlion Client"),
            Map.entry("geyser", "Geyser"),
            Map.entry("floodgate", "Floodgate"),
            Map.entry("bungeecord", "BungeeCord / Velocity"),
            Map.entry("forge", "Forge"),
            Map.entry("neoforge", "NeoForge"),
            Map.entry("fml", "Forge Mod Loader")
    );

    private static final int MAX_CHANNEL_DISPLAY_LENGTH = 48;

    /**
     * Returns the detected namespaces of the player's registered channels, sorted by name.
     * Vanilla ("minecraft:") and our own ("clientinfo:") channels are ignored.
     */
    public List<Entry> detect(Player player) {
        Set<String> channels = player.getListeningPluginChannels();
        if (channels == null || channels.isEmpty()) {
            return List.of();
        }

        Map<String, List<String>> byNamespace = new TreeMap<>();
        for (String channel : channels) {
            if (channel == null) continue;
            String clean = sanitize(channel).toLowerCase(Locale.ROOT);
            if (clean.isEmpty()) continue;
            if (clean.startsWith("minecraft:") || clean.startsWith(ClientInfoPlugin.MODLIST_CHANNEL.split(":")[0] + ":")) {
                continue;
            }
            String namespace = clean.contains(":") ? clean.substring(0, clean.indexOf(':')) : clean;
            byNamespace.computeIfAbsent(namespace, k -> new ArrayList<>()).add(clean);
        }

        List<Entry> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : byNamespace.entrySet()) {
            String namespace = e.getKey();
            String display = friendlyName(namespace);
            List<String> list = e.getValue();
            Collections.sort(list);
            result.add(new Entry(namespace, display, Collections.unmodifiableList(list)));
        }
        return result;
    }

    /** Total number of non-vanilla channels the client registered. */
    public int countChannels(List<Entry> entries) {
        int total = 0;
        for (Entry entry : entries) {
            total += entry.channels().size();
        }
        return total;
    }

    /** Best-effort friendly name; falls back to the raw namespace if unknown. */
    private static String friendlyName(String namespace) {
        String known = KNOWN_NAMESPACES.get(namespace);
        if (known != null) {
            return known;
        }
        // Fabric API registers many "fabric-*" channels; group them under one name.
        if (namespace.startsWith("fabric-") || namespace.equals("fabric")) {
            return "Fabric API";
        }
        return namespace;
    }

    private static String sanitize(String input) {
        StringBuilder builder = new StringBuilder();
        for (char c : input.trim().toCharArray()) {
            if (c == '§' || c == '&' || Character.isISOControl(c)) continue;
            builder.append(c);
            if (builder.length() >= MAX_CHANNEL_DISPLAY_LENGTH) break;
        }
        return builder.toString().trim();
    }

    /** Small helper so callers can pass a LinkedHashMap-ordered placeholder map. */
    public static Map<String, String> placeholders(String... kv) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(kv[i], kv[i + 1]);
        }
        return map;
    }
}

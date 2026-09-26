package de.clientinfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the last known mod list per online player.
 *
 * Mod lists are only available if a compatible client mod sends them (see PROTOCOL.md).
 * They are kept in memory only and removed when the player leaves, so nothing is ever
 * shown that the client did not actually report.
 */
public final class ModListManager {

    private final Map<UUID, List<String>> modLists = new ConcurrentHashMap<>();

    /** Stores the mod list reported by the player's client. */
    public void setModList(UUID uuid, List<String> mods) {
        modLists.put(uuid, Collections.unmodifiableList(List.copyOf(mods)));
    }

    /** Returns the reported mod list, or empty if the client never sent one. */
    public Optional<List<String>> getModList(UUID uuid) {
        return Optional.ofNullable(modLists.get(uuid));
    }

    public boolean hasModList(UUID uuid) {
        return modLists.containsKey(uuid);
    }

    /** Removes the stored mod list (called on quit). */
    public void remove(UUID uuid) {
        modLists.remove(uuid);
    }

    public void clear() {
        modLists.clear();
    }
}

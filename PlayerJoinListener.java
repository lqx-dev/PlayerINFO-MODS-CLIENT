package de.clientinfo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sends the join notification to a player (only to that player) if they enabled it
 * and have the clientinfo.join permission. Also clears mod list data on quit.
 */
public final class PlayerJoinListener implements Listener {

    private final ClientInfoPlugin plugin;

    public PlayerJoinListener(ClientInfoPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("clientinfo.join")) {
            return;
        }
        if (!plugin.getPlayerSettings().isJoinNotificationEnabled(player.getUniqueId())) {
            return;
        }

        // The client sends its brand (and a client mod its mod list) shortly after joining,
        // so wait a bit before reading it.
        long delay = Math.max(1L, plugin.getConfig().getLong("join-notification.delay-ticks", 40L));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                sendJoinNotification(player);
            }
        }, delay);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getModListManager().remove(event.getPlayer().getUniqueId());
    }

    private void sendJoinNotification(Player player) {
        MessageUtil messages = plugin.getMessages();
        String client = plugin.getClientDetector().getDisplayName(player);
        Optional<List<String>> mods = plugin.getModListManager().getModList(player.getUniqueId());

        messages.sendPrefixed(player, "join.welcome");
        messages.sendPrefixed(player, "join.client", Map.of("%client%", client));
        if (mods.isPresent()) {
            messages.sendPrefixed(player, "join.mods-count", Map.of("%count%", String.valueOf(mods.get().size())));
        } else {
            messages.sendPrefixed(player, "join.mods-unavailable",
                    Map.of("%value%", messages.raw("messages.mods-unavailable")));
        }
    }
}

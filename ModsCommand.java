package de.clientinfo;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Handles /mods <player|self|join|reload>.
 */
public final class ModsCommand implements CommandExecutor {

    /** Valid Minecraft usernames: 1-16 characters, letters, digits and underscore. */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{1,16}$");

    private final ClientInfoPlugin plugin;

    public ModsCommand(ClientInfoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        MessageUtil messages = plugin.getMessages();

        if (args.length != 1) {
            messages.sendPrefixed(sender, "messages.usage");
            return true;
        }

        String arg = args[0];
        switch (arg.toLowerCase(Locale.ROOT)) {
            case "self" -> handleSelf(sender);
            case "join" -> handleJoin(sender);
            case "reload" -> handleReload(sender);
            default -> handlePlayer(sender, arg);
        }
        return true;
    }

    private void handleSelf(CommandSender sender) {
        MessageUtil messages = plugin.getMessages();
        if (!(sender instanceof Player player)) {
            messages.sendPrefixed(sender, "messages.players-only");
            return;
        }
        if (!player.hasPermission("clientinfo.mods.self")) {
            messages.sendPrefixed(player, "messages.no-permission");
            return;
        }
        sendInfo(player, player);
    }

    private void handleJoin(CommandSender sender) {
        MessageUtil messages = plugin.getMessages();
        if (!(sender instanceof Player player)) {
            messages.sendPrefixed(sender, "messages.players-only");
            return;
        }
        if (!player.hasPermission("clientinfo.join")) {
            messages.sendPrefixed(player, "messages.no-permission");
            return;
        }
        boolean enabled = plugin.getPlayerSettings().toggleJoinNotification(player.getUniqueId());
        messages.sendPrefixed(player, enabled ? "messages.join-enabled" : "messages.join-disabled");
    }

    private void handleReload(CommandSender sender) {
        MessageUtil messages = plugin.getMessages();
        if (!sender.hasPermission("clientinfo.reload")) {
            messages.sendPrefixed(sender, "messages.no-permission");
            return;
        }
        plugin.reloadAll();
        messages.sendPrefixed(sender, "messages.reload-success");
    }

    private void handlePlayer(CommandSender sender, String name) {
        MessageUtil messages = plugin.getMessages();
        if (!sender.hasPermission("clientinfo.mods")) {
            messages.sendPrefixed(sender, "messages.no-permission");
            return;
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            messages.sendPrefixed(sender, "messages.invalid-name");
            return;
        }

        Player target = Bukkit.getPlayerExact(name);
        if (target != null) {
            sendInfo(sender, target);
            return;
        }

        // Not online: distinguish between "known but offline" and "never seen".
        // Client information only exists while a player is online, so we can't show anything for offline players.
        OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(name);
        if (offline != null && offline.hasPlayedBefore()) {
            messages.sendPrefixed(sender, "messages.player-offline",
                    Map.of("%player%", offline.getName() != null ? offline.getName() : name));
        } else {
            messages.sendPrefixed(sender, "messages.player-not-found");
        }
    }

    /** Sends the ClientInfo box about {@code target} to {@code viewer}. */
    private void sendInfo(CommandSender viewer, Player target) {
        MessageUtil messages = plugin.getMessages();
        String client = plugin.getClientDetector().getDisplayName(target);
        Optional<List<String>> mods = plugin.getModListManager().getModList(target.getUniqueId());

        messages.send(viewer, "info.header");
        messages.send(viewer, "info.title");
        messages.send(viewer, "info.header");
        messages.send(viewer, "info.player", Map.of("%player%", target.getName()));
        messages.send(viewer, "info.client", Map.of("%client%", client));

        if (mods.isPresent()) {
            List<String> list = mods.get();
            messages.send(viewer, "info.mods-count", Map.of("%count%", String.valueOf(list.size())));
            if (!list.isEmpty()) {
                viewer.sendMessage(net.kyori.adventure.text.Component.empty());
                for (String mod : list) {
                    messages.send(viewer, "info.mod-entry", Map.of("%mod%", mod));
                }
            }
        } else {
            messages.send(viewer, "info.mods-unavailable",
                    Map.of("%value%", messages.raw("messages.mods-unavailable")));
        }

        messages.send(viewer, "info.footer");
    }
}

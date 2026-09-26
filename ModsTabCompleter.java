package de.clientinfo;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tab completion for /mods. Only suggests sub-commands the sender is allowed to use.
 */
public final class ModsTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, String @NotNull [] args) {
        if (args.length != 1) {
            return List.of();
        }

        List<String> options = new ArrayList<>();
        if (sender instanceof Player) {
            if (sender.hasPermission("clientinfo.mods.self")) options.add("self");
            if (sender.hasPermission("clientinfo.join")) options.add("join");
        }
        if (sender.hasPermission("clientinfo.reload")) options.add("reload");
        if (sender.hasPermission("clientinfo.mods")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                options.add(online.getName());
            }
        }

        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                result.add(option);
            }
        }
        return result;
    }
}

package de.tubyoub.statusplugin.commands.tabCompleter;

import de.tubyoub.statusplugin.StatusPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

/**
 * Class implementing the TabCompleter interface to provide tab completion
 * functionality for the status setting command (/status).
 */
public class StatusSetTabCompleter implements TabCompleter {
    private final StatusPlugin plugin;

    public StatusSetTabCompleter(StatusPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Method to handle tab completion for the status setting command.
     *
     * @param sender The sender of the command.
     * @param command The command to be completed.
     * @param alias The alias of the command.
     * @param args The arguments of the command.
     * @return A list of suggestions for tab completion.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        plugin.getFilteredLogger().debug("Tab completing for /status command. Args: {}", String.join(" ", args));

        if (!(sender instanceof Player)) {
            plugin.getFilteredLogger().debug("Console sender, no tab completion for /status.");
            return suggestions; // No tab completion for console on /status
        }

        Player player = (Player) sender;

        if (plugin.getConfigManager().isGroupMode()) {
            if (args.length == 1) {
            // In group mode, suggest available groups that the player has permission for
            suggestions.addAll(plugin.getConfigManager().getStatusGroups().entrySet().stream()
                    .filter(entry -> {
                        String groupName = entry.getKey();
                        List<String> permissions = entry.getValue().getPermissions();
                        // If the group has specific permissions, check if the player has any of them
                        if (permissions != null && !permissions.isEmpty()) {
                            return permissions.stream().anyMatch(player::hasPermission) || player.isOp();
                        } else {
                            // Otherwise check if player has the default permission
                            return player.hasPermission("StatusPlugin.group.set") || player.isOp();
                        }
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()));
            plugin.getFilteredLogger().debug("Group mode: Suggested groups: {}", suggestions);
        }
            // No further suggestions in group mode for /status
        } else {
            // Not in group mode, provide tab completion for /status <status> or /status remove
            if (args.length == 1) {
                if (args[0].isEmpty() || "remove".startsWith(args[0].toLowerCase())) {
                suggestions.add("remove");
                plugin.getFilteredLogger().debug("Not in group mode: Suggested 'remove'.");
            }
            } else if (args.length == 2 && sender.hasPermission("StatusPlugin.admin.setStatus")) {
                suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(name -> name.startsWith(args[1]))
                        .filter(name -> sender != Bukkit.getPlayer(name))
                        .collect(Collectors.toList()));
            }
            // No further suggestions for /status in non-group mode
        }

        return suggestions;
    }
}

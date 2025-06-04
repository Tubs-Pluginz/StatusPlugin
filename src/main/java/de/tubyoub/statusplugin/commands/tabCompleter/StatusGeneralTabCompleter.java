package de.tubyoub.statusplugin.commands.tabCompleter;

import de.tubyoub.statusplugin.StatusPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class implementing the TabCompleter interface to provide tab completion
 * functionality for the general status command (/tsp).
 */
public class StatusGeneralTabCompleter implements TabCompleter {
    private final StatusPlugin plugin;

    public StatusGeneralTabCompleter(StatusPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Method to handle tab completion for the general status command.
     *
     * @param sender  The sender of the command.
     * @param command The command to be completed.
     * @param alias   The alias of the command.
     * @param args    The arguments of the command.
     * @return A list of suggestions for tab completion.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        // plugin.getFilteredLogger().debug("Tab completing for /tsp command. Args: {}", String.join(" ", args));

        if (args.length == 1) {
            // Add suggestions for the first argument of the command
            suggestions.add("help");
            suggestions.add("info");
            if (sender.hasPermission("StatusPlugin.admin.reload") || !(sender instanceof Player)) {
                suggestions.add("reload");
            }
            if (sender.hasPermission("StatusPlugin.admin.setStatus") || !(sender instanceof Player)) {
                suggestions.add("remove");
                suggestions.add("setstatus");
            }
            if (sender.hasPermission("StatusPlugin.admin.setMaxlength") || !(sender instanceof Player)) {
                suggestions.add("setmaxlength");
            }
            if (sender.hasPermission("StatusPlugin.admin.resetMaxlength") || !(sender instanceof Player)) {
                suggestions.add("resetmaxlength");
            }
            plugin.getFilteredLogger().debug("Suggested first arguments for /tsp: {}", suggestions);
        } else if (args.length == 2) {
            // Add suggestions for the second argument of the command based on the first argument
            if (args[0].equalsIgnoreCase("remove") && (sender.hasPermission("StatusPlugin.admin.setStatus") || !(sender instanceof Player))) {
                // If the first argument is "remove", suggest the names of online players
                suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                plugin.getFilteredLogger().debug("Suggested players for /tsp remove: {}", suggestions);
            } else if (args[0].equalsIgnoreCase("setstatus") && (sender.hasPermission("StatusPlugin.admin.setStatus") || !(sender instanceof Player))) {
                // If the first argument is "setstatus", suggest the names of online players
                suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                plugin.getFilteredLogger().debug("Suggested players for /tsp setstatus: {}", suggestions);
            } else if (args[0].equalsIgnoreCase("setmaxlength") && (sender.hasPermission("StatusPlugin.admin.setMaxlength") || !(sender instanceof Player))) {
                // If the first argument is "setmaxlength", suggest some default lengths
                suggestions.add("10");
                suggestions.add("20");
                suggestions.add("30");
                plugin.getFilteredLogger().debug("Suggested lengths for /tsp setmaxlength: {}", suggestions);
            } else if (args[0].equalsIgnoreCase("help")) {
                // If the first argument is "help", suggest "colorcodes"
                suggestions.add("colorcodes");
                plugin.getFilteredLogger().debug("Suggested help topic for /tsp help: {}", suggestions);
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setstatus") && (sender.hasPermission("StatusPlugin.admin.setStatus") || !(sender instanceof Player))) {
            // If the command is /tsp setstatus <player>, suggest available groups if group mode is on
             if (plugin.getConfigManager().isGroupMode()) {
                 suggestions.addAll(plugin.getConfigManager().getStatusGroups().keySet());
                 plugin.getFilteredLogger().debug("Suggested groups for /tsp setstatus <player>: {}", suggestions);
             }
        }
        return suggestions;
    }
}

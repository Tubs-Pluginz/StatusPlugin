package de.tubyoub.statusplugin.commands.tabCompleter;

import de.tubyoub.statusplugin.StatusPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

/**
 * Class implementing the TabCompleter interface to provide tab completion functionality for the status plugin.
 */
public class StatusTabCompleter implements TabCompleter {
    private final StatusPlugin plugin;

    public StatusTabCompleter(StatusPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Method to handle tab completion for the status plugin commands.
     * @param sender The sender of the command.
     * @param command The command to be completed.
     * @param alias The alias of the command.
     * @param args The arguments of the command.
     * @return A list of suggestions for tab completion.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Add suggestions for the first argument of the command
            suggestions.add("help");
            suggestions.add("remove");
            suggestions.add("setmaxlength");
            suggestions.add("resetmaxlength");
            suggestions.add("info");
            suggestions.add("reload");
            suggestions.add("grave");
        } else if (args.length == 2) {
            // Add suggestions for the second argument of the command based on the first argument
            if (args[0].equalsIgnoreCase("remove")) {
                // If the first argument is "remove", suggest the names of online players
                suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            } else if (args[0].equalsIgnoreCase("setmaxlength")) {
                // If the first argument is "setmaxlength", suggest some default lengths
                suggestions.add("10");
                suggestions.add("20");
                suggestions.add("30");
            } else if (args[0].equalsIgnoreCase("help")) {
                // If the first argument is "help", suggest "colorcodes"
                suggestions.add("colorcodes");
            } else if (args[0].equalsIgnoreCase("grave")) {
                // If the first argument is the group command, suggest available groups
                suggestions.addAll(plugin.getConfigManager().getStatusGroups().keySet());
            }
        }
        return suggestions;
    }
}
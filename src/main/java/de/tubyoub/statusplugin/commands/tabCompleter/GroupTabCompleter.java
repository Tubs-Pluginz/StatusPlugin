package de.tubyoub.statusplugin.commands.tabCompleter;

import de.tubyoub.statusplugin.StatusPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the TabCompleter interface to provide tab completion functionality for the group command.
 */
public class GroupTabCompleter implements TabCompleter {
    private final StatusPlugin plugin;

    public GroupTabCompleter(StatusPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Method to handle tab completion for the group command.
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
            // Suggest available groups for the first argument
            suggestions.addAll(plugin.getConfigManager().getStatusGroups().keySet());
        }
        return suggestions;
    }
}
package de.tubyoub.statusplugin.commands;

import de.tubyoub.statusplugin.Managers.StatusManager;
import de.tubyoub.statusplugin.StatusPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GroupCommand implements CommandExecutor {
    private final StatusManager statusManager;
    private final StatusPlugin plugin;

    public GroupCommand(StatusPlugin plugin) {
        this.statusManager = plugin.getStatusManager();
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getPluginPrefix() + " This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (!statusManager.isGroupMode()) {
            player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Group mode is not enabled.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Usage: /" + command.getName() + " <groupname>");
            return true;
        }

        String groupName = args[0];
        if (statusManager.setGroupStatus(player, groupName)) {
            player.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + " Your status has been set to the " + groupName + " group.");
        }

        return true;
    }
}
package de.tubyoub.statusplugin.commands;

import de.tubyoub.statusplugin.Managers.StatusManager;
import de.tubyoub.statusplugin.StatusPlugin;
import de.tubyoub.utils.ColourUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Class implementing the CommandExecutor interface to handle status setting
 * commands.
 */
public class StatusSetCommand implements CommandExecutor {
    private final StatusManager statusManager;
    private final StatusPlugin plugin;

    /**
     * Constructor for the StatusSetCommand class.
     *
     * @param plugin The StatusPlugin instance.
     */
    public StatusSetCommand(StatusPlugin plugin) {
        this.statusManager = plugin.getStatusManager();
        this.plugin = plugin;
    }

    /**
     * Method to handle status setting commands.
     *
     * @param sender  The sender of the command.
     * @param command The command that was sent.
     * @param label   The alias of the command that was used.
     * @param args    The arguments that were provided with the command.
     * @return A boolean indicating whether the command was handled successfully.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getPluginPrefix() + " This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        // Handle removal commands first
        if (args.length > 0 && args[0].equalsIgnoreCase("remove")) {
            if (args.length > 1) {
                removePlayerStatus(player, args[1]);
            } else {
                removeOwnStatus(player);
            }
            return true;
        }

        // Group mode handling
        if (plugin.getConfigManager().isGroupMode()) {
            if (args.length == 2) {
                // Admin setting another player's group: /status <playername> <groupname>
                handleGroupCommandForOtherPlayer(player, args[0], args[1]);
            } else if (args.length == 1) {
                // Player setting own group: /status <groupname>
                handleGroupCommand(player, args[0]);
            } else {
                player.sendMessage(
                        plugin.getPluginPrefix() + ChatColor.RED +
                        " Usage: /" + command.getName() + " <groupname> or /" +
                        command.getName() + " <player> <groupname>");
            }
            return true;
        }

        // Default status mode handling
        if (args.length == 0) {
            player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED +
                    " Usage: /" + command.getName() + " <status> or /" +
                    command.getName() + " remove [player]");
            return true;
        }

        // Check if targeting another player
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null && !target.equals(player)) {
            // Setting another player's status
            if (!player.hasPermission("StatusPlugin.admin.setStatus")) {
                player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED +
                        " You don't have permission to set another player's status.");
                plugin.getFilteredLogger().debug("Player {} tried to set status for {} without permission",
                        player.getName(), target.getName());
                return true;
            }

            String status = Arrays.stream(args, 1, args.length).collect(Collectors.joining(" "));
            if (statusManager.setStatus(target, status, player)) {
                player.sendMessage(plugin.getPluginPrefix() + " Set " + target.getName() + "'s status to: "
                        + ColourUtils.format(status));
                plugin.getFilteredLogger().debug("Player {} set status for {} to: {}",
                        player.getName(), target.getName(), status);
            }
        } else {
            // Setting own status
            if (!player.hasPermission("StatusPlugin.setStatus")) {
                player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED +
                        " You don't have permission to set your own status.");
                plugin.getFilteredLogger().debug("Player {} tried to set their own status without permission",
                        player.getName());
                return true;
            }

            String status = String.join(" ", args);
            if (statusManager.setStatus(player, status, player)) {
                player.sendMessage(plugin.getPluginPrefix() + " Your status has been set to: " + "["
                        + ColourUtils.format(statusManager.translateColorsAndFormatting(
                            statusManager.getStatus(player), player)) + ChatColor.RESET + "]");
                plugin.getFilteredLogger().debug("Player {} set their status to: {}", player.getName(), status);
            }
        }
        return true;
    }

    /**
     * Handles the group command.
     * Sets the player's status to a predefined group status.
     *
     * @param player    The player who sent the command.
     * @param groupName The name of the status group.
     */
    private void handleGroupCommand(Player player, String groupName) {
        plugin.getFilteredLogger().debug("Player {} attempting to set group status to {}", player.getName(),
                groupName);
        if (statusManager.setGroupStatus(player, groupName)) {
            player.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + " Your status has been set to the "
                    + groupName + " group.");
            plugin.getFilteredLogger().debug("Player {} successfully set group status to {}", player.getName(),
                    groupName);
        }
    }

    /**
     * Handles setting a group status for another player.
     *
     * @param sender      The player who sent the command.
     * @param targetName  The name of the player to set the status for.
     * @param groupName   The name of the status group to set.
     */
    private void handleGroupCommandForOtherPlayer(Player sender, String targetName, String groupName) {
        // Check if sender has admin permissions
        if (!sender.hasPermission("StatusPlugin.admin.setStatus")) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED +
                    " You don't have permission to set another player's status group.");
            plugin.getFilteredLogger().debug("Player {} tried to set group status for another player without permission",
                    sender.getName());
            return;
        }

        // Check if target player exists
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED +
                    " Player " + targetName + " not found or offline.");
            plugin.getFilteredLogger().debug("Player {} tried to set group for non-existent player: {}",
                    sender.getName(), targetName);
            return;
        }

        // Set the group status
        plugin.getFilteredLogger().debug("Player {} attempting to set group status for {} to {}",
                sender.getName(), target.getName(), groupName);

        // Pass the sender as the admin parameter
        if (statusManager.setGroupStatus(target, groupName, sender)) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN +
                    " Status for " + target.getName() + " has been set to the " + groupName + " group.");
            target.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN +
                    " Your status has been set to the " + groupName + " group by " + sender.getName() + ".");
            plugin.getFilteredLogger().debug("Player {} successfully set group status for {} to {}",
                    sender.getName(), target.getName(), groupName);
        }
    }

    /**
     * Removes the sender's status.
     * This method is called when a player with the appropriate permissions sends the
     * remove command without any arguments.
     *
     * @param player The player who sent the command.
     */
    private void removeOwnStatus(Player player) {
        if (!player.hasPermission("StatusPlugin.setStatus")) {
            player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " You don't have permission to remove your status.");
            plugin.getFilteredLogger().debug("Player {} tried to remove their status without permission", player.getName());
            return;
        }
        statusManager.removeStatus(player);
        player.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + " Your status has been removed.");
        plugin.getFilteredLogger().debug("Player {} removed their status", player.getName());
    }

    /**
     * Removes another player's status.
     * This method is called when a player with the appropriate permissions sends the
     * remove command with a player name argument.
     *
     * @param sender      The player who sent the command.
     * @param targetName  The name of the player whose status should be removed.
     */
    private void removePlayerStatus(Player sender, String targetName) {
        if (!sender.hasPermission("StatusPlugin.admin.setStatus")) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED +
                    " You don't have permission to remove another player's status.");
            plugin.getFilteredLogger().debug("Player {} tried to remove {}'s status without permission",
                    sender.getName(), targetName);
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Player " + targetName + " not found or offline.");
            plugin.getFilteredLogger().debug("Player {} tried to remove status for non-existent player: {}",
                    sender.getName(), targetName);
            return;
        }

        statusManager.removeStatus(target);
        sender.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + " Status for " +
                target.getName() + " has been removed.");
        plugin.getFilteredLogger().info("Player {} removed status for {}", sender.getName(), target.getName());
    }
}
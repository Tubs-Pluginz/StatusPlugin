package de.tubyoub.statusplugin.commands;

import de.tubyoub.statusplugin.Managers.StatusManager;
import de.tubyoub.statusplugin.StatusPlugin;
import de.tubyoub.utils.ColourUtils;
import de.tubyoub.utils.VersionChecker;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Class implementing the CommandExecutor interface to handle status commands.
 */
public class StatusCommand implements CommandExecutor {
    String version;
    private final StatusManager statusManager;
    private final boolean newVersion;
    private StatusPlugin plugin;

    /**
     * Constructor for the StatusCommand class.
     *
     * @param statusManager  The StatusManager instance used to manage player statuses.
     * @param newVersion If the plugin has a new Version..
     * @param version        The current version of the plugin.
     */
    public StatusCommand(StatusManager statusManager, boolean newVersion, String version) {
        this.statusManager = statusManager;
        this.newVersion = newVersion;
        this.version = version;
    }

    /**
     * Method to handle status commands.
     * This method is called whenever a player or the console sends a status command.
     * The method handles different subcommands based on the arguments provided.
     *
     * @param sender  The sender of the command.
     * @param command The command that was sent.
     * @param label   The alias of the command that was used.
     * @param args    The arguments that were provided with the command.
     * @return A boolean indicating whether the command was handled successfully.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.plugin = (StatusPlugin) Bukkit.getPluginManager().getPlugin("TubsStatusPlugin");

        if (!(sender instanceof Player)) {
            // Handle console commands here
            if (args.length > 0 && "reload".equals(args[0])) {
                statusManager.reloadConfig();
                statusManager.reloadStatuses();
                sender.sendMessage(plugin.getPluginPrefix() + " Statuses have been reloaded.");
                return true;
            }
            sender.sendMessage(plugin.getPluginPrefix() + " This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(plugin.getPluginPrefix() + "Try using /status help");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reloadPlugin(player);
                return true;
            case "help":
                helpCommand(player, plugin, args);
                return true;
            case "setmaxlength":
                setmaxlenghtCommand(player, args);
                return true;
            case "resetmaxlength":
                resetmaxlenghtCommand(player);
                return true;
            case "info":
                infoCommand(player, plugin);
                return true;
            case "remove":
                if (args.length == 1) {
                    removeOwnStatus(player);
                } else if (args.length == 2) {
                    removeOtherPlayerStatus(player, args[1]);
                } else {
                    player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Usage: /status remove [player]");
                }
                return true;
            default:
                handleDefaultCommand(player, args);
                return true;
        }
    }


    /**
     * Handles the default status command.
     * If the command has more than one argument, it tries to set the status of another player.
     * If the command has only one argument, it tries to set the status of the sender.
     *
     * @param player The player who sent the command.
     * @param args   The arguments provided with the command.
     */
    private void handleDefaultCommand(Player player, String[] args) {
        if (args.length > 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                if (!player.hasPermission("StatusPlugin.admin.setStatus")) {
                    player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " You don't have permission to set another player's status.");
                    return;
                }
                String status = Arrays.stream(args, 1, args.length).collect(Collectors.joining(" "));
                if (statusManager.setStatus(target, status, player)) {
                    player.sendMessage(plugin.getPluginPrefix() + " Set " + target.getName() + "'s status to: " + ColourUtils.format(status));
                }
                return;
            }
            player.sendMessage("Invalid player name: " + args[0]);
            return;
        }
        if (!player.hasPermission("StatusPlugin.setStatus")) {
            player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " You don't have permission to set your own status.");
            return;
        }
        String status = String.join(" ", args);
        if (statusManager.setStatus(player, status, player)) {
            player.sendMessage(plugin.getPluginPrefix() + " Your status has been set to: " + "[" + ColourUtils.format(statusManager.getStatus(player)) + ChatColor.RESET + "]");
        }
    }

    /**
     * Reloads the plugin configuration and statuses.
     * This method is called when a player with the appropriate permissions sends the reload command.
     *
     * @param sender The player who sent the command.
     */
    private void reloadPlugin(Player sender) {
        if (!sender.hasPermission("StatusPlugin.admin.reload")) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " You don't have permission to reload statuses.");
            return;
        }
        statusManager.reloadConfig();
        statusManager.reloadStatuses();
        sender.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + " Config & Statuses successfully reloaded");
    }

    /**
     * Handles the help command.
     * If the command has more than one argument and the second argument is "colorcodes", it displays the color codes.
     * Otherwise, it displays the list of available commands.
     *
     * @param sender The player who sent the command.
     * @param plugin The StatusPlugin instance.
     * @param args   The arguments provided with the command.
     */
    private void helpCommand(Player sender, StatusPlugin plugin, String[] args) {
        if (args.length > 1 && "colorcodes".equals(args[1].toLowerCase())) {
            plugin.sendPluginMessages(sender, "title");
            displayColorCodes(sender, plugin);
            plugin.sendPluginMessages(sender, "line");
        } else {
            plugin.sendPluginMessages(sender, "title");
            sender.sendMessage("Here you can see all available commands:");
            // The rest of the code is self-explanatory and does not need documentation.
        }
    }

    /**
     * Displays the available color and formatting codes to the sender.
     *
     * @param sender The player who sent the command.
     * @param plugin The StatusPlugin instance.
     */
    private void displayColorCodes(CommandSender sender, StatusPlugin plugin) {
        sender.sendMessage(ChatColor.GOLD + "Available Color and Formatting Codes:");
        for (ChatColor code : ChatColor.values()) {
            sender.sendMessage(code + code.name() + ChatColor.RESET + " - &" + code.getChar());
        }
        sender.sendMessage("Space - &_");
        sender.sendMessage(ChatColor.RED + "These color codes are only usable if u have the permissions for them, ask your Server Admin why you can't use specific colorcodes");
    }

    /**
     * Handles the setmaxlength command.
     * If the sender has the appropriate permissions, it tries to set the maximum status length.
     * If the command has two arguments, it tries to parse the second argument as an integer and set the maximum status length to that value.
     * If the command does not have two arguments, it sends a usage message to the sender.
     *
     * @param sender The player who sent the command.
     * @param args   The arguments provided with the command.
     */
    private void setmaxlenghtCommand(Player sender, String[] args) {
        if (!sender.hasPermission("StatusPlugin.admin.setMaxlength")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to set the maximum status length.");
            return;
        }
        if (args.length == 2) {
            try {
                int maxLength = Integer.parseInt(args[1]);
                statusManager.setMaxStatusLength(maxLength);
                sender.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + " Max status length set to " + maxLength);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Invalid number format.");
            }
        } else {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Usage: /status setmaxlength <length>");
        }
    }

    /**
     * Resets the maximum status length to its default value.
     * This method is called when a player with the appropriate permissions sends the resetmaxlength command.
     *
     * @param sender The player who sent the command.
     */
    private void resetmaxlenghtCommand(Player sender) {
        if (!sender.hasPermission("StatusPlugin.admin.resetMaxlength")) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " You don't have permission to reset the maximum status length.");
            return;
        }
        statusManager.resetMaxStatusLength();
        sender.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + " Max status length reset to default.");
    }

    /**
     * Displays information about the plugin to the sender.
     * This method is called when a player sends the info command.
     *
     * @param sender The player who sent the command.
     * @param plugin The StatusPlugin instance.
     */
    public void infoCommand(Player sender, StatusPlugin plugin) {
        if (!(plugin == null)) {
            plugin.sendPluginMessages(sender, "title");
        } else {
            sender.sendMessage(ChatColor.GOLD + "◢◤" + ChatColor.YELLOW + "Tu" + ChatColor.DARK_GREEN + "b's" + ChatColor.DARK_AQUA + " Status" + ChatColor.GOLD + " Plugin" + ChatColor.YELLOW + "◥◣");
        }
        sender.sendMessage(ChatColor.GREEN + "Author: TubYoub");
        sender.sendMessage(ChatColor.GREEN + "Version: " + version);

        if (newVersion) {
            sender.sendMessage(ChatColor.YELLOW + "A new version is available! Update at: " + ChatColor.UNDERLINE + "https://modrinth.com/plugin/tubs-status-plugin/version/latest");
        } else {
            sender.sendMessage(ChatColor.GREEN + "You are using the latest version!");
        }
        TextComponent githubLink = new TextComponent(ChatColor.DARK_GRAY + "" + ChatColor.UNDERLINE + "GitHub");
        githubLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/TubYoub/StatusPlugin"));
        githubLink.setUnderlined(true);
        sender.spigot().sendMessage(githubLink);

        TextComponent discordLink = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "Discord");
        discordLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.tubyoub.de"));
        discordLink.setUnderlined(true);
        sender.spigot().sendMessage(discordLink);

        sender.sendMessage("If you have any issues please report them on GitHub or on the Discord.");
        if (!(plugin == null)) {
            plugin.sendPluginMessages(sender, "line");
        } else {
            sender.sendMessage(ChatColor.GOLD + "-" + ChatColor.YELLOW + "-" + ChatColor.GREEN + "-" + ChatColor.DARK_GREEN + "-" + ChatColor.BLUE + "-" + ChatColor.DARK_AQUA + "-"
                    + ChatColor.GOLD + "-" + ChatColor.YELLOW + "-" + ChatColor.GREEN + "-" + ChatColor.DARK_GREEN + "-" + ChatColor.BLUE + "-" + ChatColor.DARK_AQUA + "-"
                    + ChatColor.GOLD + "-" + ChatColor.YELLOW + "-" + ChatColor.GREEN + "-" + ChatColor.DARK_GREEN + "-" + ChatColor.BLUE + "-" + ChatColor.DARK_AQUA + "-"
                    + ChatColor.GOLD + "-");
        }
    }

    /**
     * Removes the sender's status.
     * This method is called when a player with the appropriate permissions sends the remove command without any arguments.
     *
     * @param player The player who sent the command.
     */
    private void removeOwnStatus(Player player) {
        if (!player.hasPermission("StatusPlugin.setStatus")) {
            player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " You don't have permission to remove your status.");
            return;
        }
        statusManager.removeStatus(player);
        player.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + " Your status has been removed.");
    }

    /**
     * Removes the status of another player.
     * This method is called when a player with the appropriate permissions sends the remove command with another player's name as an argument.
     * If the sender does not have the appropriate permissions, it sends a message to the sender and returns.
     * If the target player is not found, it sends a message to the sender and returns.
     * Otherwise, it removes the target player's status and sends a confirmation message to the sender.
     *
     * @param sender     The player who sent the command.
     * @param targetName The name of the target player.
     */
    private void removeOtherPlayerStatus(Player sender, String targetName) {
        if (!sender.hasPermission("StatusPlugin.admin.setStatus")) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " You don't have permission to remove another player's status.");
            return;
        }
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Player not found: " + targetName);
            return;
        }
        statusManager.removeStatus(target);
        sender.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + " Removed " + target.getName() + "'s status.");
    }
}
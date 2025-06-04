package de.tubyoub.statusplugin.commands;

import de.tubyoub.statusplugin.Managers.StatusManager;
import de.tubyoub.statusplugin.StatusPlugin;
import de.tubyoub.utils.ColourUtils;
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
 * Class implementing the CommandExecutor interface to handle general status
 * commands like reload, info, help, etc.
 */
public class StatusGeneralCommand implements CommandExecutor {
    String version;
    private final StatusManager statusManager;
    private final boolean newVersion;
    private StatusPlugin plugin;

    /**
     * Constructor for the StatusGeneralCommand class.
     *
     * @param statusManager The StatusManager instance used to manage player
     *                      statuses.
     * @param newVersion    If the plugin has a new Version..
     * @param version       The current version of the plugin.
     */
    public StatusGeneralCommand(StatusManager statusManager, boolean newVersion, String version) {
        this.statusManager = statusManager;
        this.newVersion = newVersion;
        this.version = version;
    }

    /**
     * Method to handle general status commands.
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

        if (args.length == 0) {
            sender.sendMessage(plugin.getPluginPrefix() + "Try using /tsp help");
            plugin.getFilteredLogger().debug("{} used /tsp with no arguments.", sender.getName());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reloadPlugin(sender);
                return true;
            case "help":
                helpCommand(sender, plugin, args);
                return true;
            case "setmaxlength":
                setmaxlenghtCommand(sender, args);
                return true;
            case "resetmaxlength":
                resetmaxlenghtCommand(sender);
                return true;
            case "info":
                infoCommand(sender, plugin);
                return true;
            case "remove":
                if (sender instanceof Player) {
                    removeOtherPlayerStatus((Player) sender, args);
                } else {
                    sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED
                            + " Usage: /tsp remove <player> (Console can only remove other players)");
                }
                return true;
            case "setstatus":
                if (sender instanceof Player) {
                    setOtherPlayerStatus((Player) sender, args);
                } else {
                    setOtherPlayerStatusConsole(sender, args);
                }
                return true;
            default:
                sender.sendMessage(plugin.getPluginPrefix() + "Unknown subcommand. Try /tsp help");
                plugin.getFilteredLogger().debug("{} used /tsp with unknown subcommand: {}", sender.getName(), args[0]);
                return true;
        }
    }

    /**
     * Sets the status of another player (executed by a player).
     *
     * @param sender The player who sent the command.
     * @param args   The arguments provided with the command.
     */
    private void setOtherPlayerStatus(Player sender, String[] args) {
        if (!sender.hasPermission("StatusPlugin.admin.setStatus")) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED
                    + " You don't have permission to set another player's status.");
            plugin.getFilteredLogger().debug("Player {} tried to set status for another player without permission",
                    sender.getName());
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Usage: /tsp setstatus <player> <status>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Invalid player name: " + args[1]);
            plugin.getFilteredLogger().debug("Invalid player name provided for setstatus: {}", args[1]);
            return;
        }

        String content = Arrays.stream(args, 2, args.length).collect(Collectors.joining(" "));
        boolean isGroupMode = plugin.getConfigManager().isGroupMode();

        if (isGroupMode) {
            if (statusManager.setGroupStatus(target, content, sender)) {
                sender.sendMessage(plugin.getPluginPrefix() + " Set " + target.getName() + "'s group to: " + content);
                plugin.getFilteredLogger().debug("Player {} set group for {} to: {}", sender.getName(), target.getName(), content);
            }
        } else {
            if (statusManager.setStatus(target, content, sender)) {
                sender.sendMessage(plugin.getPluginPrefix() + " Set " + target.getName() + "'s status to: " + ColourUtils.format(content));
                plugin.getFilteredLogger().debug("Player {} set status for {} to: {}", sender.getName(), target.getName(), content);
            }
        }
    }

    /**
     * Sets the status of another player (executed by console).
     *
     * @param sender The console sender.
     * @param args   The arguments provided with the command.
     */
    private void setOtherPlayerStatusConsole(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Usage: /tsp setstatus <player> <status>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target != null) {
            String status = Arrays.stream(args, 2, args.length).collect(Collectors.joining(" "));
            if (statusManager.setStatus(target, status, sender)) {
                sender.sendMessage(plugin.getPluginPrefix() + " Set " + target.getName() + "'s status to: "
                        + ColourUtils.format(status));
                plugin.getFilteredLogger().debug("Console set status for {} to: {}", target.getName(), status);
            }
        } else {
            sender.sendMessage("Invalid player name: " + args[1]);
            plugin.getFilteredLogger().debug("Invalid player name provided for setstatus (console): {}", args[1]);
        }
    }

    /**
     * Removes the status of another player (executed by a player or console).
     *
     * @param sender The sender of the command.
     * @param args   The arguments provided with the command.
     */
    private void removeOtherPlayerStatus(Player sender, String[] args) {
        if (!sender.hasPermission("StatusPlugin.admin.setStatus")) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED
                    + " You don't have permission to remove another player's status.");
            plugin.getFilteredLogger().debug("Player {} tried to remove status for another player without permission",
                    sender.getName());
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Usage: /tsp remove <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Player not found: " + args[1]);
            plugin.getFilteredLogger().debug("Player not found for remove status: {}", args[1]);
            return;
        }
        statusManager.removeStatus(target);
        sender.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + " Removed " + target.getName() + "'s status.");
        plugin.getFilteredLogger().debug("{} removed status for {}", sender.getName(), target.getName());
    }

    /**
     * Reloads the plugin configuration and statuses.
     * This method is called when a player with the appropriate permissions sends the
     * reload command.
     *
     * @param sender The sender of the command.
     */
    private void reloadPlugin(CommandSender sender) {
        if (!(sender instanceof Player) || sender.hasPermission("StatusPlugin.admin.reload")) {
            statusManager.reloadConfig();
            statusManager.reloadStatuses();
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + " Config & Statuses successfully reloaded");
            plugin.getFilteredLogger().info("{} reloaded the plugin.", sender.getName());
        } else {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " You don't have permission to reload statuses.");
            plugin.getFilteredLogger().debug("Player {} tried to reload without permission", sender.getName());
        }
    }

    /**
     * Handles the help command.
     * If the command has more than one argument and the second argument is
     * "colorcodes", it displays the color codes.
     * Otherwise, it displays the list of available commands.
     *
     * @param sender The sender of the command.
     * @param plugin The StatusPlugin instance.
     * @param args   The arguments provided with the command.
     */
    private void helpCommand(CommandSender sender, StatusPlugin plugin, String[] args) {
        if (args.length > 1 && "colorcodes".equals(args[1].toLowerCase())) {
            plugin.sendPluginMessages(sender, "title");
            displayColorCodes(sender, plugin);
            plugin.sendPluginMessages(sender, "line");
            plugin.getFilteredLogger().debug("{} requested color codes help.", sender.getName());
        } else {
            plugin.sendPluginMessages(sender, "title");
            sender.sendMessage(ChatColor.GOLD + "Available Commands:");

            // User commands
            sender.sendMessage(ChatColor.YELLOW + "User Commands:");
            if (sender.hasPermission("StatusPlugin.setStatus")) {
                sender.sendMessage(ChatColor.GREEN + "/status " + ChatColor.AQUA + "<status> " + ChatColor.GRAY + "- Set your own status" + (plugin.getConfigManager().isGroupMode() ? " (or group if group mode is on)" : "") + ".");
                sender.sendMessage(ChatColor.GREEN + "/status " + ChatColor.AQUA + "remove " + ChatColor.GRAY + "- Remove your own status" + (plugin.getConfigManager().isGroupMode() ? " (only if group mode is off)" : "") + ".");
            }
            sender.sendMessage(ChatColor.GREEN + "/tsp " + ChatColor.AQUA + "help colorcodes " + ChatColor.GRAY + "- Get all color codes to use in your status.");
            sender.sendMessage(ChatColor.GREEN + "/tsp " + ChatColor.AQUA + "info " + ChatColor.GRAY + "- Show info about the plugin.");

            // Admin commands
            if (sender.hasPermission("StatusPlugin.admin.setStatus") ||
                sender.hasPermission("StatusPlugin.admin.reload") ||
                sender.hasPermission("StatusPlugin.admin.setMaxlength") ||
                sender.hasPermission("StatusPlugin.admin.resetMaxlength")) {
                sender.sendMessage(ChatColor.YELLOW + "\nAdmin Commands:");

                if (sender.hasPermission("StatusPlugin.admin.setStatus")) {
                    sender.sendMessage(ChatColor.RED + "/tsp | /status " + ChatColor.AQUA + "remove <player> " +
                                      ChatColor.GRAY + "- Remove a player's status.");
                    sender.sendMessage(ChatColor.RED + "/tsp " + ChatColor.AQUA + "setstatus <player> <status> " +
                                      ChatColor.GRAY + "- Set a player's status.");
                    sender.sendMessage(ChatColor.RED + "/status " + ChatColor.AQUA + "<player> <status> " +
                                      ChatColor.GRAY + "- Set a player's status.");
                }

                if (sender.hasPermission("StatusPlugin.admin.reload")) {
                    sender.sendMessage(ChatColor.RED + "/tsp " + ChatColor.AQUA + "reload " +
                                      ChatColor.GRAY + "- Reload config and all statuses.");
                }

                if (sender.hasPermission("StatusPlugin.admin.setMaxlength")) {
                    sender.sendMessage(ChatColor.RED + "/tsp " + ChatColor.AQUA + "setmaxlength <length> " +
                                      ChatColor.GRAY + "- Set the max length of status.");
                }

                if (sender.hasPermission("StatusPlugin.admin.resetMaxlength")) {
                    sender.sendMessage(ChatColor.RED + "/tsp " + ChatColor.AQUA + "resetmaxlength " +
                                      ChatColor.GRAY + "- Reset the max length of status to default.");
                }
            }

            // Documentation link
            sender.sendMessage(ChatColor.YELLOW + "\nDocumentation:");
            TextComponent docsLink = new TextComponent(ChatColor.GOLD + "» " + ChatColor.LIGHT_PURPLE + "Click here for documentation" + ChatColor.GOLD + " «");
            docsLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://docs.pluginz.dev"));
            docsLink.setUnderlined(true);
            sender.spigot().sendMessage(docsLink);

            plugin.sendPluginMessages(sender, "line");
            plugin.getFilteredLogger().debug("{} requested general help.", sender.getName());
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
        sender.sendMessage(ChatColor.RED
                + "These color codes are only usable if u have the permissions for them, ask your Server Admin why you can't use specific colorcodes");
        plugin.getFilteredLogger().debug("Displayed color codes to {}", sender.getName());
    }

    /**
     * Handles the setmaxlength command.
     * If the sender has the appropriate permissions, it tries to set the maximum
     * status length.
     * If the command has two arguments, it tries to parse the second argument as an
     * integer and set the maximum status length to that value.
     * If the command does not have two arguments, it sends a usage message to the
     * sender.
     *
     * @param sender The sender of the command.
     * @param args   The arguments provided with the command.
     */
    private void setmaxlenghtCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || sender.hasPermission("StatusPlugin.admin.setMaxlength")) {
            if (args.length == 2) {
                try {
                    int maxLength = Integer.parseInt(args[1]);
                    statusManager.setMaxStatusLength(maxLength);
                    sender.sendMessage(
                            plugin.getPluginPrefix() + ChatColor.GREEN + " Max status length set to " + maxLength);
                    plugin.getFilteredLogger().info("{} set max status length to {}", sender.getName(), maxLength);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Invalid number format.");
                    plugin.getFilteredLogger().debug("{} provided invalid number format for setmaxlength: {}",
                            sender.getName(), args[1]);
                }
            } else {
                sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Usage: /tsp setmaxlength <length>");
            }
        } else {
            sender.sendMessage(
                    plugin.getPluginPrefix() + ChatColor.RED + " You don't have permission to set the maximum status length.");
            plugin.getFilteredLogger().debug("Player {} tried to set max length without permission", sender.getName());
        }
    }

    /**
     * Resets the maximum status length to its default value.
     * This method is called when a player with the appropriate permissions sends the
     * resetmaxlength command.
     *
     * @param sender The sender of the command.
     */
    private void resetmaxlenghtCommand(CommandSender sender) {
        if (!(sender instanceof Player) || sender.hasPermission("StatusPlugin.admin.resetMaxlength")) {
            statusManager.resetMaxStatusLength();
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.GREEN + " Max status length reset to default.");
            plugin.getFilteredLogger().info("{} reset max status length to default.", sender.getName());
        } else {
            sender.sendMessage(plugin.getPluginPrefix() + ChatColor.RED
                    + " You don't have permission to reset the maximum status length.");
            plugin.getFilteredLogger().debug("Player {} tried to reset max length without permission", sender.getName());
        }
    }

    /**
     * Displays information about the plugin to the sender.
     * This method is called when a player sends the info command.
     *
     * @param sender The sender of the command.
     * @param plugin The StatusPlugin instance.
     */
    public void infoCommand(CommandSender sender, StatusPlugin plugin) {
        if (!(plugin == null)) {
            plugin.sendPluginMessages(sender, "title");
        } else {
            sender.sendMessage(ChatColor.GOLD + "◢◤" + ChatColor.YELLOW + "Tu" + ChatColor.DARK_GREEN + "b's"
                    + ChatColor.DARK_AQUA + " Status" + ChatColor.GOLD + " Plugin" + ChatColor.YELLOW + "◥◣");
        }
        sender.sendMessage(ChatColor.GREEN + "Author: TubYoub");
        sender.sendMessage(ChatColor.GREEN + "Version: " + version);

        if (plugin.getConfigManager().isCheckUpdate() && plugin.getVersionInfo().isNewVersionAvailable) {
            sender.sendMessage(ChatColor.YELLOW + "A new version is available! Update at: " + ChatColor.UNDERLINE
                    + "https://modrinth.com/plugin/tubs-status-plugin/version/latest");
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
        sender.sendMessage(ChatColor.YELLOW + "\nDocumentation:");
            TextComponent docsLink = new TextComponent(ChatColor.GOLD + "» " + ChatColor.LIGHT_PURPLE + "Click here for documentation" + ChatColor.GOLD + " «");
            docsLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://docs.pluginz.dev"));
            docsLink.setUnderlined(true);
            sender.spigot().sendMessage(docsLink);
        plugin.sendPluginMessages(sender, "line");
        plugin.getFilteredLogger().debug("{} requested plugin info.", sender.getName());
    }
}
package de.tubyoub.statusplugin.commands;

import de.tubyoub.statusplugin.StatusManager;
import de.tubyoub.statusplugin.StatusPlugin;
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

public class StatusCommand implements CommandExecutor {
    private final StatusManager statusManager;

    public StatusCommand(StatusManager statusManager) {
        this.statusManager = statusManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        StatusPlugin plugin = (StatusPlugin) Bukkit.getPluginManager().getPlugin("StatusPlugin");

        if (args.length > 0 && "reload".equals(args[0])) {
            if (!sender.hasPermission("StatusPlugin.admin.reload")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to reload statuses.");
                return true;
            }
            statusManager.reloadStatuses();
            sender.sendMessage("Statuses have been reloaded.");
            return true;
        }

        if (args.length > 0 && "help".equals(args[0])) {
            if (args.length > 1 && "colors".equals(args[1])) {
                plugin.sendPluginMessages(sender, "title");
                sender.sendMessage("Here are all available color codes:");
                for (ChatColor color : ChatColor.values()) {
                    if (color.isColor()) {
                        sender.sendMessage(color + " - " + "&" + Integer.toHexString(color.ordinal()));
                    }
                }
                plugin.sendPluginMessages(sender, "line");
            } else {
                plugin.sendPluginMessages(sender, "title");
                sender.sendMessage("Here you can see all available commands:");
                sender.sendMessage("/status <status> - Set your own status.");
                sender.sendMessage("/status remove - Remove your Status.");
                sender.sendMessage("/status remove <player> - Remove a player's status. (Admin)");
                sender.sendMessage("/status <player> <status> - Set a player's status. (Admin)");
                sender.sendMessage("/status help colors - Show a list of color codes.");
                sender.sendMessage("/status reload - Reload all statuses. (Admin)");
                sender.sendMessage("/status info - Show info about the plugin.");
                plugin.sendPluginMessages(sender, "line");
            }
            return true;
        }

        if (args.length > 0 && "info".equals(args[0])) {
            plugin.sendPluginMessages(sender, "title");
            sender.sendMessage(ChatColor.GREEN + "Author: TubYoub");
            sender.sendMessage(ChatColor.GREEN + "Version: 1.0");

            TextComponent githubLink = new TextComponent(ChatColor.DARK_GRAY + "" + ChatColor.UNDERLINE + "GitHub");
            githubLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/TubYoub/StatusPlugin"));
            githubLink.setUnderlined(true);
            sender.spigot().sendMessage(githubLink);

            sender.sendMessage(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "Discord" + ChatColor.RESET + " is coming soon! (Maybe)");
            sender.sendMessage("If you have any issues please report them on GitHub.");

            plugin.sendPluginMessages(sender, "line");
            return true;
        }

        // If the first argument is a player's name, remove that player's status.
        if (args.length > 1) {
            if (!sender.hasPermission("StatusPlugin.admin.setStatus")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to remove another player's status.");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                statusManager.removeStatus(target);
                sender.sendMessage("Removed " + target.getName() + "'s status.");
                return true;
            }
            sender.sendMessage("Invalid player name: " + args[0]);
            return true;
        }

        // Otherwise, if the sender is a player, remove their own status.
        if (args.length > 0 && "remove".equals(args[0]) && sender.hasPermission("StatusPlugin.setStatus")) {
            Player playerToRemove = (Player) sender;
            statusManager.removeStatus(playerToRemove);
            playerToRemove.sendMessage("Your status has been removed.");
            return true;
        }

        if (args.length == 0 || args.length > 2) {
            player.sendMessage("Try using /status help");
            return true;
        }

        // If the first argument is a player's name, set that player's status.
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            if (!player.hasPermission("StatusPlugin.admin.setStatus")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to set another player's status.");
                return true;
            }
            String status = Arrays.stream(args, 1, args.length).collect(Collectors.joining(" "));
            statusManager.setStatus(target, status, sender);
            player.sendMessage("Set " + target.getName() + "'s status to: " + status);
            return true;
        }

        // Otherwise, set the command sender's status.
        if (!player.hasPermission("StatusPlugin.setStatus")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to set your own status.");
            return true;
        }
        String status = String.join(" ", args);
        statusManager.setStatus(player, status, sender);
        player.sendMessage("Your status has been set to: " + "[" + status + "]");
        return true;
    }
}
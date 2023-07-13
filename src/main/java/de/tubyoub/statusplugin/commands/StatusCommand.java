package de.tubyoub.statusplugin.commands;

import de.tubyoub.statusplugin.StatusManager;
import org.bukkit.Bukkit;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /status <status>");
            return true;
        }

        // If the first argument is a player's name, set that player's status.
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            if (!player.hasPermission("StatusPlugin.admin.setStatus")) {
                player.sendMessage("You don't have permission to set another player's status.");
                return true;
            }
            String status = Arrays.stream(args, 1, args.length).collect(Collectors.joining(" "));
            statusManager.setStatus(target, status);
            player.sendMessage("Set " + target.getName() + "'s status to: " + status);
            return true;
        }

        // Otherwise, set the command sender's status.
        if (!player.hasPermission("StatusPlugin.setStatus")) {
            player.sendMessage("You don't have permission to set your own status.");
            return true;
        }
        String status = String.join(" ", args);
        statusManager.setStatus(player, status);
        player.sendMessage("Your status has been set to: " + "[" + status + "]");
        return true;
    }
}
package de.tubyoub.statusplugin.Listener;

import de.tubyoub.statusplugin.StatusManager;
import de.tubyoub.utils.ColourUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatListener implements Listener {
    private final StatusManager statusManager;
    public ChatListener(StatusManager statusManager) {
        this.statusManager = statusManager;
    }
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        // Get the player and message
        Player player = event.getPlayer();
        String message = event.getMessage();
        String status = statusManager.getStatus(player);
        String broadcastMessage;

        if (status == null) {
            broadcastMessage = player.getName() + ": " + message;
        } else {
            broadcastMessage = "[" + ColourUtils.format(status) + ChatColor.RESET + "] " + player.getName() + ": " + message;
        }
        Bukkit.broadcastMessage(broadcastMessage);
        // Cancel the original event
        event.setCancelled(true);
    }
}

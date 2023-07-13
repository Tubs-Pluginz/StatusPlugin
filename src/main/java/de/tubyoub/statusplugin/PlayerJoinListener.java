package de.tubyoub.statusplugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final StatusManager statusManager;

    public PlayerJoinListener(StatusManager statusManager) {
        this.statusManager = statusManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String status = (String) statusManager.getStatus(player);
        if (status != null) {
            player.setDisplayName("[" + status + ChatColor.RESET + "] " + ChatColor.WHITE + player.getName());
            player.setPlayerListName("[" + status + ChatColor.RESET + "] " + ChatColor.WHITE + player.getName());
        }
    }
}
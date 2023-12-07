package de.tubyoub.statusplugin.Listener;

import de.tubyoub.statusplugin.StatusManager;
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
    statusManager.updateDisplayName(player);
}

}
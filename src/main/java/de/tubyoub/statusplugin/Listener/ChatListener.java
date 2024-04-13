package de.tubyoub.statusplugin.Listener;

import de.tubyoub.statusplugin.Managers.ConfigManager;
import de.tubyoub.statusplugin.Managers.StatusManager;
import de.tubyoub.utils.ColourUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

/**
 * Class implementing the Listener interface to handle player chat events.
 * When a player sends a chat message, the message is formatted based on the player's status.
 */
public class ChatListener implements Listener {
    private final StatusManager statusManager;
    private final ConfigManager configManager;

    /**
     * Constructor for the ChatListener class.
     * @param statusManager The StatusManager instance used to manage player statuses.
     * @param configManager The ConfigManager instance used to manage the plugin configuration.
     */
    public ChatListener(StatusManager statusManager, ConfigManager configManager) {
        this.statusManager = statusManager;
        this.configManager = configManager;
    }

    /**
     * Event handler for player chat events.
     * When a player sends a chat message, the message is formatted based on the player's status.
     * If the player has a status, it is added to the beginning of the message.
     * If the player does not have a status, the message is sent as is.
     * @param event The PlayerChatEvent to be handled.
     */
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (configManager.isChatFormatter()) {
            // Get the player and message
            Player player = event.getPlayer();
            String message = event.getMessage();

            // Translate the player's status and add placeholders
            String status = statusManager.translateColorsAndFormatting(statusManager.getStatus(player),player);
            status = PlaceholderAPI.setPlaceholders(player, status);

            // Format the broadcast message
            String broadcastMessage;
            if (status == null) {
                broadcastMessage = player.getName() + ": " + message;
            } else {
                broadcastMessage = "[" + ColourUtils.format(status) + ChatColor.RESET + "] " + player.getName() + ": " + message;
            }

            // Broadcast the message and cancel the original event
            Bukkit.broadcastMessage(broadcastMessage);
            event.setCancelled(true);
        }
    }
}
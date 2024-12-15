package de.tubyoub.statusplugin.Listener;

import de.tubyoub.statusplugin.Managers.ConfigManager;
import de.tubyoub.statusplugin.Managers.StatusManager;
import de.tubyoub.statusplugin.StatusPlugin;
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
    private final StatusPlugin plugin;

    /**
     * Constructor for the ChatListener class.
     */
    public ChatListener(StatusPlugin plugin) {
        this.plugin = plugin;
        this.statusManager = plugin.getStatusManager();
        this.configManager = plugin.getConfigManager();
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
            if (plugin.isPlaceholderAPIPresent()) {
                status = PlaceholderAPI.setPlaceholders(player, status);
            }

           // message = plugin.getStatusManager().translateColorsAndFormatting(message,player);

            // Format the broadcast message
            String broadcastMessage;
            if (status.isEmpty()) {
                broadcastMessage = player.getName() + ": " + plugin.getStatusManager().translateColorsAndFormatting(message,player);
            } else {
                broadcastMessage = configManager.getOpeningCharacter() + ColourUtils.format(status) + ChatColor.RESET + configManager.getClosingCharacter() + " " + player.getName() + ": " + plugin.getStatusManager().translateColorsAndFormatting(message,player);
            }

            // Broadcast the message and cancel the original event
            Bukkit.broadcastMessage(broadcastMessage);
            event.setCancelled(true);
        }
    }
}
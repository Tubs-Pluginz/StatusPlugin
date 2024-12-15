package de.tubyoub.statusplugin.Listener;

import de.tubyoub.statusplugin.Managers.StatusManager;
import de.tubyoub.statusplugin.StatusPlugin;
import de.tubyoub.utils.VersionChecker;
import de.tubyoub.utils.VersionChecker.UpdateUrgency;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Class implementing the Listener interface to handle player join events.
 * When a player joins, their display name is updated based on their status.
 */
public class PlayerJoinListener implements Listener {
    private final StatusManager statusManager;
    private final StatusPlugin plugin;
    private final VersionChecker.VersionInfo versionInfo;

    /**
     * Constructor for the PlayerJoinListener class.
     *
     * @param statusManager The StatusManager instance used to manage player statuses.
     */
    public PlayerJoinListener(StatusPlugin plugin, StatusManager statusManager) {
        this.statusManager = statusManager;
        this.plugin = plugin;
        this.versionInfo = plugin.getVersionInfo();
    }

    /**
     * Event handler for player join events.
     * When a player joins, their display name is updated based on their status.
     *
     * @param event The PlayerJoinEvent to be handled.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getConfigManager().isTablistFormatter()) {
            Player player = event.getPlayer();
            statusManager.updateDisplayName(player);
        }
        Player player = event.getPlayer();
        // Check if the player has admin privileges
        if (player.hasPermission("StatusPlugin.admin") && plugin.getConfigManager().isCheckUpdate()) {
            // Alert if a critical update is available
            if (this.versionInfo.isNewVersionAvailable && this.versionInfo.urgency == UpdateUrgency.CRITICAL || this.versionInfo.urgency == UpdateUrgency.HIGH) {
                player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " A critical update for Tubs StatusPlugin is available!");
                player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Please update to version: " + this.versionInfo.latestVersion);
                player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Backup your config");
                // only works this way and idk why
                TextComponent modrinthLink = new TextComponent(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "Modrinth");
                modrinthLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/plugin/bt-graves/version/" + this.versionInfo.latestVersion));
                modrinthLink.setUnderlined(true);
                TextComponent message = new TextComponent(plugin.getPluginPrefix() + ChatColor.RED + "Download the new version now from ");
                message.addExtra(modrinthLink);

                player.spigot().sendMessage(message);
            }
        }
    }
}
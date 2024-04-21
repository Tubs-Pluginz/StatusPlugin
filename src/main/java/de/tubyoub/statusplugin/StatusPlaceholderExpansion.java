package de.tubyoub.statusplugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * This class extends PlaceholderExpansion from the PlaceholderAPI.
 * It provides a way to register and use placeholders related to the StatusPlugin.
 */
public class StatusPlaceholderExpansion extends PlaceholderExpansion {

    private StatusPlugin plugin;

    /**
     * Constructor for StatusPlaceholderExpansion.
     * @param plugin The StatusPlugin instance.
     */
    public StatusPlaceholderExpansion(StatusPlugin plugin){
        this.plugin = plugin;
    }

    /**
     * This method is used to check if the expansion should persist through reloads.
     * @return true to persist.
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * This method is used to check if the expansion is able to register.
     * @return true if it can register.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * This method returns the author of the expansion.
     * @return The author's name.
     */
    @Override
    public String getAuthor(){
        return "TubYoub";
    }

    /**
     * This method returns the identifier of the expansion.
     * @return The identifier.
     */
    @Override
    public String getIdentifier(){
        return "tubsstatusplugin";
    }

    /**
     * This method returns the version of the plugin.
     * @return The plugin's version.
     */
    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    /**
     * This method is called when a placeholder with our identifier is found.
     * @param player The player who used the placeholder.
     * @param identifier The string passed to the placeholder.
     * @return The text that should replace the placeholder.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier){
        if(player == null){
            return "";
        }
        if (Objects.equals(identifier, "status")){
            return plugin.getStatusManager().getStatus(player);
        }

        // %tubsstatusplugin_status_playername%
        String[] parts = identifier.split("_");
        if(parts.length == 2 && parts[0].equals("status")){
            String playerName = parts[1];
            Player targetPlayer = Bukkit.getServer().getPlayer(playerName);
            if(targetPlayer != null){
                return plugin.getStatusManager().getStatus(targetPlayer);
            }
        }
        return null;
    }
}
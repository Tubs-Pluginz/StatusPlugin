package de.tubyoub.statusplugin.Managers;

import de.tubyoub.statusplugin.StatusPlugin;
import de.tubyoub.utils.ColourUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getLogger;

/**
 * Class responsible for managing player statuses.
 */
public class StatusManager {
    private final File statusFile;
    private final Map<UUID, String> statusMap = new HashMap<>();

    private static final int DEFAULT_MAX_LENGTH = 15;
    private int maxStatusLength = DEFAULT_MAX_LENGTH;
    private final StatusPlugin plugin;
    private ColourUtils chatColour;
    private final boolean placeholderAPIPresent;
    private ConfigManager configManager;

    /**
     * Constructor for the StatusManager class.
     * @param plugin The StatusPlugin instance.
     */
    public StatusManager(StatusPlugin plugin) {
        this.plugin = plugin;
        this.placeholderAPIPresent = plugin.isPlaceholderAPIPresent();
        this.configManager = plugin.getConfigManager();
        maxStatusLength = configManager.getMaxStatusLength();
        this.statusFile = new File(plugin.getDataFolder(), "statuses.yml");
        loadStatuses();
    }

    /**
     * Method to set the status of a player.
     * @param player The player whose status is to be set.
     * @param status The status to be set.
     * @param sender The sender of the command.
     * @return A boolean indicating whether the status was set successfully.
     */
    public boolean setStatus(Player player, String status, CommandSender sender) {
        if (configManager.isGroupMode() && !sender.hasPermission("StatusPlugin.admin.setStatus")) {
            sender.sendMessage(ChatColor.RED + "Group mode is enabled. Use /status group <groupname> to set your status.");
            return false;
        }

        if (status.contains("&_")) {
            status = status.replace("&_", " ");
        }
        String translatedStatus = translateColorsAndFormatting(status, sender);
        if (calculateEffectiveLength(translatedStatus) > maxStatusLength) {
            sender.sendMessage(ChatColor.RED + "Status is too long. Max length is " + maxStatusLength + " characters.");
            return false;
        }

        statusMap.put(player.getUniqueId(), status);
        if (configManager.isTablistFormatter()) {
            updateDisplayName(player);
        }
        saveStatuses();
        return true;
    }

    public boolean setGroupStatus(Player player, String groupName) {
        Map<String, String> statusGroups = configManager.getStatusGroups();
        if (!statusGroups.containsKey(groupName)) {
            player.sendMessage(ChatColor.RED + "Invalid group name.");
            return false;
        }

        if (!player.hasPermission("StatusPlugin.group.set" + groupName)) {
            player.sendMessage(plugin.getPluginPrefix() +  ChatColor.RED + "You don't have permission to use this status group.");
            return false;
        }

        String status = statusGroups.get(groupName);
        statusMap.put(player.getUniqueId(), status);
        if (configManager.isTablistFormatter()) {
            updateDisplayName(player);
        }
        saveStatuses();
        return true;
    }

    /**
     * Method to get the status of a player.
     * @param player The player whose status is to be retrieved.
     * @return The status of the player.
     */
    public String getStatus(Player player) {
        if (statusMap.containsKey(player.getUniqueId())){
            return statusMap.get(player.getUniqueId());
        }
        return "";
    }


   /**
     * Updates the display name of the player based on their status.
     * If the player has a status, it is translated and added to their display name.
     * If the player does not have a status, their display name is set to their name.
     * @param player The player whose display name is to be updated.
     */
    public void updateDisplayName(Player player) {
        String status = getStatus(player);

        if (status != null) {
            String translatedStatus = translateColorsAndFormatting(status, player);
            if (placeholderAPIPresent && player.hasPermission("StatusPlugin.placeholders")) {
                translatedStatus = PlaceholderAPI.setPlaceholders(player, translatedStatus);
            }
            String displayName = configManager.getOpeningCharacter() + translatedStatus + ChatColor.RESET + configManager.getClosingCharacter() + " " + ChatColor.WHITE + player.getName();
            displayName = ColourUtils.format(displayName);
            player.setDisplayName(displayName);
            player.setPlayerListName(displayName);
        } else {
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
        }
    }

    /**
     * Returns the maximum length of a status.
     * @return The maximum length of a status.
     */
    public int getMaxStatusLength() {
        return maxStatusLength;
    }

    /**
     * Sets the maximum length of a status.
     * @param maxLength The maximum length to be set.
     */
    public void setMaxStatusLength(int maxLength) {
        this.maxStatusLength = maxLength;
        configManager.setMaxStatusLength(maxLength);
        configManager.saveConfig();
    }

    /**
     * Resets the maximum length of a status to the default value.
     */
    public void resetMaxStatusLength() {
        this.maxStatusLength = DEFAULT_MAX_LENGTH;
        configManager.resetMaxStatusLength();
        configManager.saveConfig();
    }

    /**
     * Loads the statuses from the status file into the status map.
     */
    private void loadStatuses() {
        if (!statusFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(statusFile);
        for (String key : yaml.getKeys(false)) {
            statusMap.put(UUID.fromString(key), yaml.getString(key));
        }
    }

    /**
     * Saves the statuses from the status map into the status file.
     */
    public void saveStatuses() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : statusMap.entrySet()) {
            yaml.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            yaml.save(statusFile);
        } catch (IOException e) {
            getLogger().severe("Could not save statuses: " + e.getMessage());
        }
    }

    /**
     * Translates color and formatting codes in a status.
     * @param status The status to be translated.
     * @param sender The sender of the command.
     * @return The translated status.
     */
    public String translateColorsAndFormatting(String status, CommandSender sender) {
        String[] codes = {"&l", "&k", "&n", "&m", "&o"};
        String[] permissions = {
            "StatusPlugin.formatting.bold",
            "StatusPlugin.formatting.magic",
            "StatusPlugin.formatting.underline",
            "StatusPlugin.formatting.strikethrough",
            "StatusPlugin.formatting.italic"
        };

        if (!sender.hasPermission("StatusPlugin.formatting.color")) {
            status = removeColorCodes(status);
        }
        for (int i = 0; i < codes.length; i++) {
            if (status.contains(codes[i]) && !sender.hasPermission(permissions[i])) {
                status = status.replace(codes[i], "");
            }
        }
        return ChatColor.translateAlternateColorCodes('§', status);
    }

    /**
     * Removes color codes from a status.
     * @param status The status from which color codes are to be removed.
     * @return The status without color codes.
     */
    private String removeColorCodes(String status) {
        Pattern pattern = Pattern.compile("&[0-9a-fk-or]");
        Matcher matcher = pattern.matcher(status);
        return matcher.replaceAll("");
    }

    /**
     * Removes the status of a player.
     * @param player The player whose status is to be removed.
     */
    public void removeStatus(Player player) {
        statusMap.remove(player.getUniqueId());
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        saveStatuses();
    }

    /**
     * Calculates the effective length of a text string, ignoring color codes and placeholders.
     * @param text The text string whose effective length is to be calculated.
     * @return The effective length of the text string.
     */
    public int calculateEffectiveLength(String text) {
        Pattern pattern = Pattern.compile("&[0-9a-fk-or]|%[^%]+%");
        Matcher matcher = pattern.matcher(text);
        String withoutColorCodesAndPlaceholders = matcher.replaceAll("");
        return withoutColorCodesAndPlaceholders.length();
    }

    public boolean isGroupMode() {
        return configManager.isGroupMode();
    }

    public Map<String, String> getStatusGroups() {
        return configManager.getStatusGroups();
    }

    /**
     * Reloads the statuses from the status file into the status map.
     */
    public void reloadStatuses() {
        statusMap.clear();
        this.loadStatuses();
    }

    /**
     * Reloads the configuration from the configuration file.
     */
    public void reloadConfig(){
        configManager.reloadConfig();
    }
}
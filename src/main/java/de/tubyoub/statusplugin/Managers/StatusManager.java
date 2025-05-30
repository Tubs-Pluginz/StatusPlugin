package de.tubyoub.statusplugin.Managers;

import de.tubyoub.statusplugin.StatusPlugin;
import de.tubyoub.statusplugin.model.GroupConfig;
import de.tubyoub.utils.ColourUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     *
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
     *
     * @param player The player whose status is to be set.
     * @param status The status to be set.
     * @param sender The sender of the command.
     * @return A boolean indicating whether the status was set successfully.
     */
    public boolean setStatus(Player player, String status, CommandSender sender) {
        if (configManager.isGroupMode() && !(sender instanceof Player) && !sender.hasPermission("StatusPlugin.admin.setStatus")) {
            sender.sendMessage(ChatColor.RED + "Group mode is enabled. You must set a group status.");
            plugin.getFilteredLogger().debug("Player {} tried to set custom status in group mode without admin permission.", sender.getName());
            return false;
        }

        if (status.contains("&_")) {
            status = status.replace("&_", " ");
        }
        String translatedStatus = translateColorsAndFormatting(status, sender);
        if (calculateEffectiveLength(translatedStatus) > maxStatusLength) {
            sender.sendMessage(ChatColor.RED + "Status is too long. Max length is " + maxStatusLength + " characters.");
            plugin.getFilteredLogger().debug("Status '{}' for player {} is too long. Effective length: {}, Max length: {}",
                    status, player.getName(), calculateEffectiveLength(translatedStatus), maxStatusLength);
            return false;
        }

        statusMap.put(player.getUniqueId(), status);
        if (configManager.isTablistFormatter()) {
            updateDisplayName(player);
        }
        saveStatuses();
        plugin.getFilteredLogger().debug("Status for player {} set to '{}' by {}", player.getName(), status, sender.getName());
        return true;
    }

    /**
     * Method to set a player's status to a predefined group status.
     *
     * @param player    The player whose status is to be set.
     * @param groupName The name of the status group.
     * @return A boolean indicating whether the group status was set successfully.
     */
    public boolean setGroupStatus(Player player, String groupName) {
        Map<String, GroupConfig> statusGroups = configManager.getStatusGroups();
        plugin.getFilteredLogger().debug("Attempting to set group status for player {} to group {}", player.getName(), groupName);

        if (!statusGroups.containsKey(groupName)) {
            player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + " Invalid group name.");
            plugin.getFilteredLogger().debug("Invalid group name provided: {}", groupName);
            return false;
        }

        GroupConfig groupConfig = statusGroups.get(groupName);
        List<String> permissions = groupConfig.getPermissions();

        boolean hasPermission = false;
        if (permissions == null || permissions.isEmpty()) {
            // If no specific permissions are defined for the group, check the general group permission
            if (player.hasPermission("StatusPlugin.group.set")) {
                hasPermission = true;
                plugin.getFilteredLogger().debug("Player {} has general group permission for group {}", player.getName(), groupName);
            }
        } else {
            // Check if the player has any of the specific permissions defined for the group
            for (String perm : permissions) {
                if (player.hasPermission(perm)) {
                    hasPermission = true;
                    plugin.getFilteredLogger().debug("Player {} has specific permission {} for group {}", player.getName(), perm, groupName);
                    break;
                }
            }
        }

        if (!hasPermission && !player.hasPermission("StatusPlugin.admin.setStatus")) {
            player.sendMessage(plugin.getPluginPrefix() + ChatColor.RED + "You don't have permission to use this status group.");
            plugin.getFilteredLogger().debug("Player {} lacks permission to use group {}", player.getName(), groupName);
            return false;
        }

        String status = groupConfig.getStatus();
        statusMap.put(player.getUniqueId(), status);
        if (configManager.isTablistFormatter()) {
            updateDisplayName(player);
        }
        saveStatuses();
        plugin.getFilteredLogger().debug("Player {} status set to group '{}' ({})", player.getName(), groupName, status);
        return true;
    }

    /**
     * Method to get the status of a player.
     *
     * @param player The player whose status is to be retrieved.
     * @return The status of the player.
     */
    public String getStatus(Player player) {
        return statusMap.getOrDefault(player.getUniqueId(), "");
    }

    /**
     * Updates the display name of the player based on their status.
     * If the player has a status, it is translated and added to their display name.
     * If the player does not have a status, their display name is set to their name.
     *
     * @param player The player whose display name is to be updated.
     */
    public void updateDisplayName(Player player) {
        String status = getStatus(player);

        String translatedStatus = translateColorsAndFormatting(status, player);
        if (plugin.isLuckPermsPresent() && player.hasPermission("StatusPlugin.placeholders")) {
            String prefix = plugin.getLuckPerms().getPlayerAdapter(Player.class).getUser(player).getCachedData().getMetaData().getPrefix();
            String suffix = plugin.getLuckPerms().getPlayerAdapter(Player.class).getUser(player).getCachedData().getMetaData().getSuffix();

            if (prefix != null) {
                translatedStatus = translatedStatus.replace("%LP_prefix%", prefix);
            } else {
                translatedStatus = translatedStatus.replace("%LP_prefix%", "");
            }
            if (suffix != null) {
                translatedStatus = translatedStatus.replace("%LP_suffix%", suffix);
            } else {
                translatedStatus = translatedStatus.replace("%LP_suffix%", "");
            }
        }
        if (placeholderAPIPresent && player.hasPermission("StatusPlugin.placeholders")) {
            translatedStatus = PlaceholderAPI.setPlaceholders(player, translatedStatus);
        }

        String displayName;
        if (status.isEmpty()) {
            displayName = player.getName(); // No status, just use their name
    plugin.getFilteredLogger().debug("Player {} has no status, setting display name to {}", player.getName(), displayName.replace("§","&"));
        } else {
            displayName = configManager.getOpeningCharacter() + translatedStatus + ChatColor.RESET + configManager.getClosingCharacter() + " " + ChatColor.WHITE + player.getName();
            displayName = ColourUtils.format(displayName);
            plugin.getFilteredLogger().debug("Player {} has status '{}', setting display name to {}", player.getName(), status, displayName.replace("§","&"));
        }

        player.setDisplayName(displayName);
        player.setPlayerListName(displayName);
    }

    /**
     * Returns the maximum length of a status.
     *
     * @return The maximum length of a status.
     */
    public int getMaxStatusLength() {
        return maxStatusLength;
    }

    /**
     * Sets the maximum length of a status.
     *
     * @param maxLength The maximum length to be set.
     */
    public void setMaxStatusLength(int maxLength) {
        this.maxStatusLength = maxLength;
        configManager.setMaxStatusLength(maxLength);
        // ConfigManager.setMaxStatusLength already saves the config
        plugin.getFilteredLogger().debug("Max status length set to {}", maxLength);
    }

    /**
     * Resets the maximum length of a status to the default value.
     */
    public void resetMaxStatusLength() {
        this.maxStatusLength = DEFAULT_MAX_LENGTH;
        configManager.resetMaxStatusLength();
        // ConfigManager.resetMaxStatusLength already saves the config
        plugin.getFilteredLogger().debug("Max status length reset to default");
    }

    /**
     * Loads the statuses from the status file into the status map.
     */
    private void loadStatuses() {
        if (!statusFile.exists()) {
            plugin.getFilteredLogger().debug("Status file not found, no statuses loaded.");
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(statusFile);
        for (String key : yaml.getKeys(false)) {
            statusMap.put(UUID.fromString(key), yaml.getString(key));
        }
        plugin.getFilteredLogger().debug("Loaded {} statuses from file.", statusMap.size());
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
            plugin.getFilteredLogger().debug("Statuses saved to file. {} statuses saved.", statusMap.size());
        } catch (IOException e) {
            plugin.getFilteredLogger().error("Could not save statuses: {}", e.getMessage());
        }
    }

    /**
     * Translates color and formatting codes in a status.
     *
     * @param status The status to be translated.
     * @param sender The sender of the command (for permission checks).
     * @return The translated status.
     */
    public String translateColorsAndFormatting(String status, CommandSender sender) {
        String processedStatus = status;
        String[] codes = {"&l", "&k", "&n", "&m", "&o"};
        String[] permissions = {
                "StatusPlugin.formatting.bold",
                "StatusPlugin.formatting.magic",
                "StatusPlugin.formatting.underline",
                "StatusPlugin.formatting.strikethrough",
                "StatusPlugin.formatting.italic"
        };

        if (!(sender instanceof Player) || !sender.hasPermission("StatusPlugin.formatting.color")) {
            processedStatus = removeColorCodes(processedStatus);
            plugin.getFilteredLogger().debug("Removing color codes for sender {} (no color permission).", sender.getName());
        }
        for (int i = 0; i < codes.length; i++) {
            if (processedStatus.contains(codes[i]) && (!(sender instanceof Player) || !sender.hasPermission(permissions[i]))) {
                processedStatus = processedStatus.replace(codes[i], "");
                plugin.getFilteredLogger().debug("Removing formatting code {} for sender {} (no permission).", codes[i], sender.getName());
            }
        }
        String translated = ChatColor.translateAlternateColorCodes('&', processedStatus);
        plugin.getFilteredLogger().debug("Translated status/message '{}' to '{}' for sender {}", status, translated.replace("§","'Paragraph symbol' "), sender.getName());
        return translated;
    }

    /**
     * Removes color codes from a status.
     *
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
     *
     * @param player The player whose status is to be removed.
     */
    public void removeStatus(Player player) {
        statusMap.remove(player.getUniqueId());
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        saveStatuses();
        plugin.getFilteredLogger().debug("Status removed for player {}", player.getName());
    }

    /**
     * Calculates the effective length of a text string, ignoring color codes and
     * placeholders.
     *
     * @param text The text string whose effective length is to be calculated.
     * @return The effective length of the text string.
     */
    public int calculateEffectiveLength(String text) {
        // Remove color codes (&x) and PlaceholderAPI placeholders (%...%)
        String cleanedText = text.replaceAll("&[0-9a-fk-or]", "").replaceAll("%[^%]+%", "");
        plugin.getFilteredLogger().debug("Calculated effective length of '{}' (cleaned: '{}') as {}", text.replace("§","'paragraph symbol"), cleanedText.replace("§","'paragraph symbol"), cleanedText.length());
        return cleanedText.length();
    }

    public boolean isGroupMode() {
        return configManager.isGroupMode();
    }

    public Map<String, GroupConfig> getStatusGroups() {
        return configManager.getStatusGroups();
    }

    /**
     * Reloads the statuses from the status file into the status map.
     */
    public void reloadStatuses() {
        statusMap.clear();
        this.loadStatuses();
        plugin.getFilteredLogger().info("Statuses reloaded.");
    }

    /**
     * Reloads the configuration from the configuration file.
     */
    public void reloadConfig() {
        configManager.reloadConfig();
    }
}

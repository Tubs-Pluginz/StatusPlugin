package de.tubyoub.statusplugin.Managers;

import de.tubyoub.statusplugin.StatusPlugin;
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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import de.tubyoub.utils.ColourUtils;

import static org.bukkit.Bukkit.getConsoleSender;
import static org.bukkit.Bukkit.getLogger;

public class StatusManager {
    private final File statusFile;
    private final Map<UUID, String> statusMap = new HashMap<>();

    private static final int DEFAULT_MAX_LENGTH = 15;
    private int maxStatusLength = DEFAULT_MAX_LENGTH;
    private final StatusPlugin plugin;
    private ColourUtils chatColour;
    private final boolean placeholderAPIPresent;

    public StatusManager(StatusPlugin plugin) {
        this.plugin = plugin;
        this.placeholderAPIPresent = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        this.maxStatusLength = plugin.getConfig().getInt("maxStatusLength", DEFAULT_MAX_LENGTH);
        this.statusFile = new File(plugin.getDataFolder(), "statuses.yml");
        loadStatuses();
    }

    public boolean setStatus(Player player, String status, CommandSender sender) {
        String translatedStatus = translateColorsAndFormatting(status, sender);
        if (calculateEffectiveLength(translatedStatus) > maxStatusLength) {
            sender.sendMessage(ChatColor.RED + "Status is too long. Max length is " + maxStatusLength + " characters.");
            return false;
        }

        // Store the original status, not the translated one
        statusMap.put(player.getUniqueId(), status);
        updateDisplayName(player);
        saveStatuses();
        return true;
    }

    public String getStatus(Player player) {
        return statusMap.get(player.getUniqueId());
    }

    public void updateDisplayName(Player player) {
        String status = getStatus(player);
        if (status != null) {
            // Translate the status here
            String translatedStatus = translateColorsAndFormatting(status, player);
            if (placeholderAPIPresent && player.hasPermission("StatusPlugin.placeholders")) {
                translatedStatus = PlaceholderAPI.setPlaceholders(player, translatedStatus);
            }
            String displayName = "[" + translatedStatus + ChatColor.RESET + "] " + ChatColor.WHITE + player.getName();
            displayName = ColourUtils.format(displayName); // Assign the result back to displayName
            player.setDisplayName(displayName);
            player.setPlayerListName(displayName);
        } else {
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
        }
    }
    public int getMaxStatusLength() {
        return maxStatusLength;
    }

    public void setMaxStatusLength(int maxLength) {
        this.maxStatusLength = maxLength;
        plugin.getConfig().set("maxStatusLength", maxLength);
        plugin.saveConfig();
    }

    public void resetMaxStatusLength() {
        this.maxStatusLength = DEFAULT_MAX_LENGTH;
        plugin.getConfig().set("maxStatusLength", DEFAULT_MAX_LENGTH);
        plugin.saveConfig();
    }

    private void loadStatuses() {
        if (!statusFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(statusFile);
        for (String key : yaml.getKeys(false)) {
            statusMap.put(UUID.fromString(key), yaml.getString(key));
        }
    }

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

    private String removeColorCodes(String status) {
        Pattern pattern = Pattern.compile("&[0-9a-fk-or]");
        Matcher matcher = pattern.matcher(status);
        return matcher.replaceAll("");
    }

    public void removeStatus(Player player) {
        statusMap.remove(player.getUniqueId());
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        saveStatuses();
    }
    public int calculateEffectiveLength(String text) {
        Pattern pattern = Pattern.compile("&[0-9a-fk-or]|%[^%]+%");
        Matcher matcher = pattern.matcher(text);
        String withoutColorCodesAndPlaceholders = matcher.replaceAll("");
        return withoutColorCodesAndPlaceholders.length();
    }
    public void reloadStatuses() {
        statusMap.clear();
        loadStatuses();
    }
}
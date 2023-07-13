package de.tubyoub.statusplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatusManager {
    private final File statusFile;
    private final Map<UUID, String> statusMap = new HashMap<>();

    public StatusManager(StatusPlugin plugin) {
        this.statusFile = new File(plugin.getDataFolder(), "statuses.yml");
        loadStatuses();
    }

     public void setStatus(Player player, String status, CommandSender sender) {
        status = translateColorsAndFormatting(status, sender);
        statusMap.put(player.getUniqueId(), status);
        updateDisplayName(player);
        saveStatuses();
    }

    public String getStatus(Player player) {
        return statusMap.get(player.getUniqueId());
    }

    public void updateDisplayName(Player player) {
        String status = statusMap.get(player.getUniqueId());
        if (status != null) {
            player.setDisplayName("[" + status + ChatColor.RESET + "] " + ChatColor.WHITE + player.getName());
            player.setPlayerListName("[" + status + ChatColor.RESET + "] " + ChatColor.WHITE + player.getName());
        }
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
            e.printStackTrace();
        }
    }

    public String translateColorsAndFormatting(String status, CommandSender sender) {
        if (sender.hasPermission("StatusPlugin.formatting")) {
            return ChatColor.translateAlternateColorCodes('&', status);
        } else {
            return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', status));
        }
    }

    public void removeStatus(Player player) {
        statusMap.remove(player.getUniqueId());
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        saveStatuses();
}


    public void reloadStatuses() {
        statusMap.clear();
        loadStatuses();
    }
}
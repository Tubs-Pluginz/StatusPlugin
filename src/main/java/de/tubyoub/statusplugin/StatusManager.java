package de.tubyoub.statusplugin;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StatusManager {
    private final File statusFile;
    private final Map<String, Object> statusMap = new HashMap<String, Object>();

    public StatusManager(StatusPlugin plugin) {
        this.statusFile = new File(plugin.getDataFolder(), "statuses.yml");
        loadStatuses();
    }

    public void setStatus(Player player, String status) {
        statusMap.put(player.getName(), status);
        player.setDisplayName(status + " " + player.getName());
        player.setPlayerListName(status + " " + player.getName());
        saveStatuses();
    }

    public Object getStatus(Player player) {
        return statusMap.get(player.getName());
    }

    private void loadStatuses() {
        if (!statusFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(statusFile);
        statusMap.putAll(yaml.getValues(false));
    }

    private void saveStatuses() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, Object> entry : statusMap.entrySet()) {
            yaml.set(entry.getKey(), entry.getValue());
        }
        try {
            yaml.save(statusFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
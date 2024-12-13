package de.tubyoub.statusplugin.Managers;

import de.tubyoub.statusplugin.StatusPlugin;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.MergeRule;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigManager {
    private YamlDocument config;
    private int maxStatusLength;
    private boolean checkUpdate;
    private boolean chatFormatter;
    private boolean tablistFormatter;
    private boolean groupMode;
    private String openingCharacter;
    private String closingCharacter;
    private Map<String, String> statusGroups;
    private final StatusPlugin plugin;

    public ConfigManager(StatusPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        try {
            config = YamlDocument.create(new File(plugin.getDataFolder(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("fileversion"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                            .setMergeRule(MergeRule.MAPPINGS, true)
                            .setMergeRule(MergeRule.MAPPING_AT_SECTION, true)
                            .setMergeRule(MergeRule.SECTION_AT_MAPPING, true)
                            .setKeepAll(true)
                            .build());

            maxStatusLength = config.getInt("maxStatusLength", 15);
            checkUpdate = config.getBoolean("checkUpdate", true);
            chatFormatter = config.getBoolean("chatFormatter", true);
            tablistFormatter = config.getBoolean("changeTablistNames", true);
            groupMode = config.getBoolean("groupMode", false);
            openingCharacter = config.getString("openingCharacter", "[");
            closingCharacter = config.getString("closingCharacter", "]");
            loadStatusGroups();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load configuration: " + e.getMessage());
        }
    }

    private void loadStatusGroups() {
        statusGroups = new HashMap<>();
        if (config.contains("statusGroups")) {
            for (Object key : config.getSection("statusGroups").getKeys()) {
                String groupName = key.toString();
                String status = config.getString("statusGroups." + groupName + ".status");
                statusGroups.put(groupName, status);
            }
        }
    }

    public void saveConfig() {
        try {
            config.save();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save configuration: " + e.getMessage());
        }
    }

    public boolean isTablistFormatter() {
        return tablistFormatter;
    }

    public void setTablistFormatter(boolean tablistFormatter) {
        if (this.tablistFormatter != tablistFormatter) {
            this.tablistFormatter = tablistFormatter;
            config.set("changeTablistNames", tablistFormatter);
        }
    }

    public boolean isChatFormatter() {
        return chatFormatter;
    }

    public void setChatFormatter(boolean chatFormatter) {
        if (this.chatFormatter != chatFormatter) {
            this.chatFormatter = chatFormatter;
            config.set("chatFormatter", chatFormatter);
        }
    }

    public int getMaxStatusLength() {
        return maxStatusLength;
    }

    public void setMaxStatusLength(int maxLength) {
        this.maxStatusLength = maxLength;
        config.set("maxStatusLength", maxLength);
        saveConfig();
    }

    public void resetMaxStatusLength() {
        this.maxStatusLength = 15;
        config.set("maxStatusLength", 15);
        saveConfig();
    }

    public boolean isCheckUpdate() {
        return checkUpdate;
    }

    public boolean isGroupMode() {
        return groupMode;
    }

    public void setGroupMode(boolean groupMode) {
        this.groupMode = groupMode;
        config.set("groupMode", groupMode);
        saveConfig();
    }
    public String getOpeningCharacter() {
        return openingCharacter;
    }
    public String getClosingCharacter() {
        return closingCharacter;
    }

    public Map<String, String> getStatusGroups() {
        return statusGroups;
    }

    public void reloadConfig() {
        loadConfig();
    }
}
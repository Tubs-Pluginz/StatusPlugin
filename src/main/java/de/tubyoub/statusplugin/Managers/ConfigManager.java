package de.tubyoub.statusplugin.Managers;

import de.tubyoub.statusplugin.StatusPlugin;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ConfigManager {
    private YamlDocument config;
    private int maxStatusLength;
    private boolean chatFormatter;
    private boolean changeTabListNames;
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
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build());

            maxStatusLength = config.getInt("maxStatusLength", 15);
            chatFormatter = config.getBoolean("chatFormatter", true);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load configuration: " + e.getMessage());
        }
    }

    public void saveConfig() {
        try {
            config.save();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save configuration: " + e.getMessage());
        }
    }

    public boolean isChatFormatter(){
        return chatFormatter;
    }
    public void setChatFormatter(boolean chatFormatter){
        if (this.chatFormatter == chatFormatter){
            return;
        }else {
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

    public void reloadConfig() {
        loadConfig();
    }
}
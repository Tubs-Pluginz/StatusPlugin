package de.tubyoub.statusplugin;

import de.tubyoub.statusplugin.Listener.ChatListener;
import de.tubyoub.statusplugin.Listener.PlayerJoinListener;
import de.tubyoub.statusplugin.Managers.ConfigManager;
import de.tubyoub.statusplugin.Managers.StatusManager;
import de.tubyoub.statusplugin.commands.StatusGeneralCommand;
import de.tubyoub.statusplugin.commands.StatusSetCommand;
import de.tubyoub.statusplugin.commands.tabCompleter.StatusGeneralTabCompleter;
import de.tubyoub.statusplugin.commands.tabCompleter.StatusSetTabCompleter;
import de.tubyoub.utils.FilteredComponentLogger;
import de.tubyoub.utils.VersionChecker;
import de.tubyoub.statusplugin.metrics.Metrics;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.event.Level;

/**
 * Main class for the StatusPlugin.
 * This class extends JavaPlugin and represents the main entry point for the
 * plugin.
 */
public class StatusPlugin extends JavaPlugin {
    private final String version = "1.6";
    private final String project = "km0yAITg";
    private int pluginId = 20463;
    private StatusManager statusManager;
    private VersionChecker versionChecker;
    private boolean placeholderAPIPresent = false;
    private ConfigManager configManager;
    private StatusPlaceholderExpansion placeholderExpansion;
    private VersionChecker.VersionInfo versionInfo;
    private LuckPerms luckPerms;
    private boolean luckPermsPresent = false;
    private FilteredComponentLogger filteredLogger;

    /**
     * This method is called when the plugin is enabled.
     * It initializes the plugin's components and registers the necessary listeners
     * and commands.
     */
    @Override
    public void onEnable() {
        ComponentLogger componentLogger = ComponentLogger.logger(this.getClass());
        filteredLogger = new FilteredComponentLogger(componentLogger, Level.INFO);

        filteredLogger.info("______________________________");
        filteredLogger.info("\\__    ___/   _____/\\______   \\");
        filteredLogger.info("  |    |  \\_____  \\  |     ___/");
        filteredLogger.info("  |    |  /        \\ |    |");
        filteredLogger.info("  |____| /_______  / |____|" + "     TubsStatusPlugin v" + version);
        filteredLogger.info("                 \\/            " + " Running on "
                + Bukkit.getServer().getName() + " using Blackmagic");

        // Initialize the ConfigManager and load the configuration
        this.configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize the StatusManager and VersionChecker
        this.statusManager = new StatusManager(this);
        if (configManager.isCheckUpdate()) {
            versionInfo = VersionChecker.isNewVersionAvailable(version, project);
            if (versionInfo.isNewVersionAvailable) {
                switch (versionInfo.urgency) {
                    case CRITICAL, HIGH:
                        this.filteredLogger.warn("--- Important Update --- ");
                        this.filteredLogger.warn("There is a new critical update for Tubs Status Plugin available");
                        this.filteredLogger.warn("please update NOW");
                        this.filteredLogger.warn(
                                "https://modrinth.com/plugin/tubs-status-plugin/version/"
                                        + versionInfo.latestVersion);
                        this.filteredLogger.warn("backup your config");
                        this.filteredLogger.warn("---");
                        break;
                    case NORMAL, LOW, NONE:
                        this.filteredLogger.warn("There is a new update for Tubs Status Plugin available");
                        this.filteredLogger.warn(
                                "https://modrinth.com/plugin/tubs-status-plugin/version/"
                                        + versionInfo.latestVersion);
                        this.filteredLogger.warn("backup your config");
                        break;
                }
            } else {
                this.filteredLogger.info(" You are running the latest version of Tubs Status Plugin");
            }
        } else {
            this.filteredLogger.info(
                    "You have automatic checks for new updates disabled. Enable them in the config to stay up to date");
        }

        // Register the PlayerJoinListener and ChatListener
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, this.statusManager), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // Set the executor and tab completer for the "status" command (for setting
        // status)
        StatusSetCommand statusSetCommand = new StatusSetCommand(this);
        getCommand("status").setExecutor(statusSetCommand);
        getCommand("status").setTabCompleter(new StatusSetTabCompleter(this));

        // Set the executor and tab completer for the "tsp" command (for general
        // commands)
        StatusGeneralCommand statusGeneralCommand = new StatusGeneralCommand(statusManager, versionInfo.isNewVersionAvailable,
                version);
        getCommand("tsp").setExecutor(statusGeneralCommand);
        getCommand("tsp").setTabCompleter(new StatusGeneralTabCompleter(this));

        // Initialize the Metrics
        Metrics metrics = new Metrics(this, pluginId);

        // Check if PlaceholderAPI is present and register the
        // StatusPlaceholderExpansion if it is
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new StatusPlaceholderExpansion(this);
            placeholderExpansion.register();
            placeholderAPIPresent = true;
            filteredLogger.info("Tub's StatusPlugin will now use PlaceholderAPI");
        } else {
            filteredLogger.warn("Could not find PlaceholderAPI! Tub's StatusPlugin will run without it..");
        }

        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
            luckPermsPresent = true;
            filteredLogger.info("Tub's StatusPlugin will now hook into LuckPerms");
        } else {
            filteredLogger.warn("Could not find LuckPerms! Tub's StatusPlugin will run without it..");
        }

        // Schedule a task to update the display name of online players every 30
        // seconds
        if (configManager.isTablistFormatter()) {
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    statusManager.updateDisplayName(player);
                }
            }, 0L, 600L); // 600 ticks = 30 seconds
        }
        filteredLogger.info("Tub's StatusPlugin successfully loaded");
    }

    /**
     * Method to send plugin messages.
     *
     * @param sender The sender of the command.
     * @param type   The type of the message to be sent.
     */
    public void sendPluginMessages(CommandSender sender, String type) {
        if ("title".equals(type)) {
            sender.sendMessage(ChatColor.GOLD + "◢◤" + ChatColor.YELLOW + "Tu" + ChatColor.DARK_GREEN + "b's"
                    + ChatColor.DARK_AQUA + " Status" + ChatColor.GOLD + " Plugin" + ChatColor.YELLOW + "◥◣");
        } else if ("line".equals(type)) {
            sender.sendMessage(ChatColor.GOLD + "-" + ChatColor.YELLOW + "-" + ChatColor.GREEN + "-"
                    + ChatColor.DARK_GREEN + "-" + ChatColor.BLUE + "-" + ChatColor.DARK_AQUA + "-"
                    + ChatColor.GOLD + "-" + ChatColor.YELLOW + "-" + ChatColor.GREEN + "-"
                    + ChatColor.DARK_GREEN + "-" + ChatColor.BLUE + "-" + ChatColor.DARK_AQUA + "-"
                    + ChatColor.GOLD + "-" + ChatColor.YELLOW + "-" + ChatColor.GREEN + "-"
                    + ChatColor.DARK_GREEN + "-" + ChatColor.BLUE + "-" + ChatColor.DARK_AQUA + "-"
                    + ChatColor.GOLD + "-");
        }
    }

    /**
     * Method to get the plugin prefix.
     *
     * @return The plugin prefix.
     */
    public String getPluginPrefix() {
        return ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "TSP" + ChatColor.WHITE + "]";
    }

    /**
     * Method to get the ConfigManager.
     *
     * @return The ConfigManager.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Method to get the StatusManager.
     *
     * @return The StatusManager.
     */
    public StatusManager getStatusManager() {
        return statusManager;
    }

    public boolean isPlaceholderAPIPresent() {
        return placeholderAPIPresent;
    }

    public VersionChecker.VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public boolean isLuckPermsPresent() {
        return luckPermsPresent;
    }

    public FilteredComponentLogger getFilteredLogger() {
        return filteredLogger;
    }

    /**
     * This method is called when the plugin is disabled.
     * It saves the statuses.
     */
    @Override
    public void onDisable() {
        statusManager.saveStatuses();
    }
}

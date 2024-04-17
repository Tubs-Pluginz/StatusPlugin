package de.tubyoub.statusplugin;

import de.tubyoub.statusplugin.Listener.ChatListener;
import de.tubyoub.statusplugin.Listener.PlayerJoinListener;
import de.tubyoub.statusplugin.Managers.ConfigManager;
import de.tubyoub.statusplugin.Managers.StatusManager;
import de.tubyoub.statusplugin.commands.StatusCommand;
import de.tubyoub.utils.VersionChecker;
import de.tubyoub.statusplugin.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the StatusPlugin.
 * This class extends JavaPlugin and represents the main entry point for the plugin.
 */
public class StatusPlugin extends JavaPlugin {
    private final String version = "1.3.4";
    private StatusManager statusManager;
    private VersionChecker versionChecker;
    //private boolean placeholderAPIPresent;
    private ConfigManager configManager;
    private StatusPlaceholderExpansion placeholderExpansion;
    private boolean newVersion;
    private int pluginId = 20463;

    /**
     * This method is called when the plugin is enabled.
     * It initializes the plugin's components and registers the necessary listeners and commands.
     */
    @Override
    public void onEnable() {
        getLogger().info( "______________________________");
        getLogger().info("\\__    ___/   _____/\\______   \\");
        getLogger().info( "  |    |  \\_____  \\  |     ___/");
        getLogger().info( "  |    |  /        \\ |    |");
        getLogger().info( "  |____| /_______  / |____|" + "     TubsStatusPlugin v"+ version);
        getLogger().info( "                 \\/            "+ " Running on " + Bukkit.getServer().getName()  + " using Blackmagic");

        // Initialize the ConfigManager and load the configuration
        this.configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize the StatusManager and VersionChecker
        this.statusManager = new StatusManager(this);
        this.versionChecker = new VersionChecker();
        newVersion = VersionChecker.isNewVersionAvailable(version);

        // Register the PlayerJoinListener and ChatListener
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this ,this.statusManager), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this.statusManager, configManager), this);

        // Set the executor and tab completer for the "status" command
        StatusCommand statusCommand = new StatusCommand(statusManager,newVersion,version);
        getCommand("status").setExecutor(statusCommand);
        getCommand("status").setTabCompleter(new StatusTabCompleter());

        // Initialize the Metrics
        Metrics metrics = new Metrics(this, pluginId);

        // Check if PlaceholderAPI is present and register the StatusPlaceholderExpansion if it is
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new StatusPlaceholderExpansion(this);
            placeholderExpansion.register();
            getLogger().info("Tub's StatusPlugin will now use PlaceholderAPI");
        } else {
            getLogger().warning("Could not find PlaceholderAPI! Tub's StatusPlugin will run without it..");
        }

        // Schedule a task to update the display name of online players every 30 seconds
        if (configManager.isTablistFormatter()) {
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    statusManager.updateDisplayName(player);
                }
            }, 0L, 600L); // 600 ticks = 30 seconds
        }
        getLogger().info("Tub's StatusPlugin successfully loaded");
        getLogger().warning(String.valueOf(this.getConfig()));
    }

    /**
     * Method to send plugin messages.
     * @param sender The sender of the command.
     * @param type The type of the message to be sent.
     */
    public void sendPluginMessages(CommandSender sender, String type) {
        if ("title".equals(type)) {
            sender.sendMessage(ChatColor.GOLD + "◢◤" + ChatColor.YELLOW + "Tu" + ChatColor.DARK_GREEN + "b's" + ChatColor.DARK_AQUA + " Status" + ChatColor.GOLD + " Plugin" + ChatColor.YELLOW + "◥◣");
        } else if ("line".equals(type)) {
            sender.sendMessage(ChatColor.GOLD + "-" + ChatColor.YELLOW + "-" + ChatColor.GREEN + "-" + ChatColor.DARK_GREEN + "-" + ChatColor.BLUE + "-" + ChatColor.DARK_AQUA + "-"
                + ChatColor.GOLD + "-" + ChatColor.YELLOW + "-" + ChatColor.GREEN + "-" + ChatColor.DARK_GREEN + "-" + ChatColor.BLUE + "-" + ChatColor.DARK_AQUA + "-"
                + ChatColor.GOLD + "-" + ChatColor.YELLOW + "-" + ChatColor.GREEN + "-" + ChatColor.DARK_GREEN + "-" + ChatColor.BLUE + "-" + ChatColor.DARK_AQUA + "-"
                + ChatColor.GOLD + "-");
        }
    }

    /**
     * Method to get the plugin prefix.
     * @return The plugin prefix.
     */
    public String getPluginPrefix() {
        return ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "TSP" + ChatColor.WHITE + "]";
    }

    /**
     * Method to get the ConfigManager.
     * @return The ConfigManager.
     */
    public ConfigManager getConfigManager(){
        return configManager;
    }

    /**
     * Method to get the StatusManager.
     * @return The StatusManager.
     */
    public StatusManager getStatusManager(){
        return statusManager;
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
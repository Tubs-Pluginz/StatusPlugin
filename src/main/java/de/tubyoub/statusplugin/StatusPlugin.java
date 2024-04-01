package de.tubyoub.statusplugin;

import de.tubyoub.statusplugin.Listener.ChatListener;
import de.tubyoub.statusplugin.Listener.PlayerJoinListener;
import de.tubyoub.statusplugin.Managers.StatusManager;
import de.tubyoub.statusplugin.commands.StatusCommand;
import de.tubyoub.statusplugin.commands.VersionChecker;
import de.tubyoub.statusplugin.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class StatusPlugin extends JavaPlugin {
    private final String version = "1.3.2";
    private StatusManager statusManager;
    private VersionChecker versionChecker;
    //private boolean placeholderAPIPresent;
    private int pluginId = 20463;

    @Override
    public void onEnable() {
        getLogger().info( "______________________________");
        getLogger().info("\\__    ___/   _____/\\______   \\");
        getLogger().info( "  |    |  \\_____  \\  |     ___/");
        getLogger().info( "  |    |  /        \\ |    |");
        getLogger().info( "  |____| /_______  / |____|" + "     TubsStatusPlugin v"+ version);
        getLogger().info( "                 \\/            "+ " Running on " + Bukkit.getServer().getName()  + " using Blackmagic");
        this.statusManager = new StatusManager(this);
        this.saveDefaultConfig();
        this.versionChecker = new VersionChecker();
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this.statusManager), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this.statusManager), this);
        StatusCommand statusCommand = new StatusCommand(statusManager,versionChecker,version);
        //getCommand("status").setExecutor(new StatusCommand(this.statusManager,versionChecker));
        getCommand("status").setExecutor(statusCommand);
        getCommand("status").setTabCompleter(new StatusTabCompleter());

        Metrics metrics = new Metrics(this, pluginId);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
           // new StatusPlaceholderExpansion(this).register();
            //this.placeholderAPIPresent = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
            getLogger().info("Tub's StatusPlugin will now use PlaceholderAPI");
        } else {
            getLogger().warning("Could not find PlaceholderAPI! Tub's StatusPlugin will run without it..");
        }


        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                statusManager.updateDisplayName(player);
            }
        }, 0L, 600L); // 600 ticks = 30 seconds
        getLogger().info("Tub's StatusPlugin successfully loaded");
    }

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
    @Override
    public void onDisable() {
       statusManager.saveStatuses();
    }
}

package de.tubyoub.statusplugin;

import de.tubyoub.statusplugin.Listener.ChatListener;
import de.tubyoub.statusplugin.Listener.PlayerJoinListener;
import de.tubyoub.statusplugin.commands.StatusCommand;
import de.tubyoub.statusplugin.commands.VersionChecker;
import de.tubyoub.statusplugin.metrics.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class StatusPlugin extends JavaPlugin {
    private StatusManager statusManager;
    private VersionChecker versionChecker;
    private int pluginId = 20463;

    @Override
    public void onEnable() {
        this.statusManager = new StatusManager(this);
        this.saveDefaultConfig();
        this.versionChecker = new VersionChecker();
        StatusCommand statusCommand = new StatusCommand(statusManager,versionChecker);
        getCommand("status").setExecutor(new StatusCommand(this.statusManager,versionChecker));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this.statusManager), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this.statusManager), this);
        getCommand("status").setExecutor(statusCommand);
        getCommand("status").setTabCompleter(new StatusTabCompleter());

        Metrics metrics = new Metrics(this, pluginId);
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

package de.tubyoub.statusplugin;

import de.tubyoub.statusplugin.commands.StatusCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class StatusPlugin extends JavaPlugin {
    private StatusManager statusManager;

    @Override
    public void onEnable() {
        this.statusManager = new StatusManager(this);
        this.saveDefaultConfig();
        this.statusManager = new StatusManager(this);
        StatusCommand statusCommand = new StatusCommand(statusManager);
        getCommand("status").setExecutor(new StatusCommand(this.statusManager));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this.statusManager), this);
        getCommand("status").setExecutor(statusCommand);
        getCommand("status").setTabCompleter(new StatusTabCompleter());
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

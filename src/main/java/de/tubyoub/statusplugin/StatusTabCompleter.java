package de.tubyoub.statusplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

public class StatusTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("help");
            suggestions.add("remove");
            suggestions.add("setmaxlength");
            suggestions.add("resetmaxlength");
            suggestions.add("info");
            suggestions.add("reload");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
            } else if (args[0].equalsIgnoreCase("setmaxlength")) {
                suggestions.add("10");
                suggestions.add("20");
                suggestions.add("30");
            } else if (args[0].equalsIgnoreCase("help")) {
                suggestions.add("colorcodes");
            }
        }
        return suggestions;
    }
}

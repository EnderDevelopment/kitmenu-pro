package com.enderdevelopment.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class KitCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;

    public KitCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command is only available to players.");
            return true;
        }
        if (args.length == 0) {
            plugin.openKitMenu(player);
            return true;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        if (action.equals("create")) {
            if (!player.hasPermission("kit.admin")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to create kits.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(ChatColor.YELLOW + "Usage: /kit create <name>");
                return true;
            }
            String name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)).trim();
            if (!plugin.createKit(name, player)) {
                player.sendMessage(ChatColor.RED + "That kit name is invalid or already exists.");
            } else {
                player.sendMessage(ChatColor.GREEN + "Kit " + name + " created from your inventory.");
            }
            return true;
        }
        if (action.equals("delete")) {
            if (!player.hasPermission("kit.admin")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to delete kits.");
                return true;
            }
            if (args.length != 2) {
                player.sendMessage(ChatColor.YELLOW + "Usage: /kit delete <name>");
                return true;
            }
            player.sendMessage(plugin.deleteKit(args[1]) ? ChatColor.GREEN + "Kit deleted." : ChatColor.RED + "Kit not found.");
            return true;
        }
        if (action.equals("layout") && args.length == 2 && args[1].equalsIgnoreCase("save")) {
            if (plugin.saveLayout(player)) {
                player.sendMessage(ChatColor.GREEN + "Your layout has been saved for your most recently selected kit.");
            } else {
                player.sendMessage(ChatColor.RED + "Select a kit before saving its layout.");
            }
            return true;
        }
        player.sendMessage(ChatColor.YELLOW + "Usage: /kit, /kit create <name>, /kit delete <name>, /kit layout save");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("create");
            suggestions.add("delete");
            suggestions.add("layout");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("layout")) {
            suggestions.add("save");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            suggestions.addAll(plugin.getKitNames());
        }
        return suggestions;
    }
}

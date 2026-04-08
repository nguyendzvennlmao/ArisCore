package me.aris.core.shards.commands;

import me.aris.core.ArisCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class ShardsCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public ShardsCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        if (action.equals("balance") || action.equals("bal")) {
            if (args.length > 1) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    plugin.getShardsMessageManager().sendMessage(null, "player-not-found");
                    return true;
                }
                plugin.getShardsManager().getBalance(target, sender);
            } else if (sender instanceof Player) {
                plugin.getShardsManager().getBalance((Player) sender);
            } else {
                sender.sendMessage("Usage: /shards balance <player>");
            }
            return true;
        }
        
        if (!sender.hasPermission("ariscore.shards.admin")) {
            plugin.getShardsMessageManager().sendMessage(null, "no-permission");
            return true;
        }
        
        if (args.length < 3) {
            sendHelp(sender);
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getShardsMessageManager().sendMessage(null, "player-not-found");
            return true;
        }
        
        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            plugin.getShardsMessageManager().sendMessage(null, "invalid-amount");
            return true;
        }
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", formatNumber(amount));
        placeholders.put("player", target.getName());
        
        switch (action) {
            case "give":
                plugin.getShardsManager().addShards(target, amount);
                plugin.getShardsMessageManager().sendMessage((Player) sender, "added", placeholders);
                break;
            case "take":
                if (plugin.getShardsManager().removeShards(target, amount)) {
                    plugin.getShardsMessageManager().sendMessage((Player) sender, "taken", placeholders);
                } else {
                    sender.sendMessage("§cPlayer doesn't have enough shards!");
                }
                break;
            case "set":
                plugin.getShardsManager().setShards(target, amount);
                plugin.getShardsMessageManager().sendMessage((Player) sender, "set", placeholders);
                break;
            case "reset":
                plugin.getShardsManager().setShards(target, plugin.getShardsManager().getStartBalance());
                plugin.getShardsMessageManager().sendMessage((Player) sender, "reset", "player", target.getName());
                break;
            default:
                sendHelp(sender);
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Shards Commands ===");
        sender.sendMessage("§e/shards balance §7- Check your balance");
        sender.sendMessage("§e/shards balance <player> §7- Check player balance");
        sender.sendMessage("§e/shards give <player> <amount> §7- Give shards");
        sender.sendMessage("§e/shards take <player> <amount> §7- Take shards");
        sender.sendMessage("§e/shards set <player> <amount> §7- Set shards");
        sender.sendMessage("§e/shards reset <player> §7- Reset shards");
    }
    
    private String formatNumber(long number) {
        if (number >= 1000000000) {
            return String.format("%.1f", number / 1000000000.0) + "B";
        } else if (number >= 1000000) {
            return String.format("%.1f", number / 1000000.0) + "M";
        } else if (number >= 1000) {
            return String.format("%.1f", number / 1000.0) + "K";
        }
        return String.valueOf(number);
    }
  }

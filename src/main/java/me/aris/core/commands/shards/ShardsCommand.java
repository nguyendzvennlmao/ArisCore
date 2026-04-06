package me.aris.core.commands.shards;

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
            if (sender instanceof Player) {
                plugin.getShardsManager().getBalance((Player) sender);
            } else {
                sender.sendMessage("Usage: /shards <give|take|set|reset|balance> [player] [amount]");
            }
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        if (action.equals("balance")) {
            if (args.length > 1) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    plugin.getMessageManager().sendMessage(null, "player-not-found", "shards");
                    return true;
                }
                plugin.getShardsManager().getBalance(target, sender);
            } else if (sender instanceof Player) {
                plugin.getShardsManager().getBalance((Player) sender);
            }
            return true;
        }
        
        if (!sender.hasPermission("ariscore.shards.admin")) {
            plugin.getMessageManager().sendMessage(null, "no-permission", "shards");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage("Usage: /shards " + action + " <player> <amount>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageManager().sendMessage(null, "player-not-found", "shards");
            return true;
        }
        
        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage(null, "invalid-amount", "shards");
            return true;
        }
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", String.valueOf(amount));
        placeholders.put("player", target.getName());
        
        switch (action) {
            case "give":
                plugin.getShardsManager().addShards(target, amount);
                plugin.getMessageManager().sendMessage(sender, "added", "shards", placeholders);
                break;
            case "take":
                if (plugin.getShardsManager().removeShards(target, amount)) {
                    plugin.getMessageManager().sendMessage(sender, "taken", "shards", placeholders);
                } else {
                    sender.sendMessage("Player doesn't have enough shards!");
                }
                break;
            case "set":
                plugin.getShardsManager().setShards(target, amount);
                plugin.getMessageManager().sendMessage(sender, "set", "shards", placeholders);
                break;
            case "reset":
                plugin.getShardsManager().setShards(target, plugin.getShardsManager().getStartBalance());
                plugin.getMessageManager().sendMessage(sender, "reset", "shards", "player", target.getName());
                break;
            default:
                sender.sendMessage("Usage: /shards <give|take|set|reset|balance> [player] [amount]");
        }
        
        return true;
    }
                  }

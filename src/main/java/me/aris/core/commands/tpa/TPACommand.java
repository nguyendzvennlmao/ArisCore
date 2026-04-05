package me.aris.core.commands.tpa;

import me.aris.core.ArisCore;
import me.aris.core.models.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.HashMap;
import java.util.Map;

public class TPACommand implements CommandExecutor {
    private ArisCore plugin;
    
    public TPACommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "tpa");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpa")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "tpa");
            return true;
        }
        
        if (args.length < 1) {
            plugin.getMessageManager().sendMessage(player, "tpa-usage", "tpa");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().sendMessage(player, "player-not-found", "tpa", "player", args[0]);
            return true;
        }
        
        if (player.equals(target)) {
            plugin.getMessageManager().sendMessage(player, "self-teleport", "tpa");
            return true;
        }
        
        if (!plugin.getTPAManager().canSendRequest(player, target)) {
            return true;
        }
        
        TeleportRequest request = new TeleportRequest(player, target, false);
        plugin.getTPAManager().addRequest(request);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        plugin.getMessageManager().sendMessage(player, "sent-request", "tpa", placeholders);
        
        plugin.getMessageManager().sendMessage(target, "receive-request", "tpa", "player", player.getName());
        
        if (plugin.getTPAManager().isTPAutoEnabled(target)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                target.performCommand("tpaccept " + player.getName());
            }, 5L);
        }
        
        return true;
    }
    }

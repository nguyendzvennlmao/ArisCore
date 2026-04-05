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

public class TPAHereCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public TPAHereCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "tpa");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpahere")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "tpa");
            return true;
        }
        
        if (args.length < 1) {
            plugin.getMessageManager().sendMessage(player, "tpahere-usage", "tpa");
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
        
        if (!plugin.getTPAManager().isTPAHereEnabled(target)) {
            plugin.getMessageManager().sendMessage(player, "block-tphere-request", "tpa");
            return true;
        }
        
        TeleportRequest request = new TeleportRequest(player, target, true);
        plugin.getTPAManager().addRequest(request);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        plugin.getMessageManager().sendMessage(player, "sent-here-request", "tpa", placeholders);
        
        plugin.getMessageManager().sendMessage(target, "receive-here-request", "tpa", "player", player.getName());
        
        return true;
    }
    }

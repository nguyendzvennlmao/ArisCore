package me.aris.core.tpa.commands;

import me.aris.core.ArisCore;
import me.aris.core.tpa.model.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TPAcceptCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public TPAcceptCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getTPAMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpaccept")) {
            plugin.getTPAMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        TeleportRequest request = null;
        
        if (args.length > 0) {
            Player senderPlayer = Bukkit.getPlayer(args[0]);
            if (senderPlayer != null) {
                request = plugin.getTPAManager().getRequest(player, senderPlayer);
            }
        } else {
            List<TeleportRequest> requests = plugin.getTPAManager().getRequestsForTarget(player);
            if (!requests.isEmpty()) {
                request = requests.get(0);
            }
        }
        
        if (request == null) {
            plugin.getTPAMessageManager().sendMessage(player, "no-requests-found");
            return true;
        }
        
        plugin.getTPAManager().removeRequest(request);
        
        plugin.getTPAMessageManager().sendMessage(player, "accepted-teleport");
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        plugin.getTPAMessageManager().sendMessage(request.getSender(), "request-accepted", placeholders);
        
        plugin.getTPASoundManager().playAccept(player);
        plugin.getTPASoundManager().playAccept(request.getSender());
        
        if (request.isHere()) {
            plugin.getTPAMessageManager().sendMessage(request.getSender(), "teleport-to-you", placeholders);
            plugin.getTeleportManager().startTeleport(request.getSender(), player.getLocation(), "tpa");
        } else {
            plugin.getTeleportManager().startTeleport(player, request.getSender().getLocation(), "tpa");
        }
        
        return true;
    }
                  }

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

public class TPDenyCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public TPDenyCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getTPAMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpdeny")) {
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
            plugin.getTPAMessageManager().sendMessage(player, "non-valid-request");
            return true;
        }
        
        plugin.getTPAManager().removeRequest(request);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", request.getSender().getName());
        plugin.getTPAMessageManager().sendMessage(player, "cancelled-request", placeholders);
        
        placeholders.put("player", player.getName());
        plugin.getTPAMessageManager().sendMessage(request.getSender(), "cancelled-request-sender", placeholders);
        
        plugin.getTPASoundManager().playDeny(player);
        plugin.getTPASoundManager().playDeny(request.getSender());
        
        return true;
    }
                                                  }

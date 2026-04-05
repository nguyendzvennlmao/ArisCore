package me.aris.core.commands.tpa;

import me.aris.core.ArisCore;
import me.aris.core.models.TeleportRequest;
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
            plugin.getMessageManager().sendMessage(null, "player-only", "tpa");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpaccept")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "tpa");
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
            plugin.getMessageManager().sendMessage(player, "no-requests-found", "tpa");
            return true;
        }
        
        plugin.getTPAManager().removeRequest(request);
        
        plugin.getMessageManager().sendMessage(player, "accepted-teleport", "tpa");
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        plugin.getMessageManager().sendMessage(request.getSender(), "request-accepted", "tpa", placeholders);
        
        if (request.isHere()) {
            plugin.getMessageManager().sendMessage(request.getSender(), "teleport-to-you", "tpa", placeholders);
            plugin.getTeleportManager().startTeleport(request.getSender(), player.getLocation(), "tpa",
                () -> {},
                () -> {}
            );
        } else {
            plugin.getTeleportManager().startTeleport(player, request.getSender().getLocation(), "tpa",
                () -> {},
                () -> {}
            );
        }
        
        return true;
    }
            }

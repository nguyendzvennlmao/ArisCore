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

public class TPDenyCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public TPDenyCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "tpa");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpdeny")) {
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
            plugin.getMessageManager().sendMessage(player, "non-valid-request", "tpa");
            return true;
        }
        
        plugin.getTPAManager().removeRequest(request);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", request.getSender().getName());
        plugin.getMessageManager().sendMessage(player, "cancelled-request", "tpa", placeholders);
        
        placeholders.put("player", player.getName());
        plugin.getMessageManager().sendMessage(request.getSender(), "cancelled-request-sender", "tpa", placeholders);
        
        return true;
    }
                    }

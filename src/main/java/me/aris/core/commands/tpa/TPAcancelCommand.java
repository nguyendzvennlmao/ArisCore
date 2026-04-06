package me.aris.core.commands.tpa;

import me.aris.core.ArisCore;
import me.aris.core.models.TeleportRequest;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

public class TPAcancelCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public TPAcancelCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "tpa");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpacancel")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "tpa");
            return true;
        }
        
        List<TeleportRequest> toRemove = new ArrayList<>();
        for (TeleportRequest request : plugin.getTPAManager().getRequestsForTarget(player)) {
            if (request.getSender().equals(player)) {
                toRemove.add(request);
            }
        }
        
        if (toRemove.isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "cancel-requests-failed", "tpa");
            return true;
        }
        
        for (TeleportRequest request : toRemove) {
            plugin.getTPAManager().removeRequest(request);
        }
        
        plugin.getMessageManager().sendMessage(player, "cancel-requests", "tpa");
        
        return true;
    }
                             }

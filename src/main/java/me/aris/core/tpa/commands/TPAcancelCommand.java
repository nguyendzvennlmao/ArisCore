package me.aris.core.tpa.commands;

import me.aris.core.ArisCore;
import me.aris.core.tpa.model.TeleportRequest;
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
            plugin.getTPAMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpacancel")) {
            plugin.getTPAMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        List<TeleportRequest> toRemove = new ArrayList<>();
        for (TeleportRequest request : plugin.getTPAManager().getRequestsForTarget(player)) {
            if (request.getSender().equals(player)) {
                toRemove.add(request);
            }
        }
        
        if (toRemove.isEmpty()) {
            plugin.getTPAMessageManager().sendMessage(player, "cancel-requests-failed");
            return true;
        }
        
        for (TeleportRequest request : toRemove) {
            plugin.getTPAManager().removeRequest(request);
        }
        
        plugin.getTPAMessageManager().sendMessage(player, "cancel-requests");
        plugin.getTPASoundManager().playCancel(player);
        
        return true;
    }
}

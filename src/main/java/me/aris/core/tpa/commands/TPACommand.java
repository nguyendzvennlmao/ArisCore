package me.aris.core.tpa.commands;

import me.aris.core.ArisCore;
import me.aris.core.tpa.model.TeleportRequest;
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
            plugin.getTPAMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpa")) {
            plugin.getTPAMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        if (args.length < 1) {
            plugin.getTPAMessageManager().sendMessage(player, "tpa-usage");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getTPAMessageManager().sendMessage(player, "player-not-found", "player", args[0]);
            return true;
        }
        
        if (player.equals(target)) {
            plugin.getTPAMessageManager().sendMessage(player, "self-teleport");
            return true;
        }
        
        if (!plugin.getTPAManager().canSendRequest(player, target)) {
            return true;
        }
        
        if (plugin.getTPAConfigManager().isGUIEnabled() && plugin.getTPAManager().isGUIEnabled(player)) {
            plugin.getTPAGUI().openRequestGUI(player, target, false);
        } else {
            TeleportRequest request = new TeleportRequest(player, target, false);
            plugin.getTPAManager().addRequest(request);
            
            plugin.getTPAMessageManager().sendMessage(player, "sent-request", "player", target.getName());
            plugin.getTPAMessageManager().sendMessage(target, "receive-request", "player", player.getName());
            plugin.getTPASoundManager().playRequestSent(player);
            plugin.getTPASoundManager().playRequestReceived(target);
            
            if (plugin.getTPAManager().isTPAutoEnabled(target)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    target.performCommand("tpaccept " + player.getName());
                }, 5L);
            }
        }
        
        return true;
    }
}

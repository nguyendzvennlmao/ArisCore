package me.aris.core.commands.warp;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelWarpCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public DelWarpCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "warp");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.delwarp")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "warp");
            return true;
        }
        
        if (args.length < 1) {
            plugin.getMessageManager().sendMessage(player, "warp-invalid-name", "warp");
            return true;
        }
        
        String warpName = args[0];
        
        if (!plugin.getWarpManager().warpExists(warpName)) {
            plugin.getMessageManager().sendMessage(player, "warp-not-found", "warp", "warp", warpName);
            return true;
        }
        
        plugin.getWarpManager().removeWarp(warpName);
        plugin.getMessageManager().sendMessage(player, "warp-deleted", "warp", "warp", warpName);
        
        return true;
    }
              }

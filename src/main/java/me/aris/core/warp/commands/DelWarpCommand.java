package me.aris.core.warp.commands;

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
            plugin.getWarpMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.delwarp")) {
            plugin.getWarpMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        if (args.length < 1) {
            plugin.getWarpMessageManager().sendMessage(player, "delwarp-usage");
            return true;
        }
        
        String warpName = args[0];
        
        if (!plugin.getWarpManager().warpExists(warpName)) {
            plugin.getWarpMessageManager().sendMessage(player, "warp-not-found", "warp", warpName);
            return true;
        }
        
        plugin.getWarpManager().removeWarp(warpName);
        plugin.getWarpMessageManager().sendMessage(player, "warp-deleted", "warp", warpName);
        plugin.getWarpSoundManager().playWarpDelete(player);
        
        return true;
    }
              }

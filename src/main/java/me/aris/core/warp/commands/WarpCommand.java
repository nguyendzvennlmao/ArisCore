package me.aris.core.warp.commands;

import me.aris.core.ArisCore;
import me.aris.core.warp.model.Warp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Map;

public class WarpCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public WarpCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getWarpMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.warp")) {
            plugin.getWarpMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        if (args.length < 1) {
            Map<String, Warp> warps = plugin.getWarpManager().getWarps();
            if (warps.isEmpty()) {
                plugin.getWarpMessageManager().sendMessage(player, "no-warps");
            } else {
                String warpList = String.join(", ", warps.keySet());
                plugin.getWarpMessageManager().sendMessage(player, "warp-list", "warps", warpList);
            }
            return true;
        }
        
        String warpName = args[0];
        
        if (!plugin.getWarpManager().warpExists(warpName)) {
            plugin.getWarpMessageManager().sendMessage(player, "warp-not-found", "warp", warpName);
            return true;
        }
        
        Warp warp = plugin.getWarpManager().getWarp(warpName);
        
        plugin.getTeleportManager().startTeleport(player, warp.getLocation(), "warp");
        
        return true;
    }
          }

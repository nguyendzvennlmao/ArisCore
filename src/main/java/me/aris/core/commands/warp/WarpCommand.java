package me.aris.core.commands.warp;

import me.aris.core.ArisCore;
import me.aris.core.models.Warp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public WarpCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "warp");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.warp")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "warp");
            return true;
        }
        
        if (args.length < 1) {
            if (plugin.getWarpManager().getWarps().isEmpty()) {
                plugin.getMessageManager().sendMessage(player, "no-warps", "warp");
            } else {
                String warps = String.join(", ", plugin.getWarpManager().getWarps().keySet());
                plugin.getMessageManager().sendMessage(player, "warp-list", "warp", "warps", warps);
            }
            return true;
        }
        
        String warpName = args[0];
        
        if (!plugin.getWarpManager().warpExists(warpName)) {
            plugin.getMessageManager().sendMessage(player, "warp-not-found", "warp", "warp", warpName);
            return true;
        }
        
        Warp warp = plugin.getWarpManager().getWarp(warpName);
        
        plugin.getTeleportManager().startTeleport(player, warp.getLocation(), "warp",
            () -> {
                plugin.getMessageManager().sendTeleportSuccess(player, "warp");
            },
            () -> {}
        );
        
        return true;
    }
            }

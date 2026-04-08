package me.aris.core.warp.commands;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWarpCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public SetWarpCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getWarpMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.setwarp")) {
            plugin.getWarpMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        if (args.length < 1) {
            plugin.getWarpMessageManager().sendMessage(player, "setwarp-usage");
            return true;
        }
        
        String warpName = args[0];
        
        if (!warpName.matches("^[a-zA-Z0-9_]+$")) {
            plugin.getWarpMessageManager().sendMessage(player, "warp-invalid-name");
            return true;
        }
        
        if (plugin.getWarpManager().warpExists(warpName)) {
            plugin.getWarpMessageManager().sendMessage(player, "warp-exists", "warp", warpName);
            return true;
        }
        
        plugin.getWarpManager().addWarp(player, warpName, player.getLocation());
        plugin.getWarpMessageManager().sendMessage(player, "warp-set", "warp", warpName);
        plugin.getWarpSoundManager().playWarpSet(player);
        
        return true;
    }
          }

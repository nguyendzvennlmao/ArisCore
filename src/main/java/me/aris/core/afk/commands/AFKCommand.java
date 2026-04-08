package me.aris.core.afk.commands;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AFKCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public AFKCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getAFKMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.afk")) {
            plugin.getAFKMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        if (plugin.getAFKManager().isAFK(player)) {
            plugin.getAFKManager().setAFK(player, false);
        } else {
            plugin.getAFKManager().setAFK(player, true);
        }
        
        return true;
    }
              }

package me.aris.core.commands.home;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public DelHomeCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "home");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.delhome")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "home");
            return true;
        }
        
        String homeName = args.length > 0 ? args[0] : "home";
        
        if (!plugin.getHomeManager().hasHome(player, homeName)) {
            plugin.getMessageManager().sendMessage(player, "home-not-found", "home", "home", homeName);
            return true;
        }
        
        plugin.getHomeManager().removeHome(player, homeName);
        plugin.getMessageManager().sendMessage(player, "home-deleted", "home", "home", homeName);
        
        return true;
    }
              }

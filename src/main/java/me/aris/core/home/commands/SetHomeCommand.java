package me.aris.core.home.commands;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public SetHomeCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getHomeMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.sethome")) {
            plugin.getHomeMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        String homeName = args.length > 0 ? args[0] : "home";
        
        if (!homeName.matches("^[a-zA-Z0-9_]+$")) {
            plugin.getHomeMessageManager().sendMessage(player, "home-invalid-name");
            return true;
        }
        
        if (plugin.getHomeManager().hasHome(player, homeName)) {
            plugin.getHomeMessageManager().sendMessage(player, "home-exists", "home", homeName);
            return true;
        }
        
        int currentHomes = plugin.getHomeManager().getHomes(player).size();
        int maxHomes = plugin.getHomeManager().getMaxHomes(player);
        
        if (currentHomes >= maxHomes) {
            plugin.getHomeMessageManager().sendMessage(player, "home-limit-reached", 
                Map.of("current", String.valueOf(currentHomes), "max", String.valueOf(maxHomes)));
            return true;
        }
        
        plugin.getHomeManager().addHome(player, homeName, player.getLocation());
        plugin.getHomeMessageManager().sendMessage(player, "home-set", "home", homeName);
        plugin.getHomeSoundManager().playHomeSet(player);
        
        return true;
    }
    }

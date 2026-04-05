package me.aris.core.commands.home;

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
            plugin.getMessageManager().sendMessage(null, "player-only", "home");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.sethome")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "home");
            return true;
        }
        
        String homeName = args.length > 0 ? args[0] : "home";
        
        if (!homeName.matches("^[a-zA-Z0-9_]+$")) {
            plugin.getMessageManager().sendMessage(player, "home-invalid-name", "home");
            return true;
        }
        
        if (plugin.getHomeManager().hasHome(player, homeName)) {
            plugin.getMessageManager().sendMessage(player, "home-exists", "home", "home", homeName);
            return true;
        }
        
        int currentHomes = plugin.getHomeManager().getHomes(player).size();
        int maxHomes = plugin.getHomeManager().getMaxHomes(player);
        
        if (currentHomes >= maxHomes) {
            plugin.getMessageManager().sendMessage(player, "home-limit-reached", "home", 
                new java.util.HashMap<String, String>() {{
                    put("current", String.valueOf(currentHomes));
                    put("max", String.valueOf(maxHomes));
                }});
            return true;
        }
        
        plugin.getHomeManager().addHome(player, homeName, player.getLocation());
        plugin.getMessageManager().sendMessage(player, "home-set", "home", "home", homeName);
        
        return true;
    }
                                                   }

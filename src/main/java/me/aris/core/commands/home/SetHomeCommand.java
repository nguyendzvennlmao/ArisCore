package me.aris.core.commands.home;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

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
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("home", homeName);
            plugin.getMessageManager().sendMessage(player, "home-exists", "home", placeholders);
            return true;
        }
        
        int currentHomes = plugin.getHomeManager().getHomes(player).size();
        int maxHomes = plugin.getHomeManager().getMaxHomes(player);
        
        if (currentHomes >= maxHomes) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("current", String.valueOf(currentHomes));
            placeholders.put("max", String.valueOf(maxHomes));
            plugin.getMessageManager().sendMessage(player, "home-limit-reached", "home", placeholders);
            return true;
        }
        
        plugin.getHomeManager().addHome(player, homeName, player.getLocation());
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("home", homeName);
        plugin.getMessageManager().sendMessage(player, "home-set", "home", placeholders);
        
        return true;
    }
                             }

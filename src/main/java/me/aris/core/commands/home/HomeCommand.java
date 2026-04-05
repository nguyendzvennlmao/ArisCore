package me.aris.core.commands.home;

import me.aris.core.ArisCore;
import me.aris.core.models.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class HomeCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public HomeCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "home");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.home")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "home");
            return true;
        }
        
        if (args.length == 0 && plugin.getConfigManager().getHomeConfig().getBoolean("gui.enabled", true)) {
            plugin.getHomeGUI().openHomeGUI(player);
            return true;
        }
        
        String homeName = args.length > 0 ? args[0] : "home";
        
        if (!plugin.getHomeManager().hasHome(player, homeName)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("home", homeName);
            plugin.getMessageManager().sendMessage(player, "home-not-found", "home", placeholders);
            return true;
        }
        
        Home home = plugin.getHomeManager().getHome(player, homeName);
        if (home == null || home.getLocation() == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("home", homeName);
            plugin.getMessageManager().sendMessage(player, "home-not-found", "home", placeholders);
            return true;
        }
        
        plugin.getTeleportManager().startTeleport(player, home.getLocation(), "home",
            () -> {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("home", homeName);
                plugin.getMessageManager().sendMessage(player, "teleport-success", "home", placeholders);
            },
            () -> {
                plugin.getMessageManager().sendMessage(player, "teleport-cancelled-movement", "home");
            }
        );
        
        return true;
    }
    }

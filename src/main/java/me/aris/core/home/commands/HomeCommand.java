package me.aris.core.home.commands;

import me.aris.core.ArisCore;
import me.aris.core.home.model.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public HomeCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getHomeMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.home")) {
            plugin.getHomeMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        if (plugin.getConfigManager().getModuleConfig("home").getBoolean("gui.enabled", true) && args.length == 0) {
            plugin.getHomeGUI().openHomeGUI(player);
            return true;
        }
        
        String homeName = args.length > 0 ? args[0] : "home";
        
        if (!plugin.getHomeManager().hasHome(player, homeName)) {
            plugin.getHomeMessageManager().sendMessage(player, "home-not-found", "home", homeName);
            return true;
        }
        
        Home home = plugin.getHomeManager().getHome(player, homeName);
        if (home == null) {
            plugin.getHomeMessageManager().sendMessage(player, "home-not-found", "home", homeName);
            return true;
        }
        
        plugin.getTeleportManager().startTeleport(player, home.getLocation(), "home");
        
        return true;
    }
}

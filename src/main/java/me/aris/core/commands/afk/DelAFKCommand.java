package me.aris.core.commands.afk;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelAFKCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public DelAFKCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "afk");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.delafk")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "afk");
            return true;
        }
        
        if (plugin.getAFKManager().getAFKLocation() == null) {
            plugin.getMessageManager().sendMessage(player, "afk-location-not-set", "afk");
            return true;
        }
        
        plugin.getAFKManager().deleteAFKLocation();
        plugin.getMessageManager().sendMessage(player, "afk-location-deleted", "afk");
        
        return true;
    }
}

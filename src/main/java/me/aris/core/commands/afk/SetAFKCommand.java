package me.aris.core.commands.afk;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetAFKCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public SetAFKCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "afk");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.setafk")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "afk");
            return true;
        }
        
        plugin.getAFKManager().setAFKLocation(player.getLocation());
        plugin.getMessageManager().sendMessage(player, "afk-location-set", "afk");
        
        return true;
    }
}

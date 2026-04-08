package me.aris.core.afk.commands;

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
            plugin.getAFKMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.setafk")) {
            plugin.getAFKMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        plugin.getAFKManager().setAFKLocation(player.getLocation());
        plugin.getAFKMessageManager().sendMessage(player, "afk-location-set");
        plugin.getAFKSoundManager().playAFKLocationSet(player);
        
        return true;
    }
}

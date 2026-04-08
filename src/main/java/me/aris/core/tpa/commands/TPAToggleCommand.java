package me.aris.core.tpa.commands;

import me.aris.core.ArisCore;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TPAToggleCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public TPAToggleCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getTPAMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpatoggle")) {
            plugin.getTPAMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        boolean current = plugin.getTPAManager().isTPAEnabled(player);
        plugin.getTPAManager().setTPAEnabled(player, !current);
        
        if (!current) {
            plugin.getTPAMessageManager().sendMessage(player, "tpa-toggle-on");
        } else {
            plugin.getTPAMessageManager().sendMessage(player, "tpa-toggle-off");
        }
        
        return true;
    }
}

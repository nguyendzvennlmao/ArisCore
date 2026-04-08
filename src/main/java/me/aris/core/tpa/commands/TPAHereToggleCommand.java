package me.aris.core.tpa.commands;

import me.aris.core.ArisCore;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TPAHereToggleCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public TPAHereToggleCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getTPAMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpaheretoggle")) {
            plugin.getTPAMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        boolean current = plugin.getTPAManager().isTPAHereEnabled(player);
        plugin.getTPAManager().setTPAHereEnabled(player, !current);
        
        if (!current) {
            plugin.getTPAMessageManager().sendMessage(player, "tpa-here-toggle-on");
        } else {
            plugin.getTPAMessageManager().sendMessage(player, "tpa-here-toggle-off");
        }
        
        return true;
    }
}

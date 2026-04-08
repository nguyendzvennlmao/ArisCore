package me.aris.core.tpa.commands;

import me.aris.core.ArisCore;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TPAcancelCommand implements CommandExecutor {
    
    private final ArisCore plugin;
    
    public TPAcancelCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getTPAMessageManager().sendMessage(sender, "player_only");
            return true;
        }
        
        Player player = (Player) sender;
        
        plugin.getTPAManager().cancelRequest(player);
        return true;
    }
}

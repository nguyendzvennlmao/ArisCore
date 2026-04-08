package me.aris.core.sell.commands;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public SellCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getSellMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.sell")) {
            plugin.getSellMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        plugin.getSellGUI().openSellGUI(player);
        
        return true;
    }
}

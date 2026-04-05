package me.aris.core.commands.tpa;

import me.aris.core.ArisCore;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TPAutoCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public TPAutoCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "tpa");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpauto")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "tpa");
            return true;
        }
        
        boolean current = plugin.getTPAManager().isTPAutoEnabled(player);
        plugin.getTPAManager().setTPAutoEnabled(player, !current);
        
        return true;
    }
    }

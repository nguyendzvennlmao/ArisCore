package me.aris.core.commands.tpa;

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
            plugin.getMessageManager().sendMessage(null, "player-only", "tpa");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.tpaheretoggle")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "tpa");
            return true;
        }
        
        boolean current = plugin.getTPAManager().isTPAHereEnabled(player);
        plugin.getTPAManager().setTPAHereEnabled(player, !current);
        
        if (!current) {
            plugin.getMessageManager().sendMessage(player, "tpa-here-toggle-on", "tpa");
        } else {
            plugin.getMessageManager().sendMessage(player, "tpa-here-toggle-off", "tpa");
        }
        
        return true;
    }
}

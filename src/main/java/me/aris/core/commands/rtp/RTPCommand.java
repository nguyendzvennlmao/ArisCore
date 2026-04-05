package me.aris.core.commands.rtp;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RTPCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public RTPCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "rtp");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.rtp")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "rtp");
            return true;
        }
        
        plugin.getRTPGUI().openRTPGUI(player);
        
        return true;
    }
}

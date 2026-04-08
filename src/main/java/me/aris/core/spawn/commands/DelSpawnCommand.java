package me.aris.core.spawn.commands;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelSpawnCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public DelSpawnCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getSpawnMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.delspawn")) {
            plugin.getSpawnMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        if (!plugin.getSpawnManager().hasSpawn()) {
            plugin.getSpawnMessageManager().sendMessage(player, "spawn-not-set");
            return true;
        }
        
        plugin.getSpawnManager().deleteSpawn();
        plugin.getSpawnMessageManager().sendMessage(player, "spawn-deleted");
        plugin.getSpawnSoundManager().playSpawnDelete(player);
        
        return true;
    }
}

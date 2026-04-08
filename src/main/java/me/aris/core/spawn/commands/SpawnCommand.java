package me.aris.core.spawn.commands;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public SpawnCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getSpawnMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.spawn")) {
            plugin.getSpawnMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        if (!plugin.getSpawnManager().hasSpawn()) {
            plugin.getSpawnMessageManager().sendMessage(player, "spawn-not-set");
            return true;
        }
        
        plugin.getTeleportManager().startTeleport(player, plugin.getSpawnManager().getSpawn(), "spawn");
        
        return true;
    }
}

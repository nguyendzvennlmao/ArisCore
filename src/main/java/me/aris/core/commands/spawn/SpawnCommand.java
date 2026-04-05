package me.aris.core.commands.spawn;

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
            plugin.getMessageManager().sendMessage(null, "player-only", "spawn");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.spawn")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "spawn");
            return true;
        }
        
        if (!plugin.getSpawnManager().hasSpawn()) {
            plugin.getMessageManager().sendMessage(player, "spawn-not-set", "spawn");
            return true;
        }
        
        plugin.getTeleportManager().startTeleport(player, plugin.getSpawnManager().getSpawn(),
            () -> {
                plugin.getMessageManager().sendMessage(player, "teleport-success", "spawn");
            },
            () -> {
                plugin.getMessageManager().sendMessage(player, "teleport-cancelled-movement", "spawn");
            }
        );
        
        return true;
    }
}

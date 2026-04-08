package me.aris.core.spawn.commands;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public SetSpawnCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getSpawnMessageManager().sendMessage(null, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.setspawn")) {
            plugin.getSpawnMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        plugin.getSpawnManager().setSpawn(player.getLocation());
        plugin.getSpawnMessageManager().sendMessage(player, "spawn-set");
        plugin.getSpawnSoundManager().playSpawnSet(player);
        
        return true;
    }
}

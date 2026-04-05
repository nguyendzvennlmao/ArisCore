package me.aris.core.commands.spawn;

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
            plugin.getMessageManager().sendMessage(null, "player-only", "spawn");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.setspawn")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "spawn");
            return true;
        }
        
        plugin.getSpawnManager().setSpawn(player.getLocation());
        plugin.getMessageManager().sendMessage(player, "spawn-set", "spawn");
        
        return true;
    }
                                          }

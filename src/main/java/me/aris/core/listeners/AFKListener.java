package me.aris.core.listeners;

import me.aris.core.ArisCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class AFKListener implements Listener {
    private ArisCore plugin;
    
    public AFKListener(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().distance(event.getTo()) > 0.1) {
            plugin.getAFKManager().updateActivity(player);
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        plugin.getAFKManager().updateActivity(player);
        
        if (plugin.getAFKManager().isAFK(player)) {
            event.setCancelled(true);
            plugin.getMessageManager().sendMessage(player, "cant-do-while-afk", "afk");
        }
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        plugin.getAFKManager().updateActivity(player);
        
        if (plugin.getAFKManager().isAFK(player)) {
            String command = event.getMessage().toLowerCase();
            if (!command.equals("/afk")) {
                event.setCancelled(true);
                plugin.getMessageManager().sendMessage(player, "cant-do-while-afk", "afk");
            }
        }
    }
}

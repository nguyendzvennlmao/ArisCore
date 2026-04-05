package me.aris.core.listeners;

import me.aris.core.ArisCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener implements Listener {
    private ArisCore plugin;
    
    public TeleportListener(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.getTeleportManager().isTeleporting(player)) {
            double allowedRange = plugin.getConfigManager().getAllowedWalkRange();
            if (allowedRange > 0) {
                if (event.getFrom().distance(event.getTo()) > allowedRange) {
                    plugin.getTeleportManager().cancelTeleport(player);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getTeleportManager().isTeleporting(player)) {
            plugin.getTeleportManager().cancelTeleport(player);
        }
        plugin.getTPAManager().removeAllRequestsFrom(player);
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (plugin.getTeleportManager().isTeleporting(event.getPlayer())) {
            plugin.getTeleportManager().cancelTeleport(event.getPlayer());
        }
    }
            }

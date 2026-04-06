package me.aris.core.managers;

import me.aris.core.ArisCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    private ArisCore plugin;
    private Map<UUID, TeleportData> activeTeleports;
    
    public TeleportManager(ArisCore plugin) {
        this.plugin = plugin;
        this.activeTeleports = new HashMap<>();
    }
    
    public void startTeleport(Player player, Location targetLocation, String module, Runnable onComplete, Runnable onCancel) {
        cancelTeleport(player);
        
        TeleportData data = new TeleportData();
        data.player = player;
        data.targetLocation = targetLocation;
        data.module = module;
        data.onComplete = onComplete;
        data.onCancel = onCancel;
        data.startLocation = player.getLocation().clone();
        data.countdown = plugin.getConfigManager().getTeleportCountdown();
        data.cancelled = false;
        
        activeTeleports.put(player.getUniqueId(), data);
        
        data.task = player.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
            TeleportData currentData = activeTeleports.get(player.getUniqueId());
            if (currentData == null || currentData.cancelled || !player.isOnline()) {
                if (scheduledTask != null) scheduledTask.cancel();
                activeTeleports.remove(player.getUniqueId());
                return;
            }
            
            Location currentLoc = player.getLocation();
            double dx = Math.abs(currentData.startLocation.getX() - currentLoc.getX());
            double dz = Math.abs(currentData.startLocation.getZ() - currentLoc.getZ());
            
            if ((dx > 0.1 || dz > 0.1) && currentData.countdown > 0) {
                plugin.getMessageManager().sendTeleportCancelled(player, module, "movement");
                currentData.cancelled = true;
                if (currentData.onCancel != null) {
                    currentData.onCancel.run();
                }
                if (scheduledTask != null) scheduledTask.cancel();
                activeTeleports.remove(player.getUniqueId());
                return;
            }
            
            if (currentData.countdown <= 0) {
                Location finalLoc = currentData.targetLocation.clone();
                player.teleportAsync(finalLoc).thenAccept(success -> {
                    if (success) {
                        plugin.getMessageManager().sendTeleportSuccess(player, module);
                        if (currentData.onComplete != null) {
                            currentData.onComplete.run();
                        }
                    }
                });
                if (scheduledTask != null) scheduledTask.cancel();
                activeTeleports.remove(player.getUniqueId());
                return;
            }
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("time", String.valueOf(currentData.countdown));
            plugin.getMessageManager().sendTeleportCountdown(player, module, currentData.countdown, placeholders);
            
            currentData.countdown--;
            
        }, null, 1L, 20L);
    }
    
    public void cancelTeleport(Player player) {
        TeleportData data = activeTeleports.remove(player.getUniqueId());
        if (data != null && data.task != null) {
            data.task.cancel();
        }
    }
    
    public boolean isTeleporting(Player player) {
        return activeTeleports.containsKey(player.getUniqueId());
    }
    
    private static class TeleportData {
        Player player;
        Location targetLocation;
        String module;
        Runnable onComplete;
        Runnable onCancel;
        Location startLocation;
        int countdown;
        boolean cancelled;
        io.papermc.paper.threadedregions.scheduler.ScheduledTask task;
    }
            }

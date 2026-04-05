package me.aris.core.managers;

import me.aris.core.ArisCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    private ArisCore plugin;
    private Map<UUID, TeleportTask> activeTeleports;
    
    public TeleportManager(ArisCore plugin) {
        this.plugin = plugin;
        this.activeTeleports = new HashMap<>();
    }
    
    public void startTeleport(Player player, Location targetLocation, Runnable onComplete, Runnable onCancel) {
        if (activeTeleports.containsKey(player.getUniqueId())) {
            cancelTeleport(player);
        }
        
        TeleportTask task = new TeleportTask(plugin, player, targetLocation, onComplete, onCancel);
        activeTeleports.put(player.getUniqueId(), task);
        task.start();
    }
    
    public void cancelTeleport(Player player) {
        TeleportTask task = activeTeleports.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
    
    public boolean isTeleporting(Player player) {
        return activeTeleports.containsKey(player.getUniqueId());
    }
    
    private class TeleportTask {
        private ArisCore plugin;
        private Player player;
        private Location startLocation;
        private Location targetLocation;
        private Runnable onComplete;
        private Runnable onCancel;
        private int countdown;
        private int taskId;
        private boolean cancelled;
        
        public TeleportTask(ArisCore plugin, Player player, Location targetLocation, Runnable onComplete, Runnable onCancel) {
            this.plugin = plugin;
            this.player = player;
            this.startLocation = player.getLocation().clone();
            this.targetLocation = targetLocation;
            this.onComplete = onComplete;
            this.onCancel = onCancel;
            this.countdown = plugin.getConfigManager().getTeleportCountdown();
            this.cancelled = false;
        }
        
        public void start() {
            taskId = new BukkitRunnable() {
                @Override
                public void run() {
                    if (cancelled || !player.isOnline()) {
                        cancel();
                        return;
                    }
                    
                    if (checkCancellationConditions()) {
                        cancelTeleport();
                        return;
                    }
                    
                    if (countdown <= 0) {
                        executeTeleport();
                        cancel();
                        return;
                    }
                    
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("time", String.valueOf(countdown));
                    plugin.getMessageManager().sendTeleportCountdown(player, getModuleName(), countdown, placeholders);
                    countdown--;
                }
            }.runTaskTimer(plugin, 0L, 20L).getTaskId();
        }
        
        private boolean checkCancellationConditions() {
            double allowedRange = plugin.getConfigManager().getAllowedWalkRange();
            Location currentLocation = player.getLocation();
            
            if (allowedRange > 0) {
                double distance = startLocation.distance(currentLocation);
                if (distance > allowedRange) {
                    plugin.getMessageManager().sendTeleportCancelled(player, getModuleName(), "movement");
                    return true;
                }
            }
            
            return false;
        }
        
        private void executeTeleport() {
            if (cancelled || !player.isOnline()) return;
            
            player.teleportAsync(targetLocation).thenAccept(success -> {
                if (success) {
                    plugin.getMessageManager().sendTeleportSuccess(player, getModuleName());
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
        }
        
        private void cancelTeleport() {
            cancelled = true;
            if (onCancel != null) {
                onCancel.run();
            }
            cancel();
        }
        
        public void cancel() {
            cancelled = true;
            Bukkit.getScheduler().cancelTask(taskId);
        }
        
        private String getModuleName() {
            return "warp";
        }
    }
      }

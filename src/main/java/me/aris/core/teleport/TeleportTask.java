package me.aris.core.teleport;

import me.aris.core.ArisCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportTask {
    private ArisCore plugin;
    private Player player;
    private Location startLocation;
    private Location targetLocation;
    private TeleportManager.TeleportCallback callback;
    private int countdown;
    private io.papermc.paper.threadedregions.scheduler.ScheduledTask task;
    private boolean cancelled;
    private boolean hasSentCancel;
    
    public TeleportTask(ArisCore plugin, Player player, Location targetLocation, TeleportManager.TeleportCallback callback) {
        this.plugin = plugin;
        this.player = player;
        this.startLocation = player.getLocation().clone();
        this.targetLocation = targetLocation;
        this.callback = callback;
        this.countdown = plugin.getConfigManager().getTeleportCountdown();
        this.cancelled = false;
        this.hasSentCancel = false;
    }
    
    public void start() {
        task = player.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
            if (cancelled || !player.isOnline()) {
                scheduledTask.cancel();
                return;
            }
            
            Location currentLoc = player.getLocation();
            double dx = Math.abs(startLocation.getX() - currentLoc.getX());
            double dz = Math.abs(startLocation.getZ() - currentLoc.getZ());
            
            if ((dx > 0.05 || dz > 0.05) && countdown > 0 && !hasSentCancel) {
                hasSentCancel = true;
                if (callback != null) {
                    callback.onCancel();
                }
                cancel();
                scheduledTask.cancel();
                return;
            }
            
            if (countdown <= 0) {
                player.teleportAsync(targetLocation).thenAccept(success -> {
                    if (success && callback != null) {
                        callback.onSuccess();
                    }
                });
                cancel();
                scheduledTask.cancel();
                return;
            }
            
            if (callback != null) {
                callback.onCountdown(countdown);
            }
            
            countdown--;
            
        }, null, 1L, 20L);
    }
    
    public void cancel() {
        cancelled = true;
        if (task != null) {
            task.cancel();
        }
    }
  }

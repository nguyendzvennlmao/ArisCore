package me.aris.core.tpa.teleport;

import me.aris.core.ArisCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPATeleportManager {
    private ArisCore plugin;
    private Map<UUID, TeleportTask> activeTeleports;
    
    public TPATeleportManager(ArisCore plugin) {
        this.plugin = plugin;
        this.activeTeleports = new HashMap<>();
    }
    
    public void startTeleport(Player player, Location targetLocation) {
        cancelTeleport(player);
        
        TeleportTask task = new TeleportTask(player, targetLocation);
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
        private Player player;
        private Location startLocation;
        private Location targetLocation;
        private int countdown;
        private int taskId;
        private boolean cancelled;
        private boolean hasSentCancel;
        
        public TeleportTask(Player player, Location targetLocation) {
            this.player = player;
            this.startLocation = player.getLocation().clone();
            this.targetLocation = targetLocation;
            this.countdown = plugin.getTPAConfigManager().getTeleportCountdown();
            this.cancelled = false;
            this.hasSentCancel = false;
        }
        
        public void start() {
            taskId = new BukkitRunnable() {
                @Override
                public void run() {
                    if (cancelled || !player.isOnline()) {
                        cancel();
                        return;
                    }
                    
                    Location currentLoc = player.getLocation();
                    double dx = Math.abs(startLocation.getX() - currentLoc.getX());
                    double dz = Math.abs(startLocation.getZ() - currentLoc.getZ());
                    
                    if ((dx > 0.05 || dz > 0.05) && countdown > 0 && !hasSentCancel) {
                        hasSentCancel = true;
                        plugin.getTPAMessageManager().sendMessage(player, "teleport-cancelled-movement");
                        plugin.getTPASoundManager().playTeleportCancel(player);
                        cancelTeleport();
                        cancel();
                        return;
                    }
                    
                    if (countdown <= 0) {
                        executeTeleport();
                        cancel();
                        return;
                    }
                    
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("time", String.valueOf(countdown));
                    plugin.getTPAMessageManager().sendMessage(player, "teleport-countdown", placeholders);
                    plugin.getTPASoundManager().playCountdown(player);
                    countdown--;
                }
            }.runTaskTimer(plugin, 0L, 20L).getTaskId();
        }
        
        private void executeTeleport() {
            if (cancelled || !player.isOnline()) return;
            
            player.teleportAsync(targetLocation).thenAccept(success -> {
                if (success) {
                    plugin.getTPAMessageManager().sendMessage(player, "teleport-success");
                    plugin.getTPASoundManager().playTeleportSuccess(player);
                }
            });
        }
        
        private void cancelTeleport() {
            cancelled = true;
        }
        
        public void cancel() {
            cancelled = true;
            if (taskId != 0) {
                org.bukkit.Bukkit.getScheduler().cancelTask(taskId);
            }
        }
    }
            }

package me.aris.core.managers;

import me.aris.core.ArisCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    private ArisCore plugin;
    private Map<UUID, Integer> teleportTasks;
    private Map<UUID, Location> startLocations;
    private Map<UUID, Integer> countdowns;
    private Map<UUID, String> teleportModules;
    private Map<UUID, Runnable> teleportCompletes;
    private Map<UUID, Runnable> teleportCancels;
    
    public TeleportManager(ArisCore plugin) {
        this.plugin = plugin;
        this.teleportTasks = new HashMap<>();
        this.startLocations = new HashMap<>();
        this.countdowns = new HashMap<>();
        this.teleportModules = new HashMap<>();
        this.teleportCompletes = new HashMap<>();
        this.teleportCancels = new HashMap<>();
    }
    
    public void startTeleport(Player player, Location targetLocation, String module, Runnable onComplete, Runnable onCancel) {
        cancelTeleport(player);
        
        UUID uuid = player.getUniqueId();
        startLocations.put(uuid, player.getLocation().clone());
        countdowns.put(uuid, plugin.getConfigManager().getTeleportCountdown());
        teleportModules.put(uuid, module);
        teleportCompletes.put(uuid, onComplete);
        teleportCancels.put(uuid, onCancel);
        
        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelTeleport(player);
                    return;
                }
                
                int currentCountdown = countdowns.getOrDefault(uuid, 0);
                Location startLoc = startLocations.get(uuid);
                Location currentLoc = player.getLocation();
                
                double dx = Math.abs(startLoc.getX() - currentLoc.getX());
                double dz = Math.abs(startLoc.getZ() - currentLoc.getZ());
                
                if (dx > 0.1 || dz > 0.1) {
                    String cancelMessage = ChatColor.RED + "The transfer was cancelled because you have already moved.";
                    player.sendMessage(cancelMessage);
                    player.sendActionBar(ChatColor.RED + "Transfer cancelled - you moved");
                    cancelTeleport(player);
                    return;
                }
                
                if (currentCountdown <= 0) {
                    player.teleportAsync(targetLocation).thenAccept(success -> {
                        if (success) {
                            String successMessage = ChatColor.GREEN + "Teleported successfully!";
                            player.sendMessage(successMessage);
                            player.sendActionBar(ChatColor.GREEN + "Teleported!");
                            if (teleportCompletes.containsKey(uuid)) {
                                teleportCompletes.get(uuid).run();
                            }
                        }
                    });
                    cancelTeleport(player);
                    return;
                }
                
                String countdownMessage = ChatColor.YELLOW + "Teleporting in " + currentCountdown + " seconds...";
                player.sendMessage(countdownMessage);
                player.sendActionBar(ChatColor.YELLOW + "Teleporting in " + currentCountdown + "s");
                countdowns.put(uuid, currentCountdown - 1);
            }
        }, 0L, 20L).getTaskId();
        
        teleportTasks.put(uuid, taskId);
    }
    
    public void cancelTeleport(Player player) {
        UUID uuid = player.getUniqueId();
        Integer taskId = teleportTasks.remove(uuid);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        startLocations.remove(uuid);
        countdowns.remove(uuid);
        teleportModules.remove(uuid);
        
        if (teleportCancels.containsKey(uuid)) {
            teleportCancels.get(uuid).run();
            teleportCancels.remove(uuid);
        }
        teleportCompletes.remove(uuid);
    }
    
    public boolean isTeleporting(Player player) {
        return teleportTasks.containsKey(player.getUniqueId());
    }
                        }

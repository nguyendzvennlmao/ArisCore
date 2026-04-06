package me.aris.core.managers;

import me.aris.core.ArisCore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    private ArisCore plugin;
    private Map<UUID, io.papermc.paper.threadedregions.scheduler.ScheduledTask> activeTeleports;
    private Map<UUID, Location> startLocations;
    private Map<UUID, Integer> countdowns;
    private Map<UUID, String> teleportModules;
    private Map<UUID, Runnable> teleportCompletes;
    private Map<UUID, Runnable> teleportCancels;
    
    public TeleportManager(ArisCore plugin) {
        this.plugin = plugin;
        this.activeTeleports = new HashMap<>();
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
        
        io.papermc.paper.threadedregions.scheduler.ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
            if (!player.isOnline()) {
                cancelTeleport(player);
                scheduledTask.cancel();
                return;
            }
            
            int currentCountdown = countdowns.getOrDefault(uuid, 0);
            Location startLoc = startLocations.get(uuid);
            Location currentLoc = player.getLocation();
            
            double dx = Math.abs(startLoc.getX() - currentLoc.getX());
            double dz = Math.abs(startLoc.getZ() - currentLoc.getZ());
            
            if ((dx > 0.1 || dz > 0.1) && currentCountdown > 0) {
                String cancelMessage = getMessageFromFile(module, "chat-teleport-cancelled-movement");
                String cancelActionBar = getMessageFromFile(module, "actionbar-teleport-cancelled-movement");
                
                if (!cancelMessage.isEmpty()) {
                    player.sendMessage(cancelMessage);
                } else {
                    player.sendMessage(ChatColor.RED + "The transfer was cancelled because you have already moved.");
                }
                
                if (!cancelActionBar.isEmpty()) {
                    player.sendActionBar(cancelActionBar);
                } else {
                    player.sendActionBar(ChatColor.RED + "Transfer cancelled - you moved");
                }
                
                cancelTeleport(player);
                scheduledTask.cancel();
                return;
            }
            
            if (currentCountdown <= 0) {
                player.teleportAsync(targetLocation).thenAccept(success -> {
                    if (success) {
                        String successMessage = getMessageFromFile(module, "chat-teleport-success");
                        String successActionBar = getMessageFromFile(module, "actionbar-teleport-success");
                        
                        if (!successMessage.isEmpty()) {
                            player.sendMessage(successMessage);
                        } else {
                            player.sendMessage(ChatColor.GREEN + "Teleported successfully!");
                        }
                        
                        if (!successActionBar.isEmpty()) {
                            player.sendActionBar(successActionBar);
                        } else {
                            player.sendActionBar(ChatColor.GREEN + "Teleported!");
                        }
                        
                        if (teleportCompletes.containsKey(uuid)) {
                            teleportCompletes.get(uuid).run();
                        }
                    }
                });
                cancelTeleport(player);
                scheduledTask.cancel();
                return;
            }
            
            String countdownMessage = getMessageFromFile(module, "chat-teleport-countdown");
            String countdownActionBar = getMessageFromFile(module, "actionbar-teleport-countdown");
            
            countdownMessage = countdownMessage.replace("%time%", String.valueOf(currentCountdown));
            countdownActionBar = countdownActionBar.replace("%time%", String.valueOf(currentCountdown));
            
            if (!countdownMessage.isEmpty()) {
                player.sendMessage(countdownMessage);
            } else {
                player.sendMessage(ChatColor.YELLOW + "Teleporting in " + currentCountdown + " seconds...");
            }
            
            if (!countdownActionBar.isEmpty()) {
                player.sendActionBar(countdownActionBar);
            } else {
                player.sendActionBar(ChatColor.YELLOW + "Teleporting in " + currentCountdown + "s");
            }
            
            countdowns.put(uuid, currentCountdown - 1);
            
        }, null, 1L, 20L);
        
        activeTeleports.put(uuid, task);
    }
    
    private String getMessageFromFile(String module, String path) {
        try {
            File file = new File(plugin.getDataFolder(), module + "/message.yml");
            if (file.exists()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String prefix = config.getString("prefix", "");
                String message = config.getString("message." + path, "");
                if (!message.isEmpty()) {
                    return ChatColor.translateAlternateColorCodes('&', prefix + message);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load message from " + module + "/message.yml: " + e.getMessage());
        }
        return "";
    }
    
    public void cancelTeleport(Player player) {
        UUID uuid = player.getUniqueId();
        io.papermc.paper.threadedregions.scheduler.ScheduledTask task = activeTeleports.remove(uuid);
        if (task != null) {
            task.cancel();
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
        return activeTeleports.containsKey(player.getUniqueId());
    }
            }

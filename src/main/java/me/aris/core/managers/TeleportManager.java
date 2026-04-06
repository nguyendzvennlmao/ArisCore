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
                String cancelMsg = getMessage(currentData.module, "chat-teleport-cancelled-movement");
                String cancelAction = getMessage(currentData.module, "actionbar-teleport-cancelled-movement");
                
                if (cancelMsg != null && !cancelMsg.isEmpty()) {
                    player.sendMessage(cancelMsg);
                } else {
                    player.sendMessage(ChatColor.RED + "The transfer was cancelled because you have already moved.");
                }
                
                if (cancelAction != null && !cancelAction.isEmpty()) {
                    player.sendActionBar(cancelAction);
                } else {
                    player.sendActionBar(ChatColor.RED + "Transfer cancelled - you moved");
                }
                
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
                        String successMsg = getMessage(currentData.module, "chat-teleport-success");
                        String successAction = getMessage(currentData.module, "actionbar-teleport-success");
                        
                        if (successMsg != null && !successMsg.isEmpty()) {
                            player.sendMessage(successMsg);
                        } else {
                            player.sendMessage(ChatColor.GREEN + "Teleported successfully!");
                        }
                        
                        if (successAction != null && !successAction.isEmpty()) {
                            player.sendActionBar(successAction);
                        } else {
                            player.sendActionBar(ChatColor.GREEN + "Teleported!");
                        }
                        
                        if (currentData.onComplete != null) {
                            currentData.onComplete.run();
                        }
                    }
                });
                if (scheduledTask != null) scheduledTask.cancel();
                activeTeleports.remove(player.getUniqueId());
                return;
            }
            
            String countMsg = getMessage(currentData.module, "chat-teleport-countdown");
            String countAction = getMessage(currentData.module, "actionbar-teleport-countdown");
            
            if (countMsg != null && !countMsg.isEmpty()) {
                player.sendMessage(countMsg.replace("%time%", String.valueOf(currentData.countdown)));
            } else {
                player.sendMessage(ChatColor.YELLOW + "Teleporting in " + currentData.countdown + " seconds...");
            }
            
            if (countAction != null && !countAction.isEmpty()) {
                player.sendActionBar(countAction.replace("%time%", String.valueOf(currentData.countdown)));
            } else {
                player.sendActionBar(ChatColor.YELLOW + "Teleporting in " + currentData.countdown + "s");
            }
            
            currentData.countdown--;
            
        }, null, 1L, 20L);
    }
    
    private String getMessage(String module, String path) {
        try {
            File file = new File(plugin.getDataFolder(), module + "/message.yml");
            if (file.exists()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String prefix = config.getString("prefix", "");
                String message = config.getString("message." + path, "");
                if (message != null && !message.isEmpty()) {
                    return ChatColor.translateAlternateColorCodes('&', prefix + message);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
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

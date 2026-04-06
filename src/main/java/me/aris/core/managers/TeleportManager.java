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
    
    public TeleportManager(ArisCore plugin) {
        this.plugin = plugin;
        this.activeTeleports = new HashMap<>();
    }
    
    public void startTeleport(Player player, Location targetLocation, String module, Runnable onComplete, Runnable onCancel) {
        cancelTeleport(player);
        
        TeleportTask task = new TeleportTask(player, targetLocation, module, onComplete, onCancel);
        task.start();
    }
    
    public void cancelTeleport(Player player) {
        io.papermc.paper.threadedregions.scheduler.ScheduledTask task = activeTeleports.remove(player.getUniqueId());
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
        private String module;
        private Runnable onComplete;
        private Runnable onCancel;
        private int currentCountdown;
        private io.papermc.paper.threadedregions.scheduler.ScheduledTask task;
        private boolean cancelled;
        private boolean hasSentCancelMessage;
        private boolean hasTeleported;
        
        public TeleportTask(Player player, Location targetLocation, String module, Runnable onComplete, Runnable onCancel) {
            this.player = player;
            this.startLocation = player.getLocation().clone();
            this.targetLocation = targetLocation;
            this.module = module;
            this.onComplete = onComplete;
            this.onCancel = onCancel;
            this.currentCountdown = plugin.getConfigManager().getTeleportCountdown();
            this.cancelled = false;
            this.hasSentCancelMessage = false;
            this.hasTeleported = false;
        }
        
        public void start() {
            task = player.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
                if (cancelled || !player.isOnline()) {
                    scheduledTask.cancel();
                    activeTeleports.remove(player.getUniqueId());
                    return;
                }
                
                boolean moved = checkIfMoved();
                
                if (moved && !hasSentCancelMessage && currentCountdown > 0) {
                    hasSentCancelMessage = true;
                    sendCancelMessage();
                    cancelTeleport();
                    scheduledTask.cancel();
                    activeTeleports.remove(player.getUniqueId());
                    if (onCancel != null) {
                        onCancel.run();
                    }
                    return;
                }
                
                if (currentCountdown <= 0 && !hasTeleported) {
                    hasTeleported = true;
                    executeTeleport();
                    scheduledTask.cancel();
                    activeTeleports.remove(player.getUniqueId());
                    return;
                }
                
                if (currentCountdown > 0) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("time", String.valueOf(currentCountdown));
                    plugin.getMessageManager().sendTeleportCountdown(player, module, currentCountdown, placeholders);
                    currentCountdown--;
                }
            }, null, 1L, 20L);
            
            activeTeleports.put(player.getUniqueId(), task);
        }
        
        private boolean checkIfMoved() {
            Location currentLocation = player.getLocation();
            double dx = Math.abs(startLocation.getX() - currentLocation.getX());
            double dz = Math.abs(startLocation.getZ() - currentLocation.getZ());
            return dx > 0.05 || dz > 0.05;
        }
        
        private void sendCancelMessage() {
            File file = new File(plugin.getDataFolder(), module + "/message.yml");
            if (file.exists()) {
                try {
                    FileConfiguration msgConfig = YamlConfiguration.loadConfiguration(file);
                    String prefix = msgConfig.getString("prefix", "");
                    String chatMessage = msgConfig.getString("message.chat-teleport-cancelled-movement", "");
                    String actionBarMessage = msgConfig.getString("message.actionbar-teleport-cancelled-movement", "");
                    
                    File configFile = new File(plugin.getDataFolder(), module + "/config.yml");
                    boolean chatEnabled = true;
                    boolean actionBarEnabled = true;
                    
                    if (configFile.exists()) {
                        FileConfiguration moduleConfig = YamlConfiguration.loadConfiguration(configFile);
                        chatEnabled = moduleConfig.getBoolean("messages.chat", true);
                        actionBarEnabled = moduleConfig.getBoolean("messages.action-bar", true);
                    }
                    
                    if (chatEnabled && !chatMessage.isEmpty()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + chatMessage));
                    }
                    
                    if (actionBarEnabled && !actionBarMessage.isEmpty()) {
                        player.sendActionBar(ChatColor.translateAlternateColorCodes('&', actionBarMessage));
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to send cancel message: " + e.getMessage());
                    player.sendMessage(ChatColor.RED + "Teleport cancelled - you moved!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Teleport cancelled - you moved!");
            }
        }
        
        private void executeTeleport() {
            if (cancelled || !player.isOnline()) return;
            
            Location finalLocation = targetLocation.clone();
            player.teleportAsync(finalLocation).thenAccept(success -> {
                if (success) {
                    plugin.getMessageManager().sendTeleportSuccess(player, module);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
        }
        
        private void cancelTeleport() {
            cancelled = true;
        }
    }
        }

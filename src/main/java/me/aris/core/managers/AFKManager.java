package me.aris.core.managers;

import me.aris.core.ArisCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKManager {
    private ArisCore plugin;
    private Map<UUID, Boolean> afkPlayers;
    private Map<UUID, Long> lastActiveTime;
    private Location afkLocation;
    private File afkLocationFile;
    private YamlConfiguration afkLocationConfig;
    private io.papermc.paper.threadedregions.scheduler.ScheduledTask task;
    
    public AFKManager(ArisCore plugin) {
        this.plugin = plugin;
        this.afkPlayers = new HashMap<>();
        this.lastActiveTime = new HashMap<>();
        this.afkLocationFile = new File(plugin.getDataFolder(), "afk-location.yml");
        loadAFKLocation();
        startAutoAFKTask();
    }
    
    private void loadAFKLocation() {
        if (!afkLocationFile.exists()) {
            try {
                afkLocationFile.createNewFile();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to create afk-location.yml: " + e.getMessage());
            }
        }
        afkLocationConfig = YamlConfiguration.loadConfiguration(afkLocationFile);
        if (afkLocationConfig.contains("world")) {
            afkLocation = new Location(
                Bukkit.getWorld(afkLocationConfig.getString("world")),
                afkLocationConfig.getDouble("x"),
                afkLocationConfig.getDouble("y"),
                afkLocationConfig.getDouble("z"),
                (float) afkLocationConfig.getDouble("yaw"),
                (float) afkLocationConfig.getDouble("pitch")
            );
        }
    }
    
    public void saveAFKLocation() {
        if (afkLocation != null) {
            afkLocationConfig.set("world", afkLocation.getWorld().getName());
            afkLocationConfig.set("x", afkLocation.getX());
            afkLocationConfig.set("y", afkLocation.getY());
            afkLocationConfig.set("z", afkLocation.getZ());
            afkLocationConfig.set("yaw", afkLocation.getYaw());
            afkLocationConfig.set("pitch", afkLocation.getPitch());
            try {
                afkLocationConfig.save(afkLocationFile);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save AFK location: " + e.getMessage());
            }
        }
    }
    
    public void setAFKLocation(Location location) {
        this.afkLocation = location;
        saveAFKLocation();
    }
    
    public void deleteAFKLocation() {
        this.afkLocation = null;
        if (afkLocationFile.exists()) {
            afkLocationFile.delete();
        }
    }
    
    public Location getAFKLocation() {
        return afkLocation;
    }
    
    public boolean isAFK(Player player) {
        return afkPlayers.getOrDefault(player.getUniqueId(), false);
    }
    
    public void setAFK(Player player, boolean afk) {
        if (afk) {
            afkPlayers.put(player.getUniqueId(), true);
            plugin.getMessageManager().sendMessage(player, "afk-on", "afk");
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    plugin.getMessageManager().sendMessage(online, "afk-on-other", "afk", "player", player.getName());
                }
            }
            if (afkLocation != null) {
                startTeleportToAFKLocation(player);
            }
        } else {
            afkPlayers.remove(player.getUniqueId());
            plugin.getMessageManager().sendMessage(player, "afk-off", "afk");
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    plugin.getMessageManager().sendMessage(online, "afk-off-other", "afk", "player", player.getName());
                }
            }
        }
    }
    
    private void startTeleportToAFKLocation(Player player) {
        int countdown = plugin.getConfigManager().getTeleportCountdown();
        plugin.getTeleportManager().startTeleport(player, afkLocation, 
            () -> {
                plugin.getMessageManager().sendMessage(player, "teleport-success", "afk");
            },
            () -> {
                plugin.getMessageManager().sendMessage(player, "teleport-cancelled-movement", "afk");
            }
        );
    }
    
    private void startAutoAFKTask() {
        int autoAFKTime = plugin.getConfigManager().getAfkConfig().getInt("auto-afk-time", 300);
        int checkInterval = plugin.getConfigManager().getAfkConfig().getInt("check-interval", 10);
        
        task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!isAFK(player)) {
                    long lastActive = lastActiveTime.getOrDefault(player.getUniqueId(), System.currentTimeMillis());
                    if (System.currentTimeMillis() - lastActive > autoAFKTime * 1000L) {
                        setAFK(player, true);
                    }
                }
            }
        }, 1L, checkInterval * 20L);
    }
    
    public void updateActivity(Player player) {
        lastActiveTime.put(player.getUniqueId(), System.currentTimeMillis());
        if (isAFK(player)) {
            setAFK(player, false);
        }
    }
    
    public void shutdown() {
        if (task != null) {
            task.cancel();
        }
    }
                }

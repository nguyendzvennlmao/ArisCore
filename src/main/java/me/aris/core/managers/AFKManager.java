package me.aris.core.managers;

import me.aris.core.ArisCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKManager {
    private ArisCore plugin;
    private Map<UUID, Boolean> afkPlayers;
    private Map<UUID, Long> lastActiveTime;
    private Location afkLocation;
    private File afkFile;
    private YamlConfiguration afkConfig;
    private io.papermc.paper.threadedregions.scheduler.ScheduledTask task;
    
    public AFKManager(ArisCore plugin) {
        this.plugin = plugin;
        this.afkPlayers = new HashMap<>();
        this.lastActiveTime = new HashMap<>();
        File locationFolder = new File(plugin.getDataFolder(), "Location");
        if (!locationFolder.exists()) {
            locationFolder.mkdirs();
        }
        this.afkFile = new File(locationFolder, "afk.yml");
        loadAFKLocation();
        startAutoAFKTask();
    }
    
    private void loadAFKLocation() {
        if (!afkFile.exists()) {
            try {
                afkFile.createNewFile();
                afkConfig = YamlConfiguration.loadConfiguration(afkFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create afk.yml: " + e.getMessage());
                afkConfig = new YamlConfiguration();
            }
        } else {
            afkConfig = YamlConfiguration.loadConfiguration(afkFile);
        }
        
        if (afkConfig.contains("world")) {
            World world = Bukkit.getWorld(afkConfig.getString("world"));
            if (world != null) {
                afkLocation = new Location(
                    world,
                    afkConfig.getDouble("x"),
                    afkConfig.getDouble("y"),
                    afkConfig.getDouble("z"),
                    (float) afkConfig.getDouble("yaw"),
                    (float) afkConfig.getDouble("pitch")
                );
            }
        }
    }
    
    public void saveAFKLocation() {
        if (afkLocation != null && afkLocation.getWorld() != null) {
            afkConfig.set("world", afkLocation.getWorld().getName());
            afkConfig.set("x", afkLocation.getX());
            afkConfig.set("y", afkLocation.getY());
            afkConfig.set("z", afkLocation.getZ());
            afkConfig.set("yaw", afkLocation.getYaw());
            afkConfig.set("pitch", afkLocation.getPitch());
            try {
                afkConfig.save(afkFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save afk.yml: " + e.getMessage());
            }
        }
    }
    
    public void setAFKLocation(Location location) {
        this.afkLocation = location.clone();
        saveAFKLocation();
    }
    
    public void deleteAFKLocation() {
        this.afkLocation = null;
        if (afkFile.exists()) {
            afkFile.delete();
        }
        try {
            afkFile.createNewFile();
            afkConfig = YamlConfiguration.loadConfiguration(afkFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to reset afk.yml: " + e.getMessage());
        }
    }
    
    public Location getAFKLocation() {
        return afkLocation != null ? afkLocation.clone() : null;
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
                plugin.getAfkTeleport().teleport(player, afkLocation);
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
        }, 20L, checkInterval * 20L);
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

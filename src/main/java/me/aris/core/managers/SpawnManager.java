package me.aris.core.managers;

import me.aris.core.ArisCore;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;

public class SpawnManager {
    private ArisCore plugin;
    private Location spawnLocation;
    private File spawnFile;
    private YamlConfiguration spawnConfig;
    
    public SpawnManager(ArisCore plugin) {
        this.plugin = plugin;
        this.spawnFile = new File(plugin.getDataFolder(), "spawn.yml");
        loadSpawn();
    }
    
    private void loadSpawn() {
        if (!spawnFile.exists()) {
            plugin.saveResource("spawn.yml", false);
        }
        spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);
        
        if (spawnConfig.contains("world")) {
            spawnLocation = new Location(
                plugin.getServer().getWorld(spawnConfig.getString("world")),
                spawnConfig.getDouble("x"),
                spawnConfig.getDouble("y"),
                spawnConfig.getDouble("z"),
                (float) spawnConfig.getDouble("yaw"),
                (float) spawnConfig.getDouble("pitch")
            );
        }
    }
    
    public void saveSpawn() {
        if (spawnLocation != null) {
            spawnConfig.set("world", spawnLocation.getWorld().getName());
            spawnConfig.set("x", spawnLocation.getX());
            spawnConfig.set("y", spawnLocation.getY());
            spawnConfig.set("z", spawnLocation.getZ());
            spawnConfig.set("yaw", spawnLocation.getYaw());
            spawnConfig.set("pitch", spawnLocation.getPitch());
            try {
                spawnConfig.save(spawnFile);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save spawn: " + e.getMessage());
            }
        }
    }
    
    public void setSpawn(Location location) {
        this.spawnLocation = location;
        saveSpawn();
    }
    
    public void deleteSpawn() {
        this.spawnLocation = null;
        if (spawnFile.exists()) {
            spawnFile.delete();
        }
    }
    
    public Location getSpawn() {
        return spawnLocation;
    }
    
    public boolean hasSpawn() {
        return spawnLocation != null;
    }
                  }

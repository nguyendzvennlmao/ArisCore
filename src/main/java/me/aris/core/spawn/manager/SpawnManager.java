package me.aris.core.spawn.manager;

import me.aris.core.ArisCore;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public class SpawnManager {
    private ArisCore plugin;
    private Location spawnLocation;
    private File spawnFile;
    private YamlConfiguration spawnConfig;
    
    public SpawnManager(ArisCore plugin) {
        this.plugin = plugin;
        this.spawnFile = new File(plugin.getDataFolder(), "Location/spawn.yml");
        loadSpawn();
    }
    
    private void loadSpawn() {
        if (!spawnFile.exists()) {
            try {
                spawnFile.createNewFile();
                spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);
            } catch (IOException e) {
                spawnConfig = new YamlConfiguration();
            }
        } else {
            spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);
        }
        
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
        if (spawnLocation != null && spawnLocation.getWorld() != null) {
            spawnConfig.set("world", spawnLocation.getWorld().getName());
            spawnConfig.set("x", spawnLocation.getX());
            spawnConfig.set("y", spawnLocation.getY());
            spawnConfig.set("z", spawnLocation.getZ());
            spawnConfig.set("yaw", spawnLocation.getYaw());
            spawnConfig.set("pitch", spawnLocation.getPitch());
            try {
                spawnConfig.save(spawnFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save spawn.yml");
            }
        }
    }
    
    public void setSpawn(Location location) {
        this.spawnLocation = location.clone();
        saveSpawn();
    }
    
    public void deleteSpawn() {
        this.spawnLocation = null;
        if (spawnFile.exists()) {
            spawnFile.delete();
        }
        try {
            spawnFile.createNewFile();
            spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to reset spawn.yml");
        }
    }
    
    public Location getSpawn() {
        return spawnLocation != null ? spawnLocation.clone() : null;
    }
    
    public boolean hasSpawn() {
        return spawnLocation != null && spawnLocation.getWorld() != null;
    }
                  }

package me.aris.core.managers;

import me.aris.core.ArisCore;
import me.aris.core.models.Warp;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WarpManager {
    private ArisCore plugin;
    private Map<String, Warp> warps;
    private File warpsFile;
    private YamlConfiguration warpsConfig;
    
    public WarpManager(ArisCore plugin) {
        this.plugin = plugin;
        this.warps = new ConcurrentHashMap<>();
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        loadWarps();
    }
    
    private void loadWarps() {
        if (!warpsFile.exists()) {
            plugin.saveResource("warps.yml", false);
        }
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
        
        for (String warpName : warpsConfig.getKeys(false)) {
            Location loc = new Location(
                plugin.getServer().getWorld(warpsConfig.getString(warpName + ".world")),
                warpsConfig.getDouble(warpName + ".x"),
                warpsConfig.getDouble(warpName + ".y"),
                warpsConfig.getDouble(warpName + ".z"),
                (float) warpsConfig.getDouble(warpName + ".yaw"),
                (float) warpsConfig.getDouble(warpName + ".pitch")
            );
            warps.put(warpName.toLowerCase(), new Warp(warpName, loc));
        }
    }
    
    public void saveWarps() {
        for (Map.Entry<String, Warp> entry : warps.entrySet()) {
            Location loc = entry.getValue().getLocation();
            warpsConfig.set(entry.getKey() + ".world", loc.getWorld().getName());
            warpsConfig.set(entry.getKey() + ".x", loc.getX());
            warpsConfig.set(entry.getKey() + ".y", loc.getY());
            warpsConfig.set(entry.getKey() + ".z", loc.getZ());
            warpsConfig.set(entry.getKey() + ".yaw", loc.getYaw());
            warpsConfig.set(entry.getKey() + ".pitch", loc.getPitch());
        }
        
        try {
            warpsConfig.save(warpsFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save warps: " + e.getMessage());
        }
    }
    
    public boolean addWarp(Player player, String name, Location location) {
        if (warps.containsKey(name.toLowerCase())) {
            return false;
        }
        warps.put(name.toLowerCase(), new Warp(name, location));
        return true;
    }
    
    public boolean removeWarp(String name) {
        if (!warps.containsKey(name.toLowerCase())) {
            return false;
        }
        warps.remove(name.toLowerCase());
        return true;
    }
    
    public Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }
    
    public Map<String, Warp> getWarps() {
        return new HashMap<>(warps);
    }
    
    public boolean warpExists(String name) {
        return warps.containsKey(name.toLowerCase());
    }
                                      }

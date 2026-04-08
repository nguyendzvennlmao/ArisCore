package me.aris.core.warp.manager;

import me.aris.core.ArisCore;
import me.aris.core.warp.model.Warp;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
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
        this.warpsFile = new File(plugin.getDataFolder(), "Location/warp.yml");
        loadWarps();
    }
    
    private void loadWarps() {
        if (!warpsFile.exists()) {
            try {
                warpsFile.createNewFile();
                warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
            } catch (IOException e) {
                warpsConfig = new YamlConfiguration();
            }
        } else {
            warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
        }
        
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
        for (String key : warpsConfig.getKeys(false)) {
            warpsConfig.set(key, null);
        }
        
        for (Map.Entry<String, Warp> entry : warps.entrySet()) {
            Location loc = entry.getValue().getLocation();
            if (loc != null && loc.getWorld() != null) {
                warpsConfig.set(entry.getKey() + ".world", loc.getWorld().getName());
                warpsConfig.set(entry.getKey() + ".x", loc.getX());
                warpsConfig.set(entry.getKey() + ".y", loc.getY());
                warpsConfig.set(entry.getKey() + ".z", loc.getZ());
                warpsConfig.set(entry.getKey() + ".yaw", loc.getYaw());
                warpsConfig.set(entry.getKey() + ".pitch", loc.getPitch());
            }
        }
        
        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save warp.yml");
        }
    }
    
    public boolean addWarp(Player player, String name, Location location) {
        if (warps.containsKey(name.toLowerCase())) {
            return false;
        }
        warps.put(name.toLowerCase(), new Warp(name, location.clone()));
        saveWarps();
        return true;
    }
    
    public boolean removeWarp(String name) {
        if (!warps.containsKey(name.toLowerCase())) {
            return false;
        }
        warps.remove(name.toLowerCase());
        saveWarps();
        return true;
    }
    
    public Warp getWarp(String name) {
        Warp warp = warps.get(name.toLowerCase());
        if (warp != null && warp.getLocation() != null) {
            return new Warp(warp.getName(), warp.getLocation().clone());
        }
        return null;
    }
    
    public Map<String, Warp> getWarps() {
        return new HashMap<>(warps);
    }
    
    public boolean warpExists(String name) {
        return warps.containsKey(name.toLowerCase());
    }
            }

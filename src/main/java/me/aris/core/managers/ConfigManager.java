package me.aris.core.managers;

import me.aris.core.ArisCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class ConfigManager {
    private ArisCore plugin;
    private FileConfiguration config;
    private FileConfiguration afkConfig;
    private FileConfiguration homeConfig;
    private FileConfiguration warpConfig;
    private FileConfiguration spawnConfig;
    private FileConfiguration tpaConfig;
    private FileConfiguration rtpConfig;
    
    public ConfigManager(ArisCore plugin) {
        this.plugin = plugin;
        reloadConfigs();
    }
    
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        afkConfig = loadModuleConfig("Afk");
        homeConfig = loadModuleConfig("Home");
        warpConfig = loadModuleConfig("Warp");
        spawnConfig = loadModuleConfig("Spawn");
        tpaConfig = loadModuleConfig("Tpa");
        rtpConfig = loadModuleConfig("Rtp");
    }
    
    private FileConfiguration loadModuleConfig(String module) {
        File file = new File(plugin.getDataFolder(), module + "/config.yml");
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return new YamlConfiguration();
    }
    
    public boolean isModuleEnabled(String module) {
        return config.getBoolean("modules." + module, true);
    }
    
    public int getTeleportCountdown() {
        return config.getInt("settings.teleport.countdown", 5);
    }
    
    public double getAllowedWalkRange() {
        return config.getDouble("settings.teleport.allowed-walk-range", 0.1);
    }
    
    public boolean isCancelOnMove() {
        return config.getBoolean("settings.teleport.cancel-on-move", true);
    }
    
    public boolean isCancelOnDamage() {
        return config.getBoolean("settings.teleport.cancel-on-damage", true);
    }
    
    public boolean isCancelOnFly() {
        return config.getBoolean("settings.teleport.cancel-on-fly", true);
    }
    
    public FileConfiguration getAfkConfig() { return afkConfig; }
    public FileConfiguration getHomeConfig() { return homeConfig; }
    public FileConfiguration getWarpConfig() { return warpConfig; }
    public FileConfiguration getSpawnConfig() { return spawnConfig; }
    public FileConfiguration getTpaConfig() { return tpaConfig; }
    public FileConfiguration getRtpConfig() { return rtpConfig; }
    }

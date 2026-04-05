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
    private FileConfiguration homeGuiConfig;
    private FileConfiguration warpConfig;
    private FileConfiguration warpGuiConfig;
    private FileConfiguration spawnConfig;
    private FileConfiguration tpaConfig;
    private FileConfiguration rtpConfig;
    private FileConfiguration rtpGuiConfig;
    
    public ConfigManager(ArisCore plugin) {
        this.plugin = plugin;
        reloadConfigs();
    }
    
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        afkConfig = loadModuleConfig("Afk", "config.yml");
        homeConfig = loadModuleConfig("Home", "config.yml");
        homeGuiConfig = loadModuleConfig("Home/gui", "home.yml");
        warpConfig = loadModuleConfig("Warp", "config.yml");
        warpGuiConfig = loadModuleConfig("Warp", "gui.yml");
        spawnConfig = loadModuleConfig("Spawn", "config.yml");
        tpaConfig = loadModuleConfig("Tpa", "config.yml");
        rtpConfig = loadModuleConfig("Rtp", "config.yml");
        rtpGuiConfig = loadModuleConfig("Rtp", "gui.yml");
    }
    
    private FileConfiguration loadModuleConfig(String module, String fileName) {
        File file = new File(plugin.getDataFolder(), module + "/" + fileName);
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return new YamlConfiguration();
    }
    
    public boolean isModuleEnabled(String module) {
        return config.getBoolean("modules." + module, true);
    }
    
    public boolean isChatEnabled(String module) {
        FileConfiguration moduleConfig = getModuleConfig(module);
        return moduleConfig.getBoolean("messages.chat", true);
    }
    
    public boolean isActionBarEnabled(String module) {
        FileConfiguration moduleConfig = getModuleConfig(module);
        return moduleConfig.getBoolean("messages.action-bar", true);
    }
    
    private FileConfiguration getModuleConfig(String module) {
        switch (module.toLowerCase()) {
            case "afk": return afkConfig;
            case "home": return homeConfig;
            case "spawn": return spawnConfig;
            case "tpa": return tpaConfig;
            case "warp": return warpConfig;
            case "rtp": return rtpConfig;
            default: return config;
        }
    }
    
    public int getTeleportCountdown() {
        return config.getInt("settings.teleport.countdown", 5);
    }
    
    public double getAllowedWalkRange() {
        return config.getDouble("settings.teleport.allowed-walk-range", 0.1);
    }
    
    public FileConfiguration getAfkConfig() { return afkConfig; }
    public FileConfiguration getHomeConfig() { return homeConfig; }
    public FileConfiguration getHomeGuiConfig() { return homeGuiConfig; }
    public FileConfiguration getWarpConfig() { return warpConfig; }
    public FileConfiguration getWarpGuiConfig() { return warpGuiConfig; }
    public FileConfiguration getSpawnConfig() { return spawnConfig; }
    public FileConfiguration getTpaConfig() { return tpaConfig; }
    public FileConfiguration getRtpConfig() { return rtpConfig; }
    public FileConfiguration getRtpGuiConfig() { return rtpGuiConfig; }
            }

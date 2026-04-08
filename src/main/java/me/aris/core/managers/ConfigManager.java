package me.aris.core.managers;

import me.aris.core.ArisCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class ConfigManager {
    private ArisCore plugin;
    private FileConfiguration config;
    
    public ConfigManager(ArisCore plugin) {
        this.plugin = plugin;
        reloadConfigs();
    }
    
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
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
    
    public FileConfiguration getModuleConfig(String module) {
        File file = new File(plugin.getDataFolder(), module + "/config.yml");
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return new YamlConfiguration();
    }
    
    public FileConfiguration getModuleMessage(String module) {
        File file = new File(plugin.getDataFolder(), module + "/message.yml");
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return new YamlConfiguration();
    }
    
    public FileConfiguration getModuleSound(String module) {
        File file = new File(plugin.getDataFolder(), module + "/sound.yml");
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return new YamlConfiguration();
    }
  }

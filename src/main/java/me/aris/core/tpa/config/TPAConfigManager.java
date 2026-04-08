package me.aris.core.tpa.config;

import me.aris.core.ArisCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class TPAConfigManager {
    private ArisCore plugin;
    private FileConfiguration config;
    private FileConfiguration messageConfig;
    private FileConfiguration soundConfig;
    private FileConfiguration tpaGuiConfig;
    private FileConfiguration tpahereGuiConfig;
    
    public TPAConfigManager(ArisCore plugin) {
        this.plugin = plugin;
        loadConfigs();
    }
    
    private void loadConfigs() {
        config = loadYaml("tpa/config.yml");
        messageConfig = loadYaml("tpa/message.yml");
        soundConfig = loadYaml("tpa/sound.yml");
        tpaGuiConfig = loadYaml("tpa/gui/tpa.yml");
        tpahereGuiConfig = loadYaml("tpa/gui/tpahere.yml");
    }
    
    private FileConfiguration loadYaml(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return new YamlConfiguration();
    }
    
    public int getTeleportCountdown() {
        return config.getInt("teleport.countdown", 5);
    }
    
    public double getAllowedWalkRange() {
        return config.getDouble("teleport.allowed-walk-range", 0.1);
    }
    
    public int getExpirationTime() {
        return config.getInt("request.expiration-time", 120);
    }
    
    public int getCooldown() {
        return config.getInt("request.cooldown", 30);
    }
    
    public boolean isChatEnabled() {
        return config.getBoolean("messages.chat", true);
    }
    
    public boolean isActionBarEnabled() {
        return config.getBoolean("messages.action-bar", true);
    }
    
    public boolean isGUIEnabled() {
        return config.getBoolean("gui.enabled", true);
    }
    
    public FileConfiguration getConfig() { return config; }
    public FileConfiguration getMessageConfig() { return messageConfig; }
    public FileConfiguration getSoundConfig() { return soundConfig; }
    public FileConfiguration getTPAGUIConfig() { return tpaGuiConfig; }
    public FileConfiguration getTPAHereGUIConfig() { return tpahereGuiConfig; }
          }

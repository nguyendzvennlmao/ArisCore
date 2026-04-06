package me.aris.core.shop;

import me.aris.core.ArisCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CategoryManager {
    private ArisCore plugin;
    private FileConfiguration shopConfig;
    private Map<String, FileConfiguration> categoryConfigs;
    
    public CategoryManager(ArisCore plugin) {
        this.plugin = plugin;
        this.categoryConfigs = new HashMap<>();
        loadConfigs();
    }
    
    private void loadConfigs() {
        File configFile = new File(plugin.getDataFolder(), "Shop/config.yml");
        if (configFile.exists()) {
            shopConfig = YamlConfiguration.loadConfiguration(configFile);
        } else {
            shopConfig = new YamlConfiguration();
        }
        
        String[] categories = {"end", "nether", "gear", "food", "shards"};
        for (String category : categories) {
            File file = new File(plugin.getDataFolder(), "Shop/gui/" + category + ".yml");
            if (file.exists()) {
                categoryConfigs.put(category, YamlConfiguration.loadConfiguration(file));
            }
        }
    }
    
    public FileConfiguration getShopConfig() {
        return shopConfig;
    }
    
    public FileConfiguration getCategoryConfig(String category) {
        return categoryConfigs.get(category);
    }
    
    public Map<String, FileConfiguration> getAllCategoryConfigs() {
        return categoryConfigs;
    }
    
    public void reload() {
        categoryConfigs.clear();
        loadConfigs();
    }
              }

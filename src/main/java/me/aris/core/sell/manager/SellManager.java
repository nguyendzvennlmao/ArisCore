package me.aris.core.sell.manager;

import me.aris.core.ArisCore;
import me.aris.core.sell.model.SellItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SellManager {
    private ArisCore plugin;
    private Map<Material, Double> prices;
    private FileConfiguration sellConfig;
    
    public SellManager(ArisCore plugin) {
        this.plugin = plugin;
        this.prices = new HashMap<>();
        loadConfigs();
    }
    
    private void loadConfigs() {
        File configFile = new File(plugin.getDataFolder(), "sell/config.yml");
        if (configFile.exists()) {
            sellConfig = YamlConfiguration.loadConfiguration(configFile);
        } else {
            sellConfig = new YamlConfiguration();
        }
        
        File pricesFile = new File(plugin.getDataFolder(), "sell/prices.yml");
        if (pricesFile.exists()) {
            FileConfiguration pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);
            if (pricesConfig.contains("prices")) {
                for (String key : pricesConfig.getConfigurationSection("prices").getKeys(false)) {
                    try {
                        Material material = Material.valueOf(key);
                        double price = pricesConfig.getDouble("prices." + key);
                        prices.put(material, price);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material in prices.yml: " + key);
                    }
                }
            }
        }
    }
    
    public double getPrice(Material material) {
        return prices.getOrDefault(material, 0.0);
    }
    
    public boolean hasPrice(Material material) {
        return prices.containsKey(material);
    }
    
    public Map<Material, Double> getPrices() {
        return new HashMap<>(prices);
    }
    
    public boolean isSellable(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return prices.containsKey(item.getType());
    }
              }

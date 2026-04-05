package me.aris.core.managers;

import me.aris.core.ArisCore;
import me.aris.core.models.Home;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HomeManager {
    private ArisCore plugin;
    private Map<UUID, Map<String, Home>> homes;
    private File homesFile;
    private YamlConfiguration homesConfig;
    
    public HomeManager(ArisCore plugin) {
        this.plugin = plugin;
        this.homes = new ConcurrentHashMap<>();
        File locationFolder = new File(plugin.getDataFolder(), "Location");
        if (!locationFolder.exists()) {
            locationFolder.mkdirs();
        }
        this.homesFile = new File(locationFolder, "home.yml");
        loadHomes();
    }
    
    private void loadHomes() {
        if (!homesFile.exists()) {
            try {
                homesFile.createNewFile();
                homesConfig = YamlConfiguration.loadConfiguration(homesFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create home.yml: " + e.getMessage());
                homesConfig = new YamlConfiguration();
            }
        } else {
            homesConfig = YamlConfiguration.loadConfiguration(homesFile);
        }
        
        for (String uuidStr : homesConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, Home> playerHomes = new HashMap<>();
                
                for (String homeName : homesConfig.getConfigurationSection(uuidStr).getKeys(false)) {
                    String path = uuidStr + "." + homeName;
                    World world = plugin.getServer().getWorld(homesConfig.getString(path + ".world"));
                    if (world != null) {
                        Location loc = new Location(
                            world,
                            homesConfig.getDouble(path + ".x"),
                            homesConfig.getDouble(path + ".y"),
                            homesConfig.getDouble(path + ".z"),
                            (float) homesConfig.getDouble(path + ".yaw"),
                            (float) homesConfig.getDouble(path + ".pitch")
                        );
                        playerHomes.put(homeName.toLowerCase(), new Home(homeName, loc));
                    }
                }
                homes.put(uuid, playerHomes);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load homes for " + uuidStr + ": " + e.getMessage());
            }
        }
    }
    
    public void saveHomes() {
        for (String key : homesConfig.getKeys(false)) {
            homesConfig.set(key, null);
        }
        
        for (Map.Entry<UUID, Map<String, Home>> entry : homes.entrySet()) {
            String uuid = entry.getKey().toString();
            for (Map.Entry<String, Home> homeEntry : entry.getValue().entrySet()) {
                String path = uuid + "." + homeEntry.getKey();
                Location loc = homeEntry.getValue().getLocation();
                if (loc != null && loc.getWorld() != null) {
                    homesConfig.set(path + ".world", loc.getWorld().getName());
                    homesConfig.set(path + ".x", loc.getX());
                    homesConfig.set(path + ".y", loc.getY());
                    homesConfig.set(path + ".z", loc.getZ());
                    homesConfig.set(path + ".yaw", loc.getYaw());
                    homesConfig.set(path + ".pitch", loc.getPitch());
                }
            }
        }
        
        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save home.yml: " + e.getMessage());
        }
    }
    
    public int getMaxHomes(Player player) {
        org.bukkit.configuration.file.FileConfiguration homeConfig = plugin.getConfigManager().getHomeConfig();
        int max = homeConfig.getInt("max-homes.default", 1);
        
        if (homeConfig.contains("max-homes")) {
            for (String permission : homeConfig.getConfigurationSection("max-homes").getKeys(false)) {
                if (permission.equals("default")) continue;
                if (player.hasPermission(permission)) {
                    int value = homeConfig.getInt("max-homes." + permission);
                    if (value > max) max = value;
                }
            }
        }
        return max;
    }
    
    public boolean addHome(Player player, String name, Location location) {
        Map<String, Home> playerHomes = homes.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        
        if (playerHomes.containsKey(name.toLowerCase())) {
            return false;
        }
        
        if (playerHomes.size() >= getMaxHomes(player)) {
            return false;
        }
        
        playerHomes.put(name.toLowerCase(), new Home(name, location.clone()));
        saveHomes();
        return true;
    }
    
    public boolean removeHome(Player player, String name) {
        Map<String, Home> playerHomes = homes.get(player.getUniqueId());
        if (playerHomes == null || !playerHomes.containsKey(name.toLowerCase())) {
            return false;
        }
        playerHomes.remove(name.toLowerCase());
        saveHomes();
        return true;
    }
    
    public Home getHome(Player player, String name) {
        Map<String, Home> playerHomes = homes.get(player.getUniqueId());
        if (playerHomes == null) return null;
        Home home = playerHomes.get(name.toLowerCase());
        if (home != null && home.getLocation() != null) {
            return new Home(home.getName(), home.getLocation().clone());
        }
        return null;
    }
    
    public Map<String, Home> getHomes(Player player) {
        Map<String, Home> playerHomes = homes.getOrDefault(player.getUniqueId(), new HashMap<>());
        Map<String, Home> cloned = new HashMap<>();
        for (Map.Entry<String, Home> entry : playerHomes.entrySet()) {
            cloned.put(entry.getKey(), new Home(entry.getValue().getName(), entry.getValue().getLocation().clone()));
        }
        return cloned;
    }
    
    public boolean hasHome(Player player, String name) {
        Map<String, Home> playerHomes = homes.get(player.getUniqueId());
        return playerHomes != null && playerHomes.containsKey(name.toLowerCase());
    }
                }

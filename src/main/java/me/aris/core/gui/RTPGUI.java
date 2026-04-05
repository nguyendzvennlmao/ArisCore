package me.aris.core.gui;

import me.aris.core.ArisCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RTPGUI implements Listener {
    private ArisCore plugin;
    private Random random;
    private Map<UUID, Long> cooldowns;
    private FileConfiguration guiConfig;
    
    public RTPGUI(ArisCore plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.cooldowns = new ConcurrentHashMap<>();
        loadGuiConfig();
    }
    
    private void loadGuiConfig() {
        File guiFile = new File(plugin.getDataFolder(), "Rtp/gui.yml");
        if (guiFile.exists()) {
            guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        } else {
            guiConfig = new YamlConfiguration();
            plugin.saveResource("Rtp/gui.yml", false);
        }
    }
    
    private String translateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public void openRTPGUI(Player player) {
        loadGuiConfig();
        
        FileConfiguration rtpConfig = plugin.getConfigManager().getRtpConfig();
        int cooldownSeconds = rtpConfig.getInt("cooldown-seconds", 60);
        long lastUse = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long remaining = (lastUse + cooldownSeconds * 1000L) - System.currentTimeMillis();
        
        if (remaining > 0) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("time", String.valueOf(remaining / 1000));
            plugin.getMessageManager().sendMessage(player, "cooldown", "rtp", placeholders);
            return;
        }
        
        String title = guiConfig.getString("title", "&8ʀᴀɴᴅᴏᴍ ᴛᴇʟᴇᴘᴏʀᴛ");
        int rows = guiConfig.getInt("rows", 3);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateColors(title));
        
        for (String worldName : guiConfig.getConfigurationSection("worlds").getKeys(false)) {
            int slot = guiConfig.getInt("worlds." + worldName + ".slot");
            String itemName = guiConfig.getString("worlds." + worldName + ".item", "GRASS_BLOCK");
            String displayName = guiConfig.getString("worlds." + worldName + ".name", "&f" + worldName);
            List<String> lore = guiConfig.getStringList("worlds." + worldName + ".lore");
            
            Material material;
            try {
                material = Material.valueOf(itemName);
            } catch (IllegalArgumentException e) {
                material = Material.GRASS_BLOCK;
            }
            
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(translateColors(displayName));
            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = lore.stream()
                    .map(this::translateColors)
                    .toList();
                meta.setLore(coloredLore);
            }
            item.setItemMeta(meta);
            gui.setItem(slot, item);
        }
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        String guiTitle = translateColors(guiConfig.getString("title", "&8ʀᴀɴᴅᴏᴍ ᴛᴇʟᴇᴘᴏʀᴛ"));
        
        if (!title.equals(guiTitle)) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String clickedName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        
        for (String worldName : guiConfig.getConfigurationSection("worlds").getKeys(false)) {
            String displayName = ChatColor.stripColor(translateColors(guiConfig.getString("worlds." + worldName + ".name", "&f" + worldName)));
            
            if (clickedName.equals(displayName)) {
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getMessageManager().sendMessage(player, "world-not-configured", "rtp");
                    player.closeInventory();
                    return;
                }
                
                FileConfiguration rtpConfig = plugin.getConfigManager().getRtpConfig();
                int cooldownSeconds = rtpConfig.getInt("cooldown-seconds", 60);
                long lastUse = cooldowns.getOrDefault(player.getUniqueId(), 0L);
                long remaining = (lastUse + cooldownSeconds * 1000L) - System.currentTimeMillis();
                
                if (remaining > 0) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("time", String.valueOf(remaining / 1000));
                    plugin.getMessageManager().sendMessage(player, "cooldown", "rtp", placeholders);
                    player.closeInventory();
                    return;
                }
                
                player.closeInventory();
                
                World finalWorld = world;
                player.getScheduler().run(plugin, scheduledTask -> {
                    int maxRetries = rtpConfig.getInt("max-retries", 50);
                    int maxRadius = guiConfig.getInt("worlds." + worldName + ".max-radius", 5000);
                    int minRadius = guiConfig.getInt("worlds." + worldName + ".min-radius", 100);
                    int minY = guiConfig.getInt("worlds." + worldName + ".min-y", 63);
                    int maxY = guiConfig.getInt("worlds." + worldName + ".max-y", 120);
                    
                    Location safeLocation = findSafeLocation(finalWorld, maxRetries, maxRadius, minRadius, minY, maxY);
                    
                    if (safeLocation == null) {
                        plugin.getMessageManager().sendMessage(player, "no-safe-location", "rtp");
                        return;
                    }
                    
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                    
                    plugin.getTeleportManager().startTeleport(player, safeLocation, "rtp",
                        () -> {
                            plugin.getMessageManager().sendTeleportSuccess(player, "rtp");
                        },
                        () -> {
                            plugin.getMessageManager().sendTeleportCancelled(player, "rtp", "movement");
                        }
                    );
                }, null);
                break;
            }
        }
    }
    
    private Location findSafeLocation(World world, int maxRetries, int maxRadius, int minRadius, int minY, int maxY) {
        for (int i = 0; i < maxRetries; i++) {
            int x = random.nextInt(maxRadius * 2) - maxRadius;
            int z = random.nextInt(maxRadius * 2) - maxRadius;
            
            double distance = Math.sqrt(x * x + z * z);
            if (distance < minRadius) {
                continue;
            }
            
            int y = minY + random.nextInt(maxY - minY + 1);
            
            Location loc = new Location(world, x + 0.5, y, z + 0.5);
            if (isSafeLocation(loc)) {
                return loc;
            }
        }
        return null;
    }
    
    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        if (world == null) return false;
        
        org.bukkit.block.Block feetBlock = world.getBlockAt(x, y, z);
        org.bukkit.block.Block headBlock = world.getBlockAt(x, y + 1, z);
        org.bukkit.block.Block groundBlock = world.getBlockAt(x, y - 1, z);
        
        if (feetBlock.getType() != Material.AIR && !feetBlock.isPassable()) {
            return false;
        }
        
        if (headBlock.getType() != Material.AIR && !headBlock.isPassable()) {
            return false;
        }
        
        if (!groundBlock.getType().isSolid() && groundBlock.getType() != Material.GRASS_BLOCK && groundBlock.getType() != Material.SAND && groundBlock.getType() != Material.STONE) {
            return false;
        }
        
        if (groundBlock.getType() == Material.LAVA || groundBlock.getType() == Material.WATER) {
            return false;
        }
        
        return true;
    }
}

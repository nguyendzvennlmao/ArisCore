package me.aris.core.rtp.gui;

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

public class RTPGUI implements Listener {
    private ArisCore plugin;
    private Random random;
    private FileConfiguration guiConfig;
    
    public RTPGUI(ArisCore plugin) {
        this.plugin = plugin;
        this.random = new Random();
        loadGuiConfig();
    }
    
    private void loadGuiConfig() {
        File guiFile = new File(plugin.getDataFolder(), "rtp/gui.yml");
        if (guiFile.exists()) {
            guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        } else {
            guiConfig = new YamlConfiguration();
            plugin.saveResource("rtp/gui.yml", false);
        }
    }
    
    private String translateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public void openRTPGUI(Player player) {
        loadGuiConfig();
        
        int cooldownSeconds = plugin.getConfigManager().getModuleConfig("rtp").getInt("cooldown-seconds", 60);
        long lastUse = plugin.getRTPManager().getCooldownRemaining(player);
        long remaining = (lastUse + cooldownSeconds * 1000L) - System.currentTimeMillis();
        
        if (remaining > 0) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("time", String.valueOf(remaining / 1000));
            plugin.getRTPMessageManager().sendMessage(player, "cooldown", placeholders);
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
                    plugin.getRTPMessageManager().sendMessage(player, "world-not-configured");
                    player.closeInventory();
                    return;
                }
                
                int cooldownSeconds = plugin.getConfigManager().getModuleConfig("rtp").getInt("cooldown-seconds", 60);
                long lastUse = plugin.getRTPManager().getCooldownRemaining(player);
                long remaining = (lastUse + cooldownSeconds * 1000L) - System.currentTimeMillis();
                
                if (remaining > 0) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("time", String.valueOf(remaining / 1000));
                    plugin.getRTPMessageManager().sendMessage(player, "cooldown", placeholders);
                    player.closeInventory();
                    return;
                }
                
                player.closeInventory();
                
                int maxRetries = plugin.getConfigManager().getModuleConfig("rtp").getInt("max-retries", 50);
                int maxRadius = guiConfig.getInt("worlds." + worldName + ".max-radius", 5000);
                int minRadius = guiConfig.getInt("worlds." + worldName + ".min-radius", 100);
                int minY = guiConfig.getInt("worlds." + worldName + ".min-y", 63);
                int maxY = guiConfig.getInt("worlds." + worldName + ".max-y", 120);
                
                Location safeLocation = null;
                for (int i = 0; i < maxRetries; i++) {
                    Location loc = plugin.getRTPManager().getRandomLocation(world, maxRadius, minRadius, minY, maxY);
                    if (plugin.getRTPManager().isSafeLocation(loc)) {
                        safeLocation = loc;
                        break;
                    }
                }
                
                if (safeLocation == null) {
                    plugin.getRTPMessageManager().sendMessage(player, "no-safe-location");
                    return;
                }
                
                plugin.getRTPManager().setCooldown(player);
                plugin.getTeleportManager().startTeleport(player, safeLocation, "rtp");
                
                break;
            }
        }
    }
              }

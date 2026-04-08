package me.aris.core.managers;

import me.aris.core.ArisCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellManager implements Listener {
    private ArisCore plugin;
    private FileConfiguration sellConfig;
    private Map<Material, Double> prices;
    private String axeName;
    private String axeMaterial;
    private boolean axeEnabled;
    private List<String> axeLore;
    
    public SellManager(ArisCore plugin) {
        this.plugin = plugin;
        this.prices = new HashMap<>();
        loadConfigs();
    }
    
    private void loadConfigs() {
        File configFile = new File(plugin.getDataFolder(), "Sell/config.yml");
        if (configFile.exists()) {
            sellConfig = YamlConfiguration.loadConfiguration(configFile);
        } else {
            sellConfig = new YamlConfiguration();
        }
        
        File pricesFile = new File(plugin.getDataFolder(), "Sell/prices.yml");
        if (pricesFile.exists()) {
            FileConfiguration pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);
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
        
        axeEnabled = sellConfig.getBoolean("axe-sell.enabled", true);
        axeMaterial = sellConfig.getString("axe-sell.item", "DIAMOND_AXE");
        axeName = ChatColor.translateAlternateColorCodes('&', sellConfig.getString("axe-sell.name", "&6&lAxeSell"));
        axeLore = sellConfig.getStringList("axe-sell.lore");
    }
    
    public boolean isAxeEnabled() {
        return axeEnabled;
    }
    
    public String getAxeMaterial() {
        return axeMaterial;
    }
    
    public String getAxeName() {
        return axeName;
    }
    
    public List<String> getAxeLore() {
        return axeLore;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!axeEnabled) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        
        if (!item.getItemMeta().getDisplayName().equals(axeName)) return;
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null) {
                org.bukkit.block.Block block = event.getClickedBlock();
                if (block.getState() instanceof org.bukkit.block.Chest) {
                    event.setCancelled(true);
                    sellChestContents(player, (org.bukkit.block.Chest) block.getState());
                } else {
                    plugin.getMessageManager().sendMessage(player, "chest-only", "sell");
                }
            }
        }
    }
    
    private void sellChestContents(Player player, org.bukkit.block.Chest chest) {
        Inventory chestInv = chest.getInventory();
        Map<Material, Integer> itemsToSell = new HashMap<>();
        double totalPrice = 0;
        int totalItems = 0;
        
        for (ItemStack item : chestInv.getContents()) {
            if (item != null && !item.getType().isAir()) {
                double price = prices.getOrDefault(item.getType(), 0.0);
                if (price > 0) {
                    int amount = item.getAmount();
                    itemsToSell.merge(item.getType(), amount, Integer::sum);
                    totalPrice += price * amount;
                    totalItems += amount;
                }
            }
        }
        
        if (totalItems == 0) {
            plugin.getMessageManager().sendMessage(player, "no-items", "sell");
            return;
        }
        
        for (Map.Entry<Material, Integer> entry : itemsToSell.entrySet()) {
            ItemStack toRemove = new ItemStack(entry.getKey(), entry.getValue());
            chestInv.removeItem(toRemove);
        }
        
        plugin.getShardsManager().addShards(player, (long) totalPrice);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("total_items", String.valueOf(totalItems));
        placeholders.put("total_price", String.valueOf((long) totalPrice));
        plugin.getMessageManager().sendMessage(player, "sold-all", "sell", placeholders);
    }
}

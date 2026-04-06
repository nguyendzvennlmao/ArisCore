package me.aris.core.gui;

import me.aris.core.ArisCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

public class ShopGUI implements Listener {
    private ArisCore plugin;
    private FileConfiguration mainConfig;
    private Map<String, FileConfiguration> categoryConfigs;
    
    public ShopGUI(ArisCore plugin) {
        this.plugin = plugin;
        this.categoryConfigs = new HashMap<>();
        loadConfigs();
    }
    
    private void loadConfigs() {
        File mainFile = new File(plugin.getDataFolder(), "Shop/gui/main.yml");
        if (mainFile.exists()) {
            mainConfig = YamlConfiguration.loadConfiguration(mainFile);
        } else {
            mainConfig = new YamlConfiguration();
        }
        
        String[] categories = {"end", "nether", "gear", "food"};
        for (String category : categories) {
            File file = new File(plugin.getDataFolder(), "Shop/gui/" + category + ".yml");
            if (file.exists()) {
                categoryConfigs.put(category, YamlConfiguration.loadConfiguration(file));
            }
        }
    }
    
    private String translateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public void openMainShop(Player player) {
        loadConfigs();
        String title = mainConfig.getString("title", "&8ѕʜᴏᴘ");
        int rows = mainConfig.getInt("rows", 3);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateColors(title));
        
        for (String category : mainConfig.getConfigurationSection("categories").getKeys(false)) {
            int slot = mainConfig.getInt("categories." + category + ".slot");
            String materialName = mainConfig.getString("categories." + category + ".material");
            String displayName = mainConfig.getString("categories." + category + ".displayname");
            List<String> lore = mainConfig.getStringList("categories." + category + ".lore");
            
            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                material = Material.CHEST;
            }
            
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(translateColors(displayName));
            if (lore != null) {
                meta.setLore(lore.stream().map(this::translateColors).toList());
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
        String mainTitle = translateColors(mainConfig.getString("title", "&8ѕʜᴏᴘ"));
        
        if (title.equals(mainTitle)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            
            String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            
            for (String category : mainConfig.getConfigurationSection("categories").getKeys(false)) {
                String catName = ChatColor.stripColor(translateColors(mainConfig.getString("categories." + category + ".displayname")));
                if (displayName.equals(catName)) {
                    openCategoryShop(player, category);
                    return;
                }
            }
            return;
        }
        
        for (Map.Entry<String, FileConfiguration> entry : categoryConfigs.entrySet()) {
            String categoryTitle = translateColors(entry.getValue().getString("title", ""));
            if (title.equals(categoryTitle)) {
                event.setCancelled(true);
                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || !clicked.hasItemMeta()) return;
                
                for (String itemKey : entry.getValue().getConfigurationSection("items").getKeys(false)) {
                    int slot = entry.getValue().getInt("items." + itemKey + ".slot");
                    if (event.getSlot() == slot) {
                        long price = entry.getValue().getLong("items." + itemKey + ".price");
                        int amount = entry.getValue().getInt("items." + itemKey + ".amount", 1);
                        String materialName = entry.getValue().getString("items." + itemKey + ".material");
                        
                        if (!plugin.getShardsManager().hasEnough(player, price)) {
                            plugin.getMessageManager().sendMessage(player, "insufficient-funds", "shop");
                            player.closeInventory();
                            return;
                        }
                        
                        if (player.getInventory().firstEmpty() == -1) {
                            plugin.getMessageManager().sendMessage(player, "inventory-full", "shop");
                            player.closeInventory();
                            return;
                        }
                        
                        if (plugin.getShardsManager().removeShards(player, price)) {
                            Material material = Material.valueOf(materialName);
                            ItemStack item = new ItemStack(material, amount);
                            player.getInventory().addItem(item);
                            player.closeInventory();
                        }
                        return;
                    }
                }
                
                if (event.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
                    openMainShop(player);
                }
                return;
            }
        }
    }
    
    private void openCategoryShop(Player player, String category) {
        FileConfiguration catConfig = categoryConfigs.get(category);
        if (catConfig == null) return;
        
        String title = catConfig.getString("title", "&8Shop");
        int rows = catConfig.getInt("rows", 3);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateColors(title));
        
        for (String itemKey : catConfig.getConfigurationSection("items").getKeys(false)) {
            int slot = catConfig.getInt("items." + itemKey + ".slot");
            String materialName = catConfig.getString("items." + itemKey + ".material");
            String displayName = catConfig.getString("items." + itemKey + ".displayname");
            long price = catConfig.getLong("items." + itemKey + ".price");
            List<String> lore = catConfig.getStringList("items." + itemKey + ".lore");
            
            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                material = Material.CHEST;
            }
            
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(translateColors(displayName));
            
            List<String> coloredLore = lore.stream()
                .map(line -> line.replace("%price%", String.valueOf(price)))
                .map(this::translateColors)
                .toList();
            meta.setLore(coloredLore);
            item.setItemMeta(meta);
            gui.setItem(slot, item);
        }
        
        ItemStack backButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(translateColors("&#e00202ʙᴀᴄᴋ"));
        backButton.setItemMeta(backMeta);
        gui.setItem(22, backButton);
        
        player.openInventory(gui);
    }
                  }

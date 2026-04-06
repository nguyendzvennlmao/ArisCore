package me.aris.core.gui;

import me.aris.core.ArisCore;
import me.aris.core.models.ShopItem;
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
    private FileConfiguration shopConfig;
    private Map<String, FileConfiguration> categoryConfigs;
    private QuantitySelectorGUI quantitySelector;
    private PurchaseHandler purchaseHandler;
    
    public ShopGUI(ArisCore plugin) {
        this.plugin = plugin;
        this.categoryConfigs = new HashMap<>();
        this.purchaseHandler = new PurchaseHandler(plugin);
        this.quantitySelector = new QuantitySelectorGUI(plugin, purchaseHandler);
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
    
    private String translateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public void openMainShop(Player player) {
        loadConfigs();
        String title = shopConfig.getString("main-menu.title", "&8ѕʜᴏᴘ");
        int rows = shopConfig.getInt("main-menu.rows", 3);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateColors(title));
        
        for (String category : shopConfig.getConfigurationSection("main-menu.categories").getKeys(false)) {
            int slot = shopConfig.getInt("main-menu.categories." + category + ".slot");
            String materialName = shopConfig.getString("main-menu.categories." + category + ".material");
            String displayName = shopConfig.getString("main-menu.categories." + category + ".displayname");
            List<String> lore = shopConfig.getStringList("main-menu.categories." + category + ".lore");
            
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
        String mainTitle = translateColors(shopConfig.getString("main-menu.title", "&8ѕʜᴏᴘ"));
        String quantityTitle = translateColors(shopConfig.getString("gui.quantity-selector.title", "&8ᴄᴏɴғɪʀᴍ ᴘᴜʀᴄʜᴀsᴇ"));
        
        if (title.equals(mainTitle)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            
            String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            
            for (String category : shopConfig.getConfigurationSection("main-menu.categories").getKeys(false)) {
                String catName = ChatColor.stripColor(translateColors(shopConfig.getString("main-menu.categories." + category + ".displayname")));
                if (displayName.equals(catName)) {
                    openCategoryShop(player, category);
                    return;
                }
            }
            return;
        }
        
        if (title.equals(quantityTitle)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            
            ShopItem pending = purchaseHandler.getPendingPurchase(player);
            if (pending == null) {
                player.closeInventory();
                return;
            }
            
            String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            
            String remove64Name = ChatColor.stripColor(translateColors(shopConfig.getString("gui.quantity-selector.remove64.name", "&cĐặt 1")));
            String remove10Name = ChatColor.stripColor(translateColors(shopConfig.getString("gui.quantity-selector.remove10.name", "&c-10")));
            String remove1Name = ChatColor.stripColor(translateColors(shopConfig.getString("gui.quantity-selector.remove1.name", "&c-1")));
            String add1Name = ChatColor.stripColor(translateColors(shopConfig.getString("gui.quantity-selector.add1.name", "&a+1")));
            String add10Name = ChatColor.stripColor(translateColors(shopConfig.getString("gui.quantity-selector.add10.name", "&a+10")));
            String set64Name = ChatColor.stripColor(translateColors(shopConfig.getString("gui.quantity-selector.set64.name", "&aĐặt 64")));
            String cancelName = ChatColor.stripColor(translateColors(shopConfig.getString("gui.quantity-selector.cancel.name", "&#e00202ᴄᴀɴᴄᴇʟ")));
            String confirmName = ChatColor.stripColor(translateColors(shopConfig.getString("gui.quantity-selector.confirm.name", "&#02de4fᴄᴏɴғɪʀᴍ")));
            
            if (displayName.equals(remove64Name)) {
                pending.setAmount(1);
                quantitySelector.refresh(player, pending);
            } else if (displayName.equals(remove10Name)) {
                pending.setAmount(Math.max(1, pending.getAmount() - 10));
                quantitySelector.refresh(player, pending);
            } else if (displayName.equals(remove1Name)) {
                pending.setAmount(Math.max(1, pending.getAmount() - 1));
                quantitySelector.refresh(player, pending);
            } else if (displayName.equals(add1Name)) {
                pending.setAmount(Math.min(6400, pending.getAmount() + 1));
                quantitySelector.refresh(player, pending);
            } else if (displayName.equals(add10Name)) {
                pending.setAmount(Math.min(6400, pending.getAmount() + 10));
                quantitySelector.refresh(player, pending);
            } else if (displayName.equals(set64Name)) {
                pending.setAmount(64);
                quantitySelector.refresh(player, pending);
            } else if (displayName.equals(cancelName)) {
                purchaseHandler.removePendingPurchase(player);
                player.closeInventory();
                openMainShop(player);
            } else if (displayName.equals(confirmName)) {
                purchaseHandler.processPurchase(player, pending);
                purchaseHandler.removePendingPurchase(player);
                player.closeInventory();
                openMainShop(player);
            }
            return;
        }
        
        for (Map.Entry<String, FileConfiguration> entry : categoryConfigs.entrySet()) {
            String categoryTitle = translateColors(entry.getValue().getString("title", ""));
            if (title.equals(categoryTitle)) {
                event.setCancelled(true);
                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || !clicked.hasItemMeta()) return;
                
                if (clicked.getType() == Material.RED_STAINED_GLASS_PANE) {
                    openMainShop(player);
                    return;
                }
                
                for (String itemKey : entry.getValue().getConfigurationSection("items").getKeys(false)) {
                    int slot = entry.getValue().getInt("items." + itemKey + ".slot");
                    if (event.getSlot() == slot) {
                        long price = entry.getValue().getLong("items." + itemKey + ".price");
                        int amount = entry.getValue().getInt("items." + itemKey + ".amount", 1);
                        String materialName = entry.getValue().getString("items." + itemKey + ".material");
                        String displayName = entry.getValue().getString("items." + itemKey + ".displayname");
                        String command = entry.getValue().getString("items." + itemKey + ".command", "");
                        
                        ShopItem shopItem = new ShopItem(entry.getKey(), itemKey, price, materialName, displayName, command, amount);
                        quantitySelector.open(player, shopItem);
                        return;
                    }
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

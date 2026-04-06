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
    private FileConfiguration shopConfig;
    private Map<String, FileConfiguration> categoryConfigs;
    private Map<Player, PendingPurchase> pendingPurchases;
    
    public ShopGUI(ArisCore plugin) {
        this.plugin = plugin;
        this.categoryConfigs = new HashMap<>();
        this.pendingPurchases = new HashMap<>();
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
    
    private void openQuantitySelector(Player player, String category, String itemKey, long price, String materialName, String displayName, String command, int defaultAmount) {
        FileConfiguration quantityConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "Shop/config.yml"));
        
        String title = quantityConfig.getString("gui.quantity-selector.title", "&8ᴄᴏɴғɪʀᴍ ᴘᴜʀᴄʜᴀsᴇ");
        int rows = quantityConfig.getInt("gui.quantity-selector.rows", 3);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateColors(title));
        
        pendingPurchases.put(player, new PendingPurchase(category, itemKey, price, materialName, displayName, command, defaultAmount));
        
        int confirmSlot = quantityConfig.getInt("gui.quantity-selector.confirm-button.slot", 15);
        String confirmMaterial = quantityConfig.getString("gui.quantity-selector.confirm-button.material", "LIME_STAINED_GLASS_PANE");
        String confirmName = quantityConfig.getString("gui.quantity-selector.confirm-button.displayname", "&aᴄᴏɴғɪʀᴍ");
        List<String> confirmLore = quantityConfig.getStringList("gui.quantity-selector.confirm-button.lore");
        
        int cancelSlot = quantityConfig.getInt("gui.quantity-selector.cancel-button.slot", 11);
        String cancelMaterial = quantityConfig.getString("gui.quantity-selector.cancel-button.material", "RED_STAINED_GLASS_PANE");
        String cancelName = quantityConfig.getString("gui.quantity-selector.cancel-button.displayname", "&cᴄᴀɴᴄᴇʟ");
        List<String> cancelLore = quantityConfig.getStringList("gui.quantity-selector.cancel-button.lore");
        
        int previewSlot = quantityConfig.getInt("gui.quantity-selector.item-preview.slot", 13);
        List<String> previewLore = quantityConfig.getStringList("gui.quantity-selector.item-preview.lore");
        
        Material confirmMat;
        try {
            confirmMat = Material.valueOf(confirmMaterial);
        } catch (IllegalArgumentException e) {
            confirmMat = Material.LIME_STAINED_GLASS_PANE;
        }
        
        ItemStack confirmButton = new ItemStack(confirmMat);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.setDisplayName(translateColors(confirmName));
        List<String> coloredConfirmLore = confirmLore.stream()
            .map(line -> line.replace("%total_price%", String.valueOf(price * defaultAmount)).replace("%amount%", String.valueOf(defaultAmount)))
            .map(this::translateColors).toList();
        confirmMeta.setLore(coloredConfirmLore);
        confirmButton.setItemMeta(confirmMeta);
        gui.setItem(confirmSlot, confirmButton);
        
        Material cancelMat;
        try {
            cancelMat = Material.valueOf(cancelMaterial);
        } catch (IllegalArgumentException e) {
            cancelMat = Material.RED_STAINED_GLASS_PANE;
        }
        
        ItemStack cancelButton = new ItemStack(cancelMat);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName(translateColors(cancelName));
        if (cancelLore != null && !cancelLore.isEmpty()) {
            cancelMeta.setLore(cancelLore.stream().map(this::translateColors).toList());
        }
        cancelButton.setItemMeta(cancelMeta);
        gui.setItem(cancelSlot, cancelButton);
        
        Material previewMat;
        try {
            previewMat = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            previewMat = Material.CHEST;
        }
        
        ItemStack previewItem = new ItemStack(previewMat);
        ItemMeta previewMeta = previewItem.getItemMeta();
        previewMeta.setDisplayName(translateColors(displayName));
        List<String> coloredPreviewLore = previewLore.stream()
            .map(line -> line.replace("%price%", String.valueOf(price)).replace("%amount%", String.valueOf(defaultAmount)))
            .map(this::translateColors).toList();
        previewMeta.setLore(coloredPreviewLore);
        previewItem.setItemMeta(previewMeta);
        gui.setItem(previewSlot, previewItem);
        
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
            
            String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            String confirmName = ChatColor.stripColor(translateColors(shopConfig.getString("gui.quantity-selector.confirm-button.displayname", "&aᴄᴏɴғɪʀᴍ")));
            String cancelName = ChatColor.stripColor(translateColors(shopConfig.getString("gui.quantity-selector.cancel-button.displayname", "&cᴄᴀɴᴄᴇʟ")));
            
            if (displayName.equals(confirmName)) {
                PendingPurchase pending = pendingPurchases.remove(player);
                if (pending != null) {
                    processPurchase(player, pending);
                }
                player.closeInventory();
                openMainShop(player);
            } else if (displayName.equals(cancelName)) {
                pendingPurchases.remove(player);
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
                        
                        openQuantitySelector(player, entry.getKey(), itemKey, price, materialName, displayName, command, amount);
                        return;
                    }
                }
                return;
            }
        }
    }
    
    private void processPurchase(Player player, PendingPurchase pending) {
        long totalPrice = pending.price * pending.amount;
        
        if (!plugin.getShardsManager().hasEnough(player, totalPrice)) {
            plugin.getMessageManager().sendMessage(player, "insufficient-funds", "shop");
            return;
        }
        
        if (player.getInventory().firstEmpty() == -1 && pending.command.isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "inventory-full", "shop");
            return;
        }
        
        if (plugin.getShardsManager().removeShards(player, totalPrice)) {
            if (!pending.command.isEmpty()) {
                String finalCommand = pending.command.replace("%player%", player.getName()).replace("%amount%", String.valueOf(pending.amount));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            } else {
                Material material;
                try {
                    material = Material.valueOf(pending.materialName);
                } catch (IllegalArgumentException e) {
                    material = Material.CHEST;
                }
                ItemStack item = new ItemStack(material, pending.amount);
                player.getInventory().addItem(item);
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
    
    private static class PendingPurchase {
        String category;
        String itemKey;
        long price;
        String materialName;
        String displayName;
        String command;
        int amount;
        
        PendingPurchase(String category, String itemKey, long price, String materialName, String displayName, String command, int amount) {
            this.category = category;
            this.itemKey = itemKey;
            this.price = price;
            this.materialName = materialName;
            this.displayName = displayName;
            this.command = command;
            this.amount = amount;
        }
    }
                }

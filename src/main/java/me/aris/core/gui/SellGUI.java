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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellGUI implements Listener {
    private ArisCore plugin;
    private FileConfiguration sellConfig;
    private FileConfiguration guiConfig;
    private Map<Material, Double> prices;
    
    public SellGUI(ArisCore plugin) {
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
        
        File guiFile = new File(plugin.getDataFolder(), "Sell/gui.yml");
        if (guiFile.exists()) {
            guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        } else {
            guiConfig = new YamlConfiguration();
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
    }
    
    private String translateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    private String formatMaterialName(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return result.toString().trim();
    }
    
    private String formatPrice(double price) {
        if (price >= 1000000) {
            return String.format("%.1fM", price / 1000000);
        } else if (price >= 1000) {
            return String.format("%.1fK", price / 1000);
        }
        return String.format("%.0f", price);
    }
    
    public void openSellGUI(Player player) {
        String title = guiConfig.getString("title", "&8Sell Vật phẩm giá ít ăn nhiều");
        int rows = guiConfig.getInt("rows", 6);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateColors(title));
        
        Map<Material, Integer> itemsToSell = new HashMap<>();
        double totalPrice = 0;
        int totalItems = 0;
        
        for (ItemStack item : player.getInventory().getContents()) {
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
        
        String itemNameTemplate = guiConfig.getString("item-display.name", "&6%item%");
        List<String> itemLoreTemplate = guiConfig.getStringList("item-display.lore");
        
        int slot = 0;
        for (Map.Entry<Material, Integer> entry : itemsToSell.entrySet()) {
            if (slot >= rows * 9 - 9) break;
            
            Material material = entry.getKey();
            double price = prices.getOrDefault(material, 0.0);
            int amount = entry.getValue();
            double itemTotal = price * amount;
            
            ItemStack display = new ItemStack(material, Math.min(amount, 64));
            ItemMeta meta = display.getItemMeta();
            
            String name = itemNameTemplate.replace("%item%", formatMaterialName(material.name()));
            meta.setDisplayName(translateColors(name));
            
            List<String> coloredLore = new ArrayList<>();
            for (String line : itemLoreTemplate) {
                String coloredLine = line.replace("%amount%", String.valueOf(amount))
                                       .replace("%price%", formatPrice(price))
                                       .replace("%total_price%", formatPrice(itemTotal));
                coloredLore.add(translateColors(coloredLine));
            }
            meta.setLore(coloredLore);
            display.setItemMeta(meta);
            gui.setItem(slot, display);
            slot++;
        }
        
        int sellButtonSlot = guiConfig.getInt("sell-all-button.slot", 49);
        String sellButtonMaterial = guiConfig.getString("sell-all-button.material", "LIME_STAINED_GLASS_PANE");
        String sellButtonName = guiConfig.getString("sell-all-button.name", "&a&lBÁN TẤT CẢ");
        List<String> sellButtonLore = guiConfig.getStringList("sell-all-button.lore");
        
        Material sellMat;
        try {
            sellMat = Material.valueOf(sellButtonMaterial);
        } catch (IllegalArgumentException e) {
            sellMat = Material.LIME_STAINED_GLASS_PANE;
        }
        
        ItemStack sellButton = new ItemStack(sellMat);
        ItemMeta sellMeta = sellButton.getItemMeta();
        sellMeta.setDisplayName(translateColors(sellButtonName));
        
        List<String> coloredSellLore = new ArrayList<>();
        for (String line : sellButtonLore) {
            String coloredLine = line.replace("%total_items%", String.valueOf(totalItems))
                                   .replace("%total_price%", formatPrice(totalPrice));
            coloredSellLore.add(translateColors(coloredLine));
        }
        sellMeta.setLore(coloredSellLore);
        sellButton.setItemMeta(sellMeta);
        gui.setItem(sellButtonSlot, sellButton);
        
        int closeButtonSlot = guiConfig.getInt("close-button.slot", 50);
        String closeButtonMaterial = guiConfig.getString("close-button.material", "BARRIER");
        String closeButtonName = guiConfig.getString("close-button.name", "&c&lĐÓNG");
        List<String> closeButtonLore = guiConfig.getStringList("close-button.lore");
        
        Material closeMat;
        try {
            closeMat = Material.valueOf(closeButtonMaterial);
        } catch (IllegalArgumentException e) {
            closeMat = Material.BARRIER;
        }
        
        ItemStack closeButton = new ItemStack(closeMat);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName(translateColors(closeButtonName));
        if (closeButtonLore != null && !closeButtonLore.isEmpty()) {
            List<String> coloredCloseLore = new ArrayList<>();
            for (String line : closeButtonLore) {
                coloredCloseLore.add(translateColors(line));
            }
            closeMeta.setLore(coloredCloseLore);
        }
        closeButton.setItemMeta(closeMeta);
        gui.setItem(closeButtonSlot, closeButton);
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        String guiTitle = translateColors(guiConfig.getString("title", "&8Sell Vật phẩm giá ít ăn nhiều"));
        
        if (!title.equals(guiTitle)) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        String sellButtonName = ChatColor.stripColor(translateColors(guiConfig.getString("sell-all-button.name", "&a&lBÁN TẤT CẢ")));
        String closeButtonName = ChatColor.stripColor(translateColors(guiConfig.getString("close-button.name", "&c&lĐÓNG")));
        
        if (displayName.equals(sellButtonName)) {
            sellAllItems(player);
            player.closeInventory();
        } else if (displayName.equals(closeButtonName)) {
            player.closeInventory();
        }
    }
    
    private void sellAllItems(Player player) {
        Map<Material, Integer> itemsToSell = new HashMap<>();
        double totalPrice = 0;
        int totalItems = 0;
        
        for (ItemStack item : player.getInventory().getContents()) {
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
            player.getInventory().removeItem(toRemove);
        }
        
        plugin.getShardsManager().addShards(player, (long) totalPrice);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("total_items", String.valueOf(totalItems));
        placeholders.put("total_price", formatPrice(totalPrice));
        plugin.getMessageManager().sendMessage(player, "sold-all", "sell", placeholders);
    }
                }

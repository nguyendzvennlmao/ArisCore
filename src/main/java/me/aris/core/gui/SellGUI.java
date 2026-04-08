package me.aris.core.gui;

import me.aris.core.ArisCore;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
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
    private FileConfiguration pricesConfig;
    private Map<Material, Double> prices;
    private Economy economy;
    private Map<Player, Inventory> openSellGUIs;
    
    public SellGUI(ArisCore plugin) {
        this.plugin = plugin;
        this.prices = new HashMap<>();
        this.openSellGUIs = new HashMap<>();
        loadConfigs();
        setupEconomy();
    }
    
    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            org.bukkit.plugin.RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                plugin.getLogger().info("Vault economy hooked for Sell!");
            }
        }
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
            pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);
            loadPrices();
        }
    }
    
    private void loadPrices() {
        prices.clear();
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
    
    private String translateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    private String formatPrice(double price) {
        if (price >= 1000000) {
            return String.format("%.1fM", price / 1000000);
        } else if (price >= 1000) {
            return String.format("%.1fK", price / 1000);
        }
        return String.format("%.0f", price);
    }
    
    private double calculateTotalPrice(Inventory gui) {
        double total = 0;
        for (int i = 0; i < gui.getSize(); i++) {
            ItemStack item = gui.getItem(i);
            if (item != null && !item.getType().isAir()) {
                double price = prices.getOrDefault(item.getType(), 0.0);
                if (price > 0) {
                    total += price * item.getAmount();
                }
            }
        }
        return total;
    }
    
    private void updateSellButton(Inventory gui) {
        double totalPrice = calculateTotalPrice(gui);
        
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
            String coloredLine = line.replace("%total_price%", formatPrice(totalPrice));
            coloredSellLore.add(translateColors(coloredLine));
        }
        sellMeta.setLore(coloredSellLore);
        sellButton.setItemMeta(sellMeta);
        gui.setItem(sellButtonSlot, sellButton);
    }
    
    private void returnItemsToPlayer(Player player, Inventory gui) {
        for (int i = 0; i < gui.getSize(); i++) {
            if (i == 49 || i == 50) continue;
            ItemStack item = gui.getItem(i);
            if (item != null && !item.getType().isAir()) {
                player.getInventory().addItem(item.clone());
            }
        }
    }
    
    public void openSellGUI(Player player) {
        String title = guiConfig.getString("title", "&8Sell Vật phẩm giá ít ăn nhiều");
        int rows = guiConfig.getInt("rows", 6);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateColors(title));
        
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
            String coloredLine = line.replace("%total_price%", "0");
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
        
        openSellGUIs.put(player, gui);
        player.openInventory(gui);
        
        updateSellButton(gui);
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Inventory gui = event.getInventory();
        String title = event.getView().getTitle();
        String guiTitle = translateColors(guiConfig.getString("title", "&8Sell Vật phẩm giá ít ăn nhiều"));
        
        if (title.equals(guiTitle)) {
            returnItemsToPlayer(player, gui);
            openSellGUIs.remove(player);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        String guiTitle = translateColors(guiConfig.getString("title", "&8Sell Vật phẩm giá ít ăn nhiều"));
        
        if (!title.equals(guiTitle)) return;
        
        int sellButtonSlot = guiConfig.getInt("sell-all-button.slot", 49);
        int closeButtonSlot = guiConfig.getInt("close-button.slot", 50);
        
        if (event.getSlot() == sellButtonSlot || event.getSlot() == closeButtonSlot) {
            event.setCancelled(true);
            
            if (event.getSlot() == sellButtonSlot) {
                sellItemsInGUI(player, event.getInventory());
                player.closeInventory();
            } else if (event.getSlot() == closeButtonSlot) {
                player.closeInventory();
            }
            return;
        }
        
        if (event.getSlot() != sellButtonSlot && event.getSlot() != closeButtonSlot) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                updateSellButton(event.getInventory());
            }, 1L);
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        String guiTitle = translateColors(guiConfig.getString("title", "&8Sell Vật phẩm giá ít ăn nhiều"));
        
        if (!title.equals(guiTitle)) return;
        
        int sellButtonSlot = guiConfig.getInt("sell-all-button.slot", 49);
        int closeButtonSlot = guiConfig.getInt("close-button.slot", 50);
        
        for (Integer slot : event.getRawSlots()) {
            if (slot == sellButtonSlot || slot == closeButtonSlot) {
                event.setCancelled(true);
                return;
            }
        }
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            updateSellButton(event.getInventory());
        }, 1L);
    }
    
    private void sellItemsInGUI(Player player, Inventory gui) {
        if (economy == null) {
            player.sendMessage(ChatColor.RED + "Economy system not found!");
            returnItemsToPlayer(player, gui);
            return;
        }
        
        Map<Material, Integer> itemsToSell = new HashMap<>();
        double totalPrice = 0;
        int totalItems = 0;
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (i == 49 || i == 50) continue;
            
            ItemStack item = gui.getItem(i);
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
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (i == 49 || i == 50) continue;
            gui.setItem(i, null);
        }
        
        economy.depositPlayer(player, totalPrice);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("total_items", String.valueOf(totalItems));
        placeholders.put("total_price", formatPrice(totalPrice));
        
        boolean chatEnabled = sellConfig.getBoolean("messages.chat", true);
        boolean actionBarEnabled = sellConfig.getBoolean("messages.action-bar", true);
        
        String prefix = "&8[&6Sell&8] &r";
        String chatMsg = "&aBạn đã bán &e%total_items% vật phẩm &avới tổng giá &6%total_price%&a!";
        String actionMsg = "&aĐã bán %total_items% vật phẩm với giá %total_price%";
        
        chatMsg = chatMsg.replace("%total_items%", placeholders.get("total_items")).replace("%total_price%", placeholders.get("total_price"));
        actionMsg = actionMsg.replace("%total_items%", placeholders.get("total_items")).replace("%total_price%", placeholders.get("total_price"));
        
        if (chatEnabled) {
            player.sendMessage(translateColors(prefix + chatMsg));
        }
        
        if (actionBarEnabled) {
            player.sendActionBar(translateColors(actionMsg));
        }
    }
            }

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
        String title = shopConfig.getString("gui.quantity-selector.title", "&8ᴄᴏɴғɪʀᴍ ᴘᴜʀᴄʜᴀsᴇ");
        int rows = shopConfig.getInt("gui.quantity-selector.rows", 3);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateColors(title));
        
        PendingPurchase pending = new PendingPurchase(category, itemKey, price, materialName, displayName, command, defaultAmount);
        pendingPurchases.put(player, pending);
        
        int remove64Slot = shopConfig.getInt("gui.quantity-selector.remove64.slot", 9);
        String remove64Name = shopConfig.getString("gui.quantity-selector.remove64.name", "&cĐặt 1");
        String remove64Material = shopConfig.getString("gui.quantity-selector.remove64.material", "RED_STAINED_GLASS_PANE");
        
        int remove10Slot = shopConfig.getInt("gui.quantity-selector.remove10.slot", 10);
        String remove10Name = shopConfig.getString("gui.quantity-selector.remove10.name", "&c-10");
        String remove10Material = shopConfig.getString("gui.quantity-selector.remove10.material", "RED_STAINED_GLASS_PANE");
        
        int remove1Slot = shopConfig.getInt("gui.quantity-selector.remove1.slot", 11);
        String remove1Name = shopConfig.getString("gui.quantity-selector.remove1.name", "&c-1");
        String remove1Material = shopConfig.getString("gui.quantity-selector.remove1.material", "RED_STAINED_GLASS_PANE");
        
        int add1Slot = shopConfig.getInt("gui.quantity-selector.add1.slot", 15);
        String add1Name = shopConfig.getString("gui.quantity-selector.add1.name", "&a+1");
        String add1Material = shopConfig.getString("gui.quantity-selector.add1.material", "LIME_STAINED_GLASS_PANE");
        
        int add10Slot = shopConfig.getInt("gui.quantity-selector.add10.slot", 16);
        String add10Name = shopConfig.getString("gui.quantity-selector.add10.name", "&a+10");
        String add10Material = shopConfig.getString("gui.quantity-selector.add10.material", "LIME_STAINED_GLASS_PANE");
        
        int set64Slot = shopConfig.getInt("gui.quantity-selector.set64.slot", 17);
        String set64Name = shopConfig.getString("gui.quantity-selector.set64.name", "&aĐặt 64");
        String set64Material = shopConfig.getString("gui.quantity-selector.set64.material", "LIME_STAINED_GLASS_PANE");
        
        int cancelSlot = shopConfig.getInt("gui.quantity-selector.cancel.slot", 21);
        String cancelName = shopConfig.getString("gui.quantity-selector.cancel.name", "&#e00202ᴄᴀɴᴄᴇʟ");
        String cancelMaterial = shopConfig.getString("gui.quantity-selector.cancel.material", "RED_STAINED_GLASS_PANE");
        
        int confirmSlot = shopConfig.getInt("gui.quantity-selector.confirm.slot", 23);
        String confirmName = shopConfig.getString("gui.quantity-selector.confirm.name", "&#02de4fᴄᴏɴғɪʀᴍ");
        String confirmMaterial = shopConfig.getString("gui.quantity-selector.confirm.material", "LIME_STAINED_GLASS_PANE");
        
        int previewSlot = shopConfig.getInt("gui.quantity-selector.item-preview.slot", 13);
        List<String> previewLore = shopConfig.getStringList("gui.quantity-selector.item-preview.lore");
        
        addButton(gui, remove64Slot, remove64Material, remove64Name);
        addButton(gui, remove10Slot, remove10Material, remove10Name);
        addButton(gui, remove1Slot, remove1Material, remove1Name);
        addButton(gui, add1Slot, add1Material, add1Name);
        addButton(gui, add10Slot, add10Material, add10Name);
        addButton(gui, set64Slot, set64Material, set64Name);
        addButton(gui, cancelSlot, cancelMaterial, cancelName);
        addButton(gui, confirmSlot, confirmMaterial, confirmName);
        
        updatePreviewItem(gui, previewSlot, materialName, displayName, price, pending.amount, previewLore);
        
        player.openInventory(gui);
    }
    
    private void addButton(Inventory gui, int slot, String materialName, String displayName) {
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.STONE;
        }
        
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(translateColors(displayName));
        button.setItemMeta(meta);
        gui.setItem(slot, button);
    }
    
    private void updatePreviewItem(Inventory gui, int slot, String materialName, String displayName, long price, int amount, List<String> lore) {
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.CHEST;
        }
        
        ItemStack preview = new ItemStack(material);
        ItemMeta meta = preview.getItemMeta();
        meta.setDisplayName(translateColors(displayName));
        
        long totalPrice = price * amount;
        List<String> coloredLore = lore.stream()
            .map(line -> line.replace("%price%", String.valueOf(price))
                           .replace("%amount%", String.valueOf(amount))
                           .replace("%total_price%", String.valueOf(totalPrice)))
            .map(this::translateColors)
            .toList();
        meta.setLore(coloredLore);
        preview.setItemMeta(meta);
        gui.setItem(slot, preview);
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
            
            PendingPurchase pending = pendingPurchases.get(player);
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
                pending.amount = 1;
                refreshQuantitySelector(player, pending);
            } else if (displayName.equals(remove10Name)) {
                pending.amount = Math.max(1, pending.amount - 10);
                refreshQuantitySelector(player, pending);
            } else if (displayName.equals(remove1Name)) {
                pending.amount = Math.max(1, pending.amount - 1);
                refreshQuantitySelector(player, pending);
            } else if (displayName.equals(add1Name)) {
                pending.amount = Math.min(6400, pending.amount + 1);
                refreshQuantitySelector(player, pending);
            } else if (displayName.equals(add10Name)) {
                pending.amount = Math.min(6400, pending.amount + 10);
                refreshQuantitySelector(player, pending);
            } else if (displayName.equals(set64Name)) {
                pending.amount = 64;
                refreshQuantitySelector(player, pending);
            } else if (displayName.equals(cancelName)) {
                pendingPurchases.remove(player);
                player.closeInventory();
                openMainShop(player);
            } else if (displayName.equals(confirmName)) {
                processPurchase(player, pending);
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
    
    private void refreshQuantitySelector(Player player, PendingPurchase pending) {
        String title = shopConfig.getString("gui.quantity-selector.title", "&8ᴄᴏɴғɪʀᴍ ᴘᴜʀᴄʜᴀsᴇ");
        int rows = shopConfig.getInt("gui.quantity-selector.rows", 3);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateColors(title));
        
        int remove64Slot = shopConfig.getInt("gui.quantity-selector.remove64.slot", 9);
        String remove64Material = shopConfig.getString("gui.quantity-selector.remove64.material", "RED_STAINED_GLASS_PANE");
        String remove64Name = shopConfig.getString("gui.quantity-selector.remove64.name", "&cĐặt 1");
        
        int remove10Slot = shopConfig.getInt("gui.quantity-selector.remove10.slot", 10);
        String remove10Material = shopConfig.getString("gui.quantity-selector.remove10.material", "RED_STAINED_GLASS_PANE");
        String remove10Name = shopConfig.getString("gui.quantity-selector.remove10.name", "&c-10");
        
        int remove1Slot = shopConfig.getInt("gui.quantity-selector.remove1.slot", 11);
        String remove1Material = shopConfig.getString("gui.quantity-selector.remove1.material", "RED_STAINED_GLASS_PANE");
        String remove1Name = shopConfig.getString("gui.quantity-selector.remove1.name", "&c-1");
        
        int add1Slot = shopConfig.getInt("gui.quantity-selector.add1.slot", 15);
        String add1Material = shopConfig.getString("gui.quantity-selector.add1.material", "LIME_STAINED_GLASS_PANE");
        String add1Name = shopConfig.getString("gui.quantity-selector.add1.name", "&a+1");
        
        int add10Slot = shopConfig.getInt("gui.quantity-selector.add10.slot", 16);
        String add10Material = shopConfig.getString("gui.quantity-selector.add10.material", "LIME_STAINED_GLASS_PANE");
        String add10Name = shopConfig.getString("gui.quantity-selector.add10.name", "&a+10");
        
        int set64Slot = shopConfig.getInt("gui.quantity-selector.set64.slot", 17);
        String set64Material = shopConfig.getString("gui.quantity-selector.set64.material", "LIME_STAINED_GLASS_PANE");
        String set64Name = shopConfig.getString("gui.quantity-selector.set64.name", "&aĐặt 64");
        
        int cancelSlot = shopConfig.getInt("gui.quantity-selector.cancel.slot", 21);
        String cancelMaterial = shopConfig.getString("gui.quantity-selector.cancel.material", "RED_STAINED_GLASS_PANE");
        String cancelName = shopConfig.getString("gui.quantity-selector.cancel.name", "&#e00202ᴄᴀɴᴄᴇʟ");
        
        int confirmSlot = shopConfig.getInt("gui.quantity-selector.confirm.slot", 23);
        String confirmMaterial = shopConfig.getString("gui.quantity-selector.confirm.material", "LIME_STAINED_GLASS_PANE");
        String confirmName = shopConfig.getString("gui.quantity-selector.confirm.name", "&#02de4fᴄᴏɴғɪʀᴍ");
        
        int previewSlot = shopConfig.getInt("gui.quantity-selector.item-preview.slot", 13);
        List<String> previewLore = shopConfig.getStringList("gui.quantity-selector.item-preview.lore");
        
        addButton(gui, remove64Slot, remove64Material, remove64Name);
        addButton(gui, remove10Slot, remove10Material, remove10Name);
        addButton(gui, remove1Slot, remove1Material, remove1Name);
        addButton(gui, add1Slot, add1Material, add1Name);
        addButton(gui, add10Slot, add10Material, add10Name);
        addButton(gui, set64Slot, set64Material, set64Name);
        addButton(gui, cancelSlot, cancelMaterial, cancelName);
        addButton(gui, confirmSlot, confirmMaterial, confirmName);
        
        updatePreviewItem(gui, previewSlot, pending.materialName, pending.displayName, pending.price, pending.amount, previewLore);
        
        player.openInventory(gui);
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
                    material = Material.CHES

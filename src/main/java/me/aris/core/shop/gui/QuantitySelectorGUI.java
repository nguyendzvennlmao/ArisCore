package me.aris.core.shop.gui;

import me.aris.core.ArisCore;
import me.aris.core.shop.model.ShopItem;
import me.aris.core.shop.manager.PurchaseHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class QuantitySelectorGUI {
    private ArisCore plugin;
    private FileConfiguration shopConfig;
    private PurchaseHandler purchaseHandler;
    
    public QuantitySelectorGUI(ArisCore plugin, PurchaseHandler purchaseHandler, FileConfiguration shopConfig) {
        this.plugin = plugin;
        this.purchaseHandler = purchaseHandler;
        this.shopConfig = shopConfig;
    }
    
    private String translateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public void open(Player player, ShopItem item) {
        String title = shopConfig.getString("gui.quantity-selector.title", "&8ᴄᴏɴғɪʀᴍ ᴘᴜʀᴄʜᴀsᴇ");
        int rows = shopConfig.getInt("gui.quantity-selector.rows", 3);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateColors(title));
        
        purchaseHandler.setPendingPurchase(player, item);
        
        addControlButtons(gui);
        updatePreviewItem(gui, item);
        
        player.openInventory(gui);
    }
    
    private void addControlButtons(Inventory gui) {
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
        
        addButton(gui, remove64Slot, remove64Material, remove64Name);
        addButton(gui, remove10Slot, remove10Material, remove10Name);
        addButton(gui, remove1Slot, remove1Material, remove1Name);
        addButton(gui, add1Slot, add1Material, add1Name);
        addButton(gui, add10Slot, add10Material, add10Name);
        addButton(gui, set64Slot, set64Material, set64Name);
        addButton(gui, cancelSlot, cancelMaterial, cancelName);
        addButton(gui, confirmSlot, confirmMaterial, confirmName);
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
    
    private void updatePreviewItem(Inventory gui, ShopItem item) {
        int previewSlot = shopConfig.getInt("gui.quantity-selector.item-preview.slot", 13);
        List<String> previewLore = shopConfig.getStringList("gui.quantity-selector.item-preview.lore");
        
        Material material;
        try {
            material = Material.valueOf(item.getMaterialName());
        } catch (IllegalArgumentException e) {
            material = Material.CHEST;
        }
        
        ItemStack preview = new ItemStack(material);
        ItemMeta meta = preview.getItemMeta();
        meta.setDisplayName(translateColors(item.getDisplayName()));
        
        long totalPrice = item.getPrice() * item.getAmount();
        List<String> coloredLore = new ArrayList<>();
        for (String line : previewLore) {
            String coloredLine = line.replace("%price%", String.valueOf(item.getPrice()))
                                   .replace("%amount%", String.valueOf(item.getAmount()))
                                   .replace("%total_price%", String.valueOf(totalPrice));
            coloredLore.add(translateColors(coloredLine));
        }
        meta.setLore(coloredLore);
        preview.setItemMeta(meta);
        gui.setItem(previewSlot, preview);
    }
    
    public void refresh(Player player, ShopItem item) {
        String title = shopConfig.getString("gui.quantity-selector.title", "&8ᴄᴏɴғɪʀᴍ ᴘᴜʀᴄʜᴀsᴇ");
        int rows = shopConfig.getInt("gui.quantity-selector.rows", 3);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateColors(title));
        
        purchaseHandler.setPendingPurchase(player, item);
        
        addControlButtons(gui);
        updatePreviewItem(gui, item);
        
        player.openInventory(gui);
    }
                               }

package me.aris.core.gui;

import me.aris.core.ArisCore;
import me.aris.core.models.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class PurchaseHandler {
    private ArisCore plugin;
    private Map<Player, ShopItem> pendingPurchases;
    
    public PurchaseHandler(ArisCore plugin) {
        this.plugin = plugin;
        this.pendingPurchases = new HashMap<>();
    }
    
    public void setPendingPurchase(Player player, ShopItem item) {
        pendingPurchases.put(player, item);
    }
    
    public ShopItem getPendingPurchase(Player player) {
        return pendingPurchases.get(player);
    }
    
    public void removePendingPurchase(Player player) {
        pendingPurchases.remove(player);
    }
    
    public boolean processPurchase(Player player, ShopItem item) {
        long totalPrice = item.getPrice() * item.getAmount();
        
        if (!plugin.getShardsManager().hasEnough(player, totalPrice)) {
            plugin.getMessageManager().sendMessage(player, "insufficient-funds", "shop");
            return false;
        }
        
        if (player.getInventory().firstEmpty() == -1 && item.getCommand().isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "inventory-full", "shop");
            return false;
        }
        
        if (plugin.getShardsManager().removeShards(player, totalPrice)) {
            if (!item.getCommand().isEmpty()) {
                String finalCommand = item.getCommand().replace("%player%", player.getName()).replace("%amount%", String.valueOf(item.getAmount()));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            } else {
                Material material;
                try {
                    material = Material.valueOf(item.getMaterialName());
                } catch (IllegalArgumentException e) {
                    material = Material.CHEST;
                }
                ItemStack stack = new ItemStack(material, item.getAmount());
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(stack);
                if (!remaining.isEmpty()) {
                    player.getWorld().dropItem(player.getLocation(), remaining.get(0));
                }
            }
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.valueOf(item.getAmount()));
            placeholders.put("item", item.getDisplayName());
            placeholders.put("price", String.valueOf(totalPrice));
            plugin.getMessageManager().sendMessage(player, "purchased", "shop", placeholders);
            return true;
        }
        return false;
    }
                                   }

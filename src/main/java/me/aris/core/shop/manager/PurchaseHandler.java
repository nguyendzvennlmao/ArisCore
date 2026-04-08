package me.aris.core.shop.manager;

import me.aris.core.ArisCore;
import me.aris.core.shop.model.ShopItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PurchaseHandler {
    private ArisCore plugin;
    private Economy economy;
    private Map<Player, ShopItem> pendingPurchases;
    private FileConfiguration shopConfig;
    
    public PurchaseHandler(ArisCore plugin) {
        this.plugin = plugin;
        this.pendingPurchases = new HashMap<>();
        
        File configFile = new File(plugin.getDataFolder(), "shop/config.yml");
        if (configFile.exists()) {
            shopConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(configFile);
        } else {
            shopConfig = new org.bukkit.configuration.file.YamlConfiguration();
        }
        
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            org.bukkit.plugin.RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                plugin.getLogger().info("Vault economy hooked for Shop!");
            }
        }
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
    
    private String getTakeCommand(String currencyType) {
        return shopConfig.getString("currencies." + currencyType.toLowerCase() + ".take-command", "");
    }
    
    private String getPlaceCommand(String currencyType) {
        return shopConfig.getString("currencies." + currencyType.toLowerCase() + ".place-command", "");
    }
    
    public boolean processPurchase(Player player, ShopItem item) {
        String currencyType = item.getCurrency();
        long totalPrice = item.getPrice() * item.getAmount();
        
        if (currencyType.equalsIgnoreCase("SHARDS")) {
            if (!plugin.getShardsManager().hasEnough(player, totalPrice)) {
                plugin.getShopMessageManager().sendMessage(player, "insufficient-funds");
                plugin.getShopSoundManager().playError(player);
                return false;
            }
            
            String takeCommand = getTakeCommand("shards");
            if (!takeCommand.isEmpty()) {
                String finalCommand = takeCommand.replace("%player%", player.getName()).replace("%price%", String.valueOf(totalPrice));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            } else {
                if (!plugin.getShardsManager().removeShards(player, totalPrice)) {
                    plugin.getShopMessageManager().sendMessage(player, "insufficient-funds");
                    plugin.getShopSoundManager().playError(player);
                    return false;
                }
            }
        } else {
            if (economy == null) {
                player.sendMessage(ChatColor.RED + "Vault economy not found!");
                plugin.getShopSoundManager().playError(player);
                return false;
            }
            
            if (!economy.has(player, totalPrice)) {
                plugin.getShopMessageManager().sendMessage(player, "insufficient-funds");
                plugin.getShopSoundManager().playError(player);
                return false;
            }
            
            economy.withdrawPlayer(player, totalPrice);
        }
        
        if (player.getInventory().firstEmpty() == -1 && item.getCommand().isEmpty()) {
            plugin.getShopMessageManager().sendMessage(player, "inventory-full");
            plugin.getShopSoundManager().playError(player);
            
            if (currencyType.equalsIgnoreCase("SHARDS")) {
                String placeCommand = getPlaceCommand("shards");
                if (!placeCommand.isEmpty()) {
                    String finalCommand = placeCommand.replace("%player%", player.getName()).replace("%price%", String.valueOf(totalPrice));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                } else {
                    plugin.getShardsManager().addShards(player, totalPrice);
                }
            } else {
                if (economy != null) {
                    economy.depositPlayer(player, totalPrice);
                }
            }
            return false;
        }
        
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
            player.getInventory().addItem(stack);
        }
        
        plugin.getShopMessageManager().sendMessage(player, "purchased", Map.of(
            "amount", String.valueOf(item.getAmount()),
            "item", item.getDisplayName(),
            "price", String.valueOf(totalPrice)
        ));
        plugin.getShopSoundManager().playPurchase(player);
        
        return true;
    }
          }

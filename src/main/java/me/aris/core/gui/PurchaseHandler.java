package me.aris.core.shop;

import me.aris.core.ArisCore;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PurchaseHandler {
    private ArisCore plugin;
    private Economy economy;
    private Map<Player, ShopItem> pendingPurchases;
    private FileConfiguration shopConfig;
    private FileConfiguration messageConfig;
    
    public PurchaseHandler(ArisCore plugin) {
        this.plugin = plugin;
        this.pendingPurchases = new HashMap<>();
        
        File configFile = new File(plugin.getDataFolder(), "Shop/config.yml");
        if (configFile.exists()) {
            shopConfig = YamlConfiguration.loadConfiguration(configFile);
        } else {
            shopConfig = new YamlConfiguration();
        }
        
        File messageFile = new File(plugin.getDataFolder(), "Shop/message.yml");
        if (messageFile.exists()) {
            messageConfig = YamlConfiguration.loadConfiguration(messageFile);
        } else {
            messageConfig = new YamlConfiguration();
        }
        
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            org.bukkit.plugin.RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                plugin.getLogger().info("Vault economy hooked!");
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
    
    private String getMessage(String path) {
        String prefix = messageConfig.getString("prefix", "&8[&aDonutShop&8] &r");
        String message = messageConfig.getString("message." + path, "");
        if (message.isEmpty()) return "";
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
    
    private String getActionBarMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', messageConfig.getString("message.actionbar-" + path, ""));
    }
    
    private void sendMessage(Player player, String path) {
        String msg = getMessage(path);
        String actionMsg = getActionBarMessage(path);
        
        boolean chatEnabled = shopConfig.getBoolean("messages.chat", true);
        boolean actionBarEnabled = shopConfig.getBoolean("messages.action-bar", true);
        
        if (chatEnabled && !msg.isEmpty()) {
            player.sendMessage(msg);
        }
        if (actionBarEnabled && !actionMsg.isEmpty()) {
            player.sendActionBar(actionMsg);
        }
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
                sendMessage(player, "insufficient-funds");
                return false;
            }
            
            String takeCommand = getTakeCommand("shards");
            if (!takeCommand.isEmpty()) {
                final String finalCommand = takeCommand.replace("%player%", player.getName()).replace("%price%", String.valueOf(totalPrice));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                    }
                }.runTask(plugin);
            } else {
                if (!plugin.getShardsManager().removeShards(player, totalPrice)) {
                    sendMessage(player, "insufficient-funds");
                    return false;
                }
            }
        } else {
            if (economy == null) {
                player.sendMessage(ChatColor.RED + "Vault economy not found!");
                return false;
            }
            
            if (!economy.has(player, totalPrice)) {
                sendMessage(player, "insufficient-funds");
                return false;
            }
            
            economy.withdrawPlayer(player, totalPrice);
        }
        
        if (player.getInventory().firstEmpty() == -1 && item.getCommand().isEmpty()) {
            sendMessage(player, "inventory-full");
            
            if (currencyType.equalsIgnoreCase("SHARDS")) {
                String placeCommand = getPlaceCommand("shards");
                if (!placeCommand.isEmpty()) {
                    final String finalCommand = placeCommand.replace("%player%", player.getName()).replace("%price%", String.valueOf(totalPrice));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                        }
                    }.runTask(plugin);
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
            final String finalCommand = item.getCommand().replace("%player%", player.getName()).replace("%amount%", String.valueOf(item.getAmount()));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                }
            }.runTask(plugin);
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
        
        return true;
    }
    }

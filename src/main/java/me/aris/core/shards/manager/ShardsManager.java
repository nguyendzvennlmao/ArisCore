package me.aris.core.shards.manager;

import me.aris.core.ArisCore;
import me.aris.core.shards.model.ShardsPlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShardsManager {
    private ArisCore plugin;
    private Map<UUID, Long> balances;
    private File dataFile;
    private YamlConfiguration dataConfig;
    private long startBalance;
    private long maxBalance;
    
    public ShardsManager(ArisCore plugin) {
        this.plugin = plugin;
        this.balances = new ConcurrentHashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "shards-data.yml");
        loadData();
    }
    
    private void loadData() {
        startBalance = plugin.getConfigManager().getModuleConfig("shards").getLong("start-balance", 0);
        maxBalance = plugin.getConfigManager().getModuleConfig("shards").getLong("max-balance", 999999999);
        
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            } catch (IOException e) {
                dataConfig = new YamlConfiguration();
            }
        } else {
            dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        }
        
        for (String uuidStr : dataConfig.getKeys(false)) {
            balances.put(UUID.fromString(uuidStr), dataConfig.getLong(uuidStr, startBalance));
        }
    }
    
    public void saveData() {
        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            dataConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save shards-data.yml");
        }
    }
    
    public long getBalance(Player player) {
        return balances.getOrDefault(player.getUniqueId(), startBalance);
    }
    
    public void getBalance(Player player, org.bukkit.command.CommandSender sender) {
        long balance = getBalance(player);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("balance", formatNumber(balance));
        
        if (sender instanceof Player) {
            plugin.getShardsMessageManager().sendMessage((Player) sender, "balance", placeholders);
        } else {
            sender.sendMessage("§e" + player.getName() + " §7has §6" + formatNumber(balance) + " Shards§7.");
        }
    }
    
    public void getBalance(Player player) {
        long balance = getBalance(player);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("balance", formatNumber(balance));
        plugin.getShardsMessageManager().sendMessage(player, "balance-self", placeholders);
        
        if (plugin.getConfigManager().getModuleConfig("shards").getBoolean("messages.action-bar", true)) {
            player.sendActionBar("§6" + formatNumber(balance) + " Shards");
        }
    }
    
    public boolean hasEnough(Player player, long amount) {
        return getBalance(player) >= amount;
    }
    
    public boolean addShards(Player player, long amount) {
        long newBalance = getBalance(player) + amount;
        if (newBalance > maxBalance) newBalance = maxBalance;
        balances.put(player.getUniqueId(), newBalance);
        saveData();
        plugin.getShardsSoundManager().playShardsReceive(player);
        return true;
    }
    
    public boolean removeShards(Player player, long amount) {
        long current = getBalance(player);
        if (current < amount) return false;
        long newBalance = current - amount;
        balances.put(player.getUniqueId(), newBalance);
        saveData();
        plugin.getShardsSoundManager().playShardsSend(player);
        return true;
    }
    
    public void setShards(Player player, long amount) {
        if (amount > maxBalance) amount = maxBalance;
        if (amount < 0) amount = 0;
        balances.put(player.getUniqueId(), amount);
        saveData();
    }
    
    public long getStartBalance() {
        return startBalance;
    }
    
    private String formatNumber(long number) {
        if (number >= 1000000000) {
            return String.format("%.1f", number / 1000000000.0) + "B";
        } else if (number >= 1000000) {
            return String.format("%.1f", number / 1000000.0) + "M";
        } else if (number >= 1000) {
            return String.format("%.1f", number / 1000.0) + "K";
        }
        return String.valueOf(number);
    }
                  }

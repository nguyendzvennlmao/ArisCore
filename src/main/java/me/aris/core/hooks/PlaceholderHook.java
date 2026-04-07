package me.aris.core.hooks;

import me.aris.core.ArisCore;
import org.bukkit.entity.Player;

public class PlaceholderHook {
    
    private ArisCore plugin;
    private PlaceholderExpansion expansion;
    
    public PlaceholderHook(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    public void register() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion = new PlaceholderExpansion();
            expansion.register();
            plugin.getLogger().info("PlaceholderAPI expansion registered!");
        }
    }
    
    public void unregister() {
        if (expansion != null) {
            expansion.unregister();
        }
    }
    
    private class PlaceholderExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {
        
        @Override
        public String getIdentifier() {
            return "arisshards";
        }
        
        @Override
        public String getAuthor() {
            return "ArisCore";
        }
        
        @Override
        public String getVersion() {
            return "1.0";
        }
        
        @Override
        public boolean persist() {
            return true;
        }
        
        @Override
        public String onPlaceholderRequest(Player player, String identifier) {
            if (player == null) {
                return "";
            }
            
            if (identifier.equals("balance")) {
                long balance = plugin.getShardsManager().getBalance(player);
                return formatNumber(balance);
            }
            
            return null;
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
            }

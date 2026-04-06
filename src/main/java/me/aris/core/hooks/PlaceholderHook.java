package me.aris.core.hooks;

import me.aris.core.ArisCore;
import me.aris.core.managers.ShardsManager;
import org.bukkit.entity.Player;

public class PlaceholderHook implements me.clip.placeholderapi.expansion.PlaceholderExpansion {
    
    private ArisCore plugin;
    
    public PlaceholderHook(ArisCore plugin) {
        this.plugin = plugin;
    }
    
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

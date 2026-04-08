package me.aris.core.sell.message;

import me.aris.core.ArisCore;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SellMessageManager {
    private ArisCore plugin;
    private FileConfiguration messageConfig;
    private FileConfiguration moduleConfig;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    public SellMessageManager(ArisCore plugin) {
        this.plugin = plugin;
        this.messageConfig = plugin.getConfigManager().getModuleMessage("sell");
        this.moduleConfig = plugin.getConfigManager().getModuleConfig("sell");
    }
    
    private String translateColors(String message) {
        if (message == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString();
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
    
    private String getRawMessage(String path) {
        if (messageConfig == null) return "";
        String prefix = messageConfig.getString("prefix", "&8[&6Sell&8]&r ");
        String message = messageConfig.getString("message." + path, "");
        if (message.isEmpty()) return "";
        return translateColors(prefix + message);
    }
    
    public void sendMessage(Player player, String path) {
        sendMessage(player, path, new HashMap<>());
    }
    
    public void sendMessage(Player player, String path, Map<String, String> placeholders) {
        if (player == null) return;
        
        boolean chatEnabled = moduleConfig.getBoolean("messages.chat", true);
        boolean actionBarEnabled = moduleConfig.getBoolean("messages.action-bar", true);
        
        String chatMessage = getRawMessage("chat-" + path);
        String actionBarMessage = getRawMessage("actionbar-" + path);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            chatMessage = chatMessage.replace("%" + entry.getKey() + "%", entry.getValue());
            actionBarMessage = actionBarMessage.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        if (chatEnabled && !chatMessage.isEmpty()) {
            player.sendMessage(chatMessage);
        }
        
        if (actionBarEnabled && !actionBarMessage.isEmpty()) {
            player.sendActionBar(actionBarMessage);
        }
    }
    
    public void sendMessage(Player player, String path, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        sendMessage(player, path, placeholders);
    }
          }

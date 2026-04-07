package me.aris.core.managers;

import me.aris.core.ArisCore;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager {
    private ArisCore plugin;
    private Map<String, FileConfiguration> messageConfigs;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    public MessageManager(ArisCore plugin) {
        this.plugin = plugin;
        this.messageConfigs = new HashMap<>();
        loadMessages();
    }
    
    public void loadMessages() {
        String[] modules = {"Afk", "Home", "Spawn", "Tpa", "Warp", "Rtp", "Shop", "Shards"};
        for (String module : modules) {
            File file = new File(plugin.getDataFolder(), module + "/message.yml");
            if (file.exists()) {
                messageConfigs.put(module.toLowerCase(), YamlConfiguration.loadConfiguration(file));
                plugin.getLogger().info("Loaded messages for " + module);
            }
        }
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
    
    public String getRawMessage(String module, String path) {
        FileConfiguration msgConfig = messageConfigs.get(module.toLowerCase());
        if (msgConfig == null) return "";
        String prefix = msgConfig.getString("prefix", "");
        String message = msgConfig.getString("message." + path, "");
        if (message.isEmpty()) return "";
        return translateColors(prefix + message);
    }
    
    public void sendMessage(Player player, String path, String module) {
        sendMessage(player, path, module, new HashMap<>());
    }
    
    public void sendMessage(Player player, String path, String module, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        sendMessage(player, path, module, placeholders);
    }
    
    public void sendMessage(Player player, String path, String module, Map<String, String> placeholders) {
        if (player == null) return;
        
        FileConfiguration moduleConfig = getModuleConfig(module);
        boolean chatEnabled = moduleConfig.getBoolean("messages.chat", true);
        boolean actionBarEnabled = moduleConfig.getBoolean("messages.action-bar", true);
        
        String chatMessage = getRawMessage(module, "chat-" + path);
        String actionBarMessage = getRawMessage(module, "actionbar-" + path);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            chatMessage = chatMessage.replace("%" + entry.getKey() + "%", entry.getValue());
            actionBarMessage = actionBarMessage.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        if (chatEnabled && !chatMessage.isEmpty()) {
            if (path.equals("receive-request") || path.equals("receive-here-request")) {
                sendClickableMessage(player, chatMessage);
            } else {
                player.sendMessage(chatMessage);
            }
        }
        
        if (actionBarEnabled && !actionBarMessage.isEmpty()) {
            player.sendActionBar(actionBarMessage);
        }
    }
    
    private void sendClickableMessage(Player player, String message) {
        String[] lines = message.split("\n");
        for (String line : lines) {
            TextComponent component = new TextComponent(line.trim());
            
            if (line.contains("<clickable>")) {
                String clickText = line.replaceAll(".*<clickable>(.*?)</clickable>.*", "$1");
                String cleanText = ChatColor.stripColor(clickText);
                
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cleanText));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    new ComponentBuilder("Click to accept").create()));
            }
            
            player.spigot().sendMessage(component);
        }
    }
    
    public void sendTeleportCountdown(Player player, String module, int time) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("time", String.valueOf(time));
        
        FileConfiguration moduleConfig = getModuleConfig(module);
        boolean chatEnabled = moduleConfig.getBoolean("messages.chat", true);
        boolean actionBarEnabled = moduleConfig.getBoolean("messages.action-bar", true);
        
        String chatMessage = getRawMessage(module, "chat-teleport-countdown");
        String actionBarMessage = getRawMessage(module, "actionbar-teleport-countdown");
        
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
    
    public void sendTeleportCancelled(Player player, String module, String reason) {
        String path = reason.equals("movement") ? "teleport-cancelled-movement" : "teleport-cancelled";
        
        FileConfiguration moduleConfig = getModuleConfig(module);
        boolean chatEnabled = moduleConfig.getBoolean("messages.chat", true);
        boolean actionBarEnabled = moduleConfig.getBoolean("messages.action-bar", true);
        
        String chatMessage = getRawMessage(module, "chat-" + path);
        String actionBarMessage = getRawMessage(module, "actionbar-" + path);
        
        if (chatEnabled && !chatMessage.isEmpty()) {
            player.sendMessage(chatMessage);
        }
        
        if (actionBarEnabled && !actionBarMessage.isEmpty()) {
            player.sendActionBar(actionBarMessage);
        }
    }
    
    public void sendTeleportSuccess(Player player, String module) {
        FileConfiguration moduleConfig = getModuleConfig(module);
        boolean chatEnabled = moduleConfig.getBoolean("messages.chat", true);
        boolean actionBarEnabled = moduleConfig.getBoolean("messages.action-bar", true);
        
        String chatMessage = getRawMessage(module, "chat-teleport-success");
        String actionBarMessage = getRawMessage(module, "actionbar-teleport-success");
        
        if (chatEnabled && !chatMessage.isEmpty()) {
            player.sendMessage(chatMessage);
        }
        
        if (actionBarEnabled && !actionBarMessage.isEmpty()) {
            player.sendActionBar(actionBarMessage);
        }
    }
    
    private FileConfiguration getModuleConfig(String module) {
        File file = new File(plugin.getDataFolder(), module + "/config.yml");
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return new YamlConfiguration();
    }
    }

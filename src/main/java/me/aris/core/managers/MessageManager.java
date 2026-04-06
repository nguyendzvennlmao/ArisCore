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
        String[] modules = {"Afk", "Home", "Spawn", "Tpa", "Warp", "Rtp"};
        for (String module : modules) {
            File file = new File(plugin.getDataFolder(), module + "/message.yml");
            if (file.exists()) {
                messageConfigs.put(module.toLowerCase(), YamlConfiguration.loadConfiguration(file));
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
    
    public void sendTeleportCountdown(Player player, String module, int time, Map<String, String> placeholders) {
        if (player == null) return;
        
        FileConfiguration msgConfig = messageConfigs.get(module.toLowerCase());
        if (msgConfig == null) return;
        
        FileConfiguration moduleConfig = getModuleConfig(module);
        boolean chatEnabled = moduleConfig.getBoolean("messages.chat", true);
        boolean actionBarEnabled = moduleConfig.getBoolean("messages.action-bar", true);
        
        String prefix = msgConfig.getString("prefix", "");
        String chatMessage = msgConfig.getString("message.chat-teleport-countdown", "");
        String actionBarMessage = msgConfig.getString("message.actionbar-teleport-countdown", "");
        
        chatMessage = chatMessage.replace("%time%", String.valueOf(time));
        actionBarMessage = actionBarMessage.replace("%time%", String.valueOf(time));
        
        if (chatEnabled && !chatMessage.isEmpty()) {
            player.sendMessage(translateColors(prefix + chatMessage));
        }
        
        if (actionBarEnabled && !actionBarMessage.isEmpty()) {
            player.sendActionBar(translateColors(actionBarMessage));
        }
    }
    
    public void sendTeleportCancelled(Player player, String module, String reason) {
        if (player == null) return;
        
        FileConfiguration msgConfig = messageConfigs.get(module.toLowerCase());
        if (msgConfig == null) return;
        
        FileConfiguration moduleConfig = getModuleConfig(module);
        boolean chatEnabled = moduleConfig.getBoolean("messages.chat", true);
        boolean actionBarEnabled = moduleConfig.getBoolean("messages.action-bar", true);
        
        String prefix = msgConfig.getString("prefix", "");
        String path = reason.equals("movement") ? "chat-teleport-cancelled-movement" : "chat-teleport-cancelled";
        String actionPath = reason.equals("movement") ? "actionbar-teleport-cancelled-movement" : "actionbar-teleport-cancelled";
        
        String chatMessage = msgConfig.getString("message." + path, "");
        String actionBarMessage = msgConfig.getString("message." + actionPath, "");
        
        if (chatEnabled && !chatMessage.isEmpty()) {
            player.sendMessage(translateColors(prefix + chatMessage));
        } else if (chatEnabled && reason.equals("movement")) {
            player.sendMessage(ChatColor.RED + "The transfer was cancelled because you have already moved.");
        }
        
        if (actionBarEnabled && !actionBarMessage.isEmpty()) {
            player.sendActionBar(translateColors(actionBarMessage));
        } else if (actionBarEnabled && reason.equals("movement")) {
            player.sendActionBar(ChatColor.RED + "Transfer cancelled - you moved");
        }
    }
    
    public void sendTeleportSuccess(Player player, String module) {
        if (player == null) return;
        
        FileConfiguration msgConfig = messageConfigs.get(module.toLowerCase());
        if (msgConfig == null) return;
        
        FileConfiguration moduleConfig = getModuleConfig(module);
        boolean chatEnabled = moduleConfig.getBoolean("messages.chat", true);
        boolean actionBarEnabled = moduleConfig.getBoolean("messages.action-bar", true);
        
        String prefix = msgConfig.getString("prefix", "");
        String chatMessage = msgConfig.getString("message.chat-teleport-success", "");
        String actionBarMessage = msgConfig.getString("message.actionbar-teleport-success", "");
        
        if (chatEnabled && !chatMessage.isEmpty()) {
            player.sendMessage(translateColors(prefix + chatMessage));
        } else if (chatEnabled) {
            player.sendMessage(ChatColor.GREEN + "Teleported successfully!");
        }
        
        if (actionBarEnabled && !actionBarMessage.isEmpty()) {
            player.sendActionBar(translateColors(actionBarMessage));
        } else if (actionBarEnabled) {
            player.sendActionBar(ChatColor.GREEN + "Teleported!");
        }
    }
    
    public void sendMessage(Player player, String path, String module, Map<String, String> placeholders) {
        if (player == null) return;
        
        FileConfiguration msgConfig = messageConfigs.get(module.toLowerCase());
        if (msgConfig == null) return;
        
        FileConfiguration moduleConfig = getModuleConfig(module);
        boolean chatEnabled = moduleConfig.getBoolean("messages.chat", true);
        boolean actionBarEnabled = moduleConfig.getBoolean("messages.action-bar", true);
        
        String prefix = msgConfig.getString("prefix", "");
        String chatMessage = msgConfig.getString("message.chat-" + path, "");
        String actionBarMessage = msgConfig.getString("message.actionbar-" + path, "");
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            chatMessage = chatMessage.replace("%" + entry.getKey() + "%", entry.getValue());
            actionBarMessage = actionBarMessage.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        if (chatEnabled && !chatMessage.isEmpty()) {
            if (path.equals("receive-request") || path.equals("receive-here-request")) {
                sendClickableMessage(player, translateColors(prefix + chatMessage));
            } else {
                player.sendMessage(translateColors(prefix + chatMessage));
            }
        }
        
        if (actionBarEnabled && !actionBarMessage.isEmpty()) {
            player.sendActionBar(translateColors(actionBarMessage));
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
    
    public void sendMessage(Player player, String path, String module) {
        sendMessage(player, path, module, new HashMap<>());
    }
    
    public void sendMessage(Player player, String path, String module, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        sendMessage(player, path, module, placeholders);
    }
    
    private FileConfiguration getModuleConfig(String module) {
        File file = new File(plugin.getDataFolder(), module + "/config.yml");
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return new YamlConfiguration();
    }
                }

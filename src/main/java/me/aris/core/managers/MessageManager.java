package me.aris.core.managers;

import me.aris.core.ArisCore;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
            StringBuilder replacement = new StringBuilder(net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString());
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
    
    private String getRawMessage(String module, String path) {
        FileConfiguration messages = messageConfigs.get(module.toLowerCase());
        if (messages == null) return "";
        String prefix = messages.getString("prefix", "");
        String message = messages.getString("message." + path, "");
        if (message.isEmpty()) return "";
        return translateColors(prefix + message);
    }
    
    private String getActionBarMessage(String module, String path) {
        FileConfiguration messages = messageConfigs.get(module.toLowerCase());
        if (messages == null) return "";
        String message = messages.getString("message.actionbar-" + path, "");
        if (message.isEmpty()) return "";
        return translateColors(message);
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
        
        boolean chatEnabled = plugin.getConfigManager().isChatEnabled(module);
        boolean actionBarEnabled = plugin.getConfigManager().isActionBarEnabled(module);
        
        if (chatEnabled) {
            String chatMessage = getRawMessage(module, "chat-" + path);
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                chatMessage = chatMessage.replace("%" + entry.getKey() + "%", entry.getValue());
            }
            if (!chatMessage.isEmpty()) {
                player.sendMessage(chatMessage);
            }
        }
        
        if (actionBarEnabled) {
            String actionBarMessage = getActionBarMessage(module, path);
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                actionBarMessage = actionBarMessage.replace("%" + entry.getKey() + "%", entry.getValue());
            }
            if (!actionBarMessage.isEmpty()) {
                player.sendActionBar(actionBarMessage);
            }
        }
    }
    
    public void sendTeleportCountdown(Player player, String module, int time, Map<String, String> extraPlaceholders) {
        Map<String, String> placeholders = new HashMap<>(extraPlaceholders);
        placeholders.put("time", String.valueOf(time));
        sendMessage(player, "teleport-countdown", module, placeholders);
    }
    
    public void sendTeleportCancelled(Player player, String module, String reason) {
        if (reason.equals("movement")) {
            sendMessage(player, "teleport-cancelled-movement", module);
        } else {
            sendMessage(player, "teleport-cancelled", module);
        }
    }
    
    public void sendTeleportSuccess(Player player, String module) {
        sendMessage(player, "teleport-success", module);
    }
            }

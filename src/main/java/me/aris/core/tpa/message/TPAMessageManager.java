package me.aris.core.tpa.message;

import me.aris.core.ArisCore;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TPAMessageManager {
    private ArisCore plugin;
    private FileConfiguration messageConfig;
    private boolean chatEnabled;
    private boolean actionBarEnabled;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    public TPAMessageManager(ArisCore plugin) {
        this.plugin = plugin;
        this.messageConfig = plugin.getTPAConfigManager().getMessageConfig();
        this.chatEnabled = plugin.getTPAConfigManager().isChatEnabled();
        this.actionBarEnabled = plugin.getTPAConfigManager().isActionBarEnabled();
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
        String prefix = messageConfig.getString("prefix", "&8[&bTPA&8]&r ");
        String message = messageConfig.getString("message." + path, "");
        if (message.isEmpty()) return "";
        return translateColors(prefix + message);
    }
    
    public void sendMessage(Player player, String path) {
        sendMessage(player, path, new HashMap<>());
    }
    
    public void sendMessage(Player player, String path, Map<String, String> placeholders) {
        if (player == null) return;
        
        String chatMessage = getRawMessage("chat-" + path);
        String actionBarMessage = getRawMessage("actionbar-" + path);
        
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
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to accept").create()));
            }
            player.spigot().sendMessage(component);
        }
    }
    
    public void sendMessage(Player player, String path, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        sendMessage(player, path, placeholders);
    }
        }

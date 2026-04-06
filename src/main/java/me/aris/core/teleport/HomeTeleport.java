package me.aris.core.teleport;

import me.aris.core.ArisCore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class HomeTeleport {
    private ArisCore plugin;
    private TeleportManager teleportManager;
    
    public HomeTeleport(ArisCore plugin, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.teleportManager = teleportManager;
    }
    
    public void teleport(Player player, Location location, String homeName) {
        teleportManager.startTeleport(player, location, new TeleportManager.TeleportCallback() {
            @Override
            public void onCountdown(int time) {
                String msg = getMessage("chat-teleport-countdown").replace("%time%", String.valueOf(time)).replace("%home%", homeName);
                String action = getMessage("actionbar-teleport-countdown").replace("%time%", String.valueOf(time)).replace("%home%", homeName);
                if (!msg.isEmpty()) player.sendMessage(msg);
                if (!action.isEmpty()) player.sendActionBar(action);
            }
            
            @Override
            public void onCancel() {
                String msg = getMessage("chat-teleport-cancelled-movement");
                String action = getMessage("actionbar-teleport-cancelled-movement");
                if (!msg.isEmpty()) {
                    player.sendMessage(msg);
                } else {
                    player.sendMessage(ChatColor.RED + "The transfer was cancelled because you have already moved.");
                }
                if (!action.isEmpty()) {
                    player.sendActionBar(action);
                } else {
                    player.sendActionBar(ChatColor.RED + "Transfer cancelled - you moved");
                }
            }
            
            @Override
            public void onSuccess() {
                String msg = getMessage("chat-teleport-success").replace("%home%", homeName);
                String action = getMessage("actionbar-teleport-success").replace("%home%", homeName);
                if (!msg.isEmpty()) player.sendMessage(msg);
                if (!action.isEmpty()) player.sendActionBar(action);
            }
        });
    }
    
    private String getMessage(String path) {
        File file = new File(plugin.getDataFolder(), "Home/message.yml");
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String prefix = config.getString("prefix", "");
            String message = config.getString("message." + path, "");
            if (!message.isEmpty()) {
                return ChatColor.translateAlternateColorCodes('&', prefix + message);
            }
        }
        return "";
    }
                    }

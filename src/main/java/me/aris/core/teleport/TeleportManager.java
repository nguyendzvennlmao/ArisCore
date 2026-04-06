package me.aris.core.teleport;

import me.aris.core.ArisCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    private ArisCore plugin;
    private Map<UUID, TeleportTask> activeTeleports;
    
    public TeleportManager(ArisCore plugin) {
        this.plugin = plugin;
        this.activeTeleports = new HashMap<>();
    }
    
    public void startTeleport(Player player, Location targetLocation, TeleportCallback callback) {
        cancelTeleport(player);
        
        TeleportTask task = new TeleportTask(plugin, player, targetLocation, callback);
        activeTeleports.put(player.getUniqueId(), task);
        task.start();
    }
    
    public void cancelTeleport(Player player) {
        TeleportTask task = activeTeleports.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
    
    public boolean isTeleporting(Player player) {
        return activeTeleports.containsKey(player.getUniqueId());
    }
    
    public interface TeleportCallback {
        void onCountdown(int time);
        void onCancel();
        void onSuccess();
    }
            }

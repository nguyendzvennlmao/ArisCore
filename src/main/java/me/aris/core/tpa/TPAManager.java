package me.aris.core.tpa;

import me.aris.core.ArisCore;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TPAManager {
    private ArisCore plugin;
    private Map<String, TeleportRequest> pendingRequests;
    private Map<String, Long> cooldowns;
    private Map<UUID, Boolean> tpaToggle;
    private Map<UUID, Boolean> tpaHereToggle;
    private Map<UUID, Boolean> tpAuto;
    private Map<UUID, Boolean> guiEnabled;
    private Map<UUID, Boolean> guiHereEnabled;
    private Map<UUID, Integer> persistentTasks;
    
    public TPAManager(ArisCore plugin) {
        this.plugin = plugin;
        this.pendingRequests = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.tpaToggle = new ConcurrentHashMap<>();
        this.tpaHereToggle = new ConcurrentHashMap<>();
        this.tpAuto = new ConcurrentHashMap<>();
        this.guiEnabled = new ConcurrentHashMap<>();
        this.guiHereEnabled = new ConcurrentHashMap<>();
        this.persistentTasks = new ConcurrentHashMap<>();
    }
    
    public boolean canSendRequest(Player sender, Player target) {
        if (sender.getUniqueId().equals(target.getUniqueId())) {
            return false;
        }
        
        String key = sender.getUniqueId().toString() + ":" + target.getUniqueId().toString();
        
        if (cooldowns.containsKey(key)) {
            long remaining = (cooldowns.get(key) + 30000) - System.currentTimeMillis();
            if (remaining > 0) {
                plugin.getMessageManager().sendMessage(sender, "request-cooldown", "tpa");
                return false;
            }
        }
        
        if (!isTPAEnabled(target)) {
            plugin.getMessageManager().sendMessage(sender, "block-tpa-request", "tpa");
            return false;
        }
        
        if (hasPendingRequestFrom(sender, target)) {
            plugin.getMessageManager().sendMessage(sender, "already-sent-request", "tpa", "player", target.getName());
            return false;
        }
        
        return true;
    }
    
    public void addRequest(TeleportRequest request) {
        String key = request.getSender().getUniqueId().toString() + ":" + request.getTarget().getUniqueId().toString();
        pendingRequests.put(key, request);
        
        String cooldownKey = request.getSender().getUniqueId().toString() + ":" + request.getTarget().getUniqueId().toString();
        cooldowns.put(cooldownKey, System.currentTimeMillis());
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (pendingRequests.containsKey(key)) {
                    pendingRequests.remove(key);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", request.getTarget().getName());
                    plugin.getMessageManager().sendMessage(request.getSender(), "request-expired", "tpa", placeholders);
                }
            }
        }.runTaskLater(plugin, 120 * 20L);
    }
    
    public TeleportRequest getRequest(Player target, Player sender) {
        String key = sender.getUniqueId().toString() + ":" + target.getUniqueId().toString();
        return pendingRequests.get(key);
    }
    
    public List<TeleportRequest> getRequestsForTarget(Player target) {
        List<TeleportRequest> requests = new ArrayList<>();
        for (TeleportRequest request : pendingRequests.values()) {
            if (request.getTarget().getUniqueId().equals(target.getUniqueId())) {
                requests.add(request);
            }
        }
        return requests;
    }
    
    public void removeRequest(TeleportRequest request) {
        String key = request.getSender().getUniqueId().toString() + ":" + request.getTarget().getUniqueId().toString();
        pendingRequests.remove(key);
    }
    
    public void removeAllRequestsFrom(Player sender) {
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().getSender().getUniqueId().equals(sender.getUniqueId()));
    }
    
    public boolean hasPendingRequestFrom(Player sender, Player target) {
        String key = sender.getUniqueId().toString() + ":" + target.getUniqueId().toString();
        return pendingRequests.containsKey(key);
    }
    
    public boolean isTPAEnabled(Player player) {
        return tpaToggle.getOrDefault(player.getUniqueId(), true);
    }
    
    public void setTPAEnabled(Player player, boolean enabled) {
        tpaToggle.put(player.getUniqueId(), enabled);
        plugin.getSoundManager().playSound(player, "tpa", enabled ? "toggle-on" : "toggle-off");
    }
    
    public boolean isTPAHereEnabled(Player player) {
        return tpaHereToggle.getOrDefault(player.getUniqueId(), true);
    }
    
    public void setTPAHereEnabled(Player player, boolean enabled) {
        tpaHereToggle.put(player.getUniqueId(), enabled);
        plugin.getSoundManager().playSound(player, "tpa", enabled ? "toggle-on" : "toggle-off");
    }
    
    public boolean isTPAutoEnabled(Player player) {
        return tpAuto.getOrDefault(player.getUniqueId(), false);
    }
    
    public void setTPAutoEnabled(Player player, boolean enabled) {
        tpAuto.put(player.getUniqueId(), enabled);
        
        if (enabled) {
            plugin.getMessageManager().sendMessage(player, "tpauto-on", "tpa");
            startPersistentActionBar(player);
            plugin.getSoundManager().playSound(player, "tpa", "tpauto-on");
        } else {
            stopPersistentActionBar(player);
            plugin.getMessageManager().sendMessage(player, "tpauto-off", "tpa");
            plugin.getSoundManager().playSound(player, "tpa", "tpauto-off");
        }
    }
    
    private void startPersistentActionBar(Player player) {
        stopPersistentActionBar(player);
        
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isTPAutoEnabled(player)) {
                    cancel();
                    persistentTasks.remove(player.getUniqueId());
                    return;
                }
                plugin.getMessageManager().sendMessage(player, "tpauto-on-persistent", "tpa");
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
        
        persistentTasks.put(player.getUniqueId(), taskId);
    }
    
    private void stopPersistentActionBar(Player player) {
        Integer taskId = persistentTasks.remove(player.getUniqueId());
        if (taskId != null) {
            org.bukkit.Bukkit.getScheduler().cancelTask(taskId);
        }
    }
    
    public boolean isGUIEnabled(Player player) {
        return guiEnabled.getOrDefault(player.getUniqueId(), true);
    }
    
    public void setGUIEnabled(Player player, boolean enabled) {
        guiEnabled.put(player.getUniqueId(), enabled);
    }
    
    public boolean isGUIHereEnabled(Player player) {
        return guiHereEnabled.getOrDefault(player.getUniqueId(), true);
    }
    
    public void setGUIHereEnabled(Player player, boolean enabled) {
        guiHereEnabled.put(player.getUniqueId(), enabled);
    }
    
    public void shutdown() {
        for (Integer taskId : persistentTasks.values()) {
            if (taskId != null) {
                org.bukkit.Bukkit.getScheduler().cancelTask(taskId);
            }
        }
        persistentTasks.clear();
    }
  }

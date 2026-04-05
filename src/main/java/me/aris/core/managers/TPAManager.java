package me.aris.core.managers;

import me.aris.core.ArisCore;
import me.aris.core.models.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;
import java.util.concurrent.*;

public class TPAManager {
    private ArisCore plugin;
    private Map<String, TeleportRequest> pendingRequests;
    private Map<String, Long> cooldowns;
    private Map<String, Integer> dailyRequests;
    private Map<UUID, Boolean> tpaToggle;
    private Map<UUID, Boolean> tpaHereToggle;
    private Map<UUID, Boolean> tpAuto;
    private Map<UUID, Boolean> guiEnabled;
    private Map<UUID, Boolean> guiHereEnabled;
    private Map<UUID, Integer> persistentTaskIds;
    private ScheduledExecutorService scheduler;
    private String lastRequestDay;
    
    public TPAManager(ArisCore plugin) {
        this.plugin = plugin;
        this.pendingRequests = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.dailyRequests = new ConcurrentHashMap<>();
        this.tpaToggle = new ConcurrentHashMap<>();
        this.tpaHereToggle = new ConcurrentHashMap<>();
        this.tpAuto = new ConcurrentHashMap<>();
        this.guiEnabled = new ConcurrentHashMap<>();
        this.guiHereEnabled = new ConcurrentHashMap<>();
        this.persistentTaskIds = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.lastRequestDay = getCurrentDay();
    }
    
    private String getCurrentDay() {
        return java.time.LocalDate.now().toString();
    }
    
    public void checkDayReset() {
        String currentDay = getCurrentDay();
        if (!currentDay.equals(lastRequestDay)) {
            dailyRequests.clear();
            lastRequestDay = currentDay;
        }
    }
    
    public boolean canSendRequest(Player sender, Player target) {
        if (sender.getUniqueId().equals(target.getUniqueId())) {
            return false;
        }
        
        String key = sender.getUniqueId().toString() + ":" + target.getUniqueId().toString();
        
        if (cooldowns.containsKey(key)) {
            long remaining = (cooldowns.get(key) + 30 * 1000L) - System.currentTimeMillis();
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
        
        scheduler.schedule(() -> {
            if (pendingRequests.containsKey(key)) {
                pendingRequests.remove(key);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", request.getTarget().getName());
                plugin.getMessageManager().sendMessage(request.getSender(), "request-expired", "tpa", placeholders);
            }
        }, 120, TimeUnit.SECONDS);
    }
    
    public TeleportRequest getRequest(Player target, Player sender) {
        String key = sender.getUniqueId().toString() + ":" + target.getUniqueId().toString();
        return pendingRequests.get(key);
    }
    
    public TeleportRequest getAnyRequest(Player target, String senderName) {
        for (TeleportRequest request : pendingRequests.values()) {
            if (request.getTarget().getUniqueId().equals(target.getUniqueId())) {
                if (senderName == null || request.getSender().getName().equalsIgnoreCase(senderName)) {
                    return request;
                }
            }
        }
        return null;
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
    }
    
    public boolean isTPAHereEnabled(Player player) {
        return tpaHereToggle.getOrDefault(player.getUniqueId(), true);
    }
    
    public void setTPAHereEnabled(Player player, boolean enabled) {
        tpaHereToggle.put(player.getUniqueId(), enabled);
    }
    
    public boolean isTPAutoEnabled(Player player) {
        return tpAuto.getOrDefault(player.getUniqueId(), false);
    }
    
    public void setTPAutoEnabled(Player player, boolean enabled) {
        tpAuto.put(player.getUniqueId(), enabled);
        
        if (enabled) {
            startPersistentActionBar(player);
            plugin.getMessageManager().sendMessage(player, "tpauto-on", "tpa");
        } else {
            stopPersistentActionBar(player);
            plugin.getMessageManager().sendMessage(player, "tpauto-off", "tpa");
        }
    }
    
    private void startPersistentActionBar(Player player) {
        stopPersistentActionBar(player);
        
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isTPAutoEnabled(player)) {
                    cancel();
                    persistentTaskIds.remove(player.getUniqueId());
                    return;
                }
                plugin.getMessageManager().sendMessage(player, "tpauto-on-persistent", "tpa");
            }
        }.runTaskTimer(plugin, 0L, 100L).getTaskId();
        
        persistentTaskIds.put(player.getUniqueId(), taskId);
    }
    
    private void stopPersistentActionBar(Player player) {
        Integer taskId = persistentTaskIds.remove(player.getUniqueId());
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
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
        for (Integer taskId : persistentTaskIds.values()) {
            if (taskId != null) {
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }
        persistentTaskIds.clear();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
  }

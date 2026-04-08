package me.aris.core.tpa.manager;

import me.aris.core.ArisCore;
import me.aris.core.tpa.model.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TPAManager {
    
    private final ArisCore plugin;
    private final Map<UUID, TeleportRequest> pendingRequests;
    private final Map<UUID, Boolean> tpaToggle;
    private final Map<UUID, Boolean> tpaHereToggle;
    private final Map<UUID, Boolean> tpaAuto;
    
    public TPAManager(ArisCore plugin) {
        this.plugin = plugin;
        this.pendingRequests = new ConcurrentHashMap<>();
        this.tpaToggle = new HashMap<>();
        this.tpaHereToggle = new HashMap<>();
        this.tpaAuto = new HashMap<>();
    }
    
    public void sendRequest(Player sender, Player target, boolean isHere) {
        UUID targetUUID = target.getUniqueId();
        
        if (isToggled(targetUUID, isHere)) {
            plugin.getTPAMessageManager().sendMessage(sender, "target_toggled");
            return;
        }
        
        if (hasPendingRequest(targetUUID)) {
            plugin.getTPAMessageManager().sendMessage(sender, "target_has_pending");
            return;
        }
        
        TeleportRequest request = new TeleportRequest(sender.getUniqueId(), targetUUID, isHere);
        pendingRequests.put(targetUUID, request);
        
        plugin.getTPAMessageManager().sendRequestMessage(sender, target, isHere);
        
        int timeout = plugin.getTPAConfigManager().getRequestTimeout();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.containsKey(targetUUID) && pendingRequests.get(targetUUID).equals(request)) {
                pendingRequests.remove(targetUUID);
                plugin.getTPAMessageManager().sendMessage(sender, "request_expired");
                plugin.getTPAMessageManager().sendMessage(target, "request_expired_target");
            }
        }, timeout * 20L);
    }
    
    public boolean acceptRequest(Player target) {
        UUID targetUUID = target.getUniqueId();
        
        if (!hasPendingRequest(targetUUID)) {
            plugin.getTPAMessageManager().sendMessage(target, "no_pending_request");
            return false;
        }
        
        TeleportRequest request = pendingRequests.remove(targetUUID);
        Player sender = Bukkit.getPlayer(request.getSenderUUID());
        
        if (sender == null || !sender.isOnline()) {
            plugin.getTPAMessageManager().sendMessage(target, "sender_offline");
            return false;
        }
        
        if (request.isHere()) {
            plugin.getTPATeleportManager().teleportHere(sender, target);
        } else {
            plugin.getTPATeleportManager().teleportTo(sender, target);
        }
        
        plugin.getTPAMessageManager().sendAcceptMessage(sender, target);
        plugin.getTPASoundManager().playAcceptSound(sender);
        plugin.getTPASoundManager().playAcceptSound(target);
        
        return true;
    }
    
    public boolean denyRequest(Player target) {
        UUID targetUUID = target.getUniqueId();
        
        if (!hasPendingRequest(targetUUID)) {
            plugin.getTPAMessageManager().sendMessage(target, "no_pending_request");
            return false;
        }
        
        TeleportRequest request = pendingRequests.remove(targetUUID);
        Player sender = Bukkit.getPlayer(request.getSenderUUID());
        
        if (sender != null && sender.isOnline()) {
            plugin.getTPAMessageManager().sendDenyMessage(sender, target);
            plugin.getTPASoundManager().playDenySound(sender);
        }
        
        plugin.getTPAMessageManager().sendMessage(target, "denied_request");
        plugin.getTPASoundManager().playDenySound(target);
        
        return true;
    }
    
    public boolean cancelRequest(Player sender) {
        for (Map.Entry<UUID, TeleportRequest> entry : pendingRequests.entrySet()) {
            TeleportRequest request = entry.getValue();
            if (request.getSenderUUID().equals(sender.getUniqueId())) {
                pendingRequests.remove(entry.getKey());
                Player target = Bukkit.getPlayer(entry.getKey());
                
                plugin.getTPAMessageManager().sendMessage(sender, "cancelled_request");
                if (target != null && target.isOnline()) {
                    plugin.getTPAMessageManager().sendMessage(target, "request_cancelled");
                    plugin.getTPASoundManager().playCancelSound(target);
                }
                return true;
            }
        }
        
        plugin.getTPAMessageManager().sendMessage(sender, "no_outgoing_request");
        return false;
    }
    
    public boolean hasPendingRequest(UUID uuid) {
        return pendingRequests.containsKey(uuid);
    }
    
    public void toggleTPA(UUID uuid) {
        tpaToggle.put(uuid, !isTPAToggled(uuid));
    }
    
    public void toggleTPAHere(UUID uuid) {
        tpaHereToggle.put(uuid, !isTPAHereToggled(uuid));
    }
    
    public void toggleTPAAuto(UUID uuid) {
        tpaAuto.put(uuid, !isTPAAutoEnabled(uuid));
    }
    
    public boolean isTPAToggled(UUID uuid) {
        return tpaToggle.getOrDefault(uuid, false);
    }
    
    public boolean isTPAHereToggled(UUID uuid) {
        return tpaHereToggle.getOrDefault(uuid, false);
    }
    
    public boolean isTPAAutoEnabled(UUID uuid) {
        return tpaAuto.getOrDefault(uuid, false);
    }
    
    private boolean isToggled(UUID uuid, boolean isHere) {
        if (isHere) {
            return isTPAHereToggled(uuid);
        } else {
            return isTPAToggled(uuid);
        }
    }
    
    public void clearAllRequests() {
        pendingRequests.clear();
    }
            }

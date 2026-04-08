package me.aris.core.tpa.model;

import org.bukkit.entity.Player;

public class TeleportRequest {
    private Player sender;
    private Player target;
    private boolean isHere;
    private long timestamp;
    
    public TeleportRequest(Player sender, Player target, boolean isHere) {
        this.sender = sender;
        this.target = target;
        this.isHere = isHere;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Player getSender() { return sender; }
    public Player getTarget() { return target; }
    public boolean isHere() { return isHere; }
    public long getTimestamp() { return timestamp; }
    }

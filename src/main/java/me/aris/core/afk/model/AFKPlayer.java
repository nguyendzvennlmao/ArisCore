package me.aris.core.afk.model;

import java.util.UUID;

public class AFKPlayer {
    private UUID uuid;
    private boolean afk;
    private long lastActive;
    
    public AFKPlayer(UUID uuid) {
        this.uuid = uuid;
        this.afk = false;
        this.lastActive = System.currentTimeMillis();
    }
    
    public UUID getUuid() { return uuid; }
    public boolean isAfk() { return afk; }
    public void setAfk(boolean afk) { this.afk = afk; }
    public long getLastActive() { return lastActive; }
    public void updateActivity() { this.lastActive = System.currentTimeMillis(); }
}

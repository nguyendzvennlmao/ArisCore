package me.aris.core.shards.model;

import java.util.UUID;

public class ShardsPlayer {
    private UUID uuid;
    private long balance;
    
    public ShardsPlayer(UUID uuid, long balance) {
        this.uuid = uuid;
        this.balance = balance;
    }
    
    public UUID getUuid() { return uuid; }
    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }
    public void addBalance(long amount) { this.balance += amount; }
    public void removeBalance(long amount) { this.balance -= amount; }
    public boolean hasEnough(long amount) { return this.balance >= amount; }
  }

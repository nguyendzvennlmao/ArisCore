package me.aris.core.rtp.manager;

import me.aris.core.ArisCore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RTPManager {
    private ArisCore plugin;
    private Random random;
    private Map<UUID, Long> cooldowns;
    
    public RTPManager(ArisCore plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.cooldowns = new HashMap<>();
    }
    
    public Location getRandomLocation(World world, int maxRadius, int minRadius, int minY, int maxY) {
        int x = random.nextInt(maxRadius * 2) - maxRadius;
        int z = random.nextInt(maxRadius * 2) - maxRadius;
        
        double distance = Math.sqrt(x * x + z * z);
        if (distance < minRadius) {
            x = (int) (x * (minRadius / distance));
            z = (int) (z * (minRadius / distance));
        }
        
        int y = minY + random.nextInt(maxY - minY + 1);
        
        return new Location(world, x + 0.5, y, z + 0.5);
    }
    
    public boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        org.bukkit.block.Block feetBlock = world.getBlockAt(x, y, z);
        org.bukkit.block.Block headBlock = world.getBlockAt(x, y + 1, z);
        org.bukkit.block.Block groundBlock = world.getBlockAt(x, y - 1, z);
        
        if (feetBlock.getType().isSolid() || !feetBlock.isPassable()) {
            return false;
        }
        
        if (headBlock.getType().isSolid() || !headBlock.isPassable()) {
            return false;
        }
        
        if (!groundBlock.getType().isSolid()) {
            return false;
        }
        
        if (groundBlock.getType().name().contains("LAVA") || groundBlock.getType().name().contains("WATER")) {
            return false;
        }
        
        return !world.getNearbyEntities(location, 2, 2, 2).stream().anyMatch(e -> !e.isDead());
    }
    
    public boolean isOnCooldown(Player player) {
        int cooldownSeconds = plugin.getConfigManager().getModuleConfig("rtp").getInt("cooldown-seconds", 60);
        long lastUse = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long remaining = (lastUse + cooldownSeconds * 1000L) - System.currentTimeMillis();
        return remaining > 0;
    }
    
    public long getCooldownRemaining(Player player) {
        int cooldownSeconds = plugin.getConfigManager().getModuleConfig("rtp").getInt("cooldown-seconds", 60);
        long lastUse = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        return (lastUse + cooldownSeconds * 1000L) - System.currentTimeMillis();
    }
    
    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
                                      }

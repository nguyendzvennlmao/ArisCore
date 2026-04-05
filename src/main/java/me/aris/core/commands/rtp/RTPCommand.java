package me.aris.core.commands.rtp;

import me.aris.core.ArisCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RTPCommand implements CommandExecutor {
    private ArisCore plugin;
    private Map<UUID, Long> cooldowns;
    private Random random;
    
    public RTPCommand(ArisCore plugin) {
        this.plugin = plugin;
        this.cooldowns = new ConcurrentHashMap<>();
        this.random = new Random();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "rtp");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.rtp")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "rtp");
            return true;
        }
        
        int cooldownSeconds = plugin.getConfigManager().getRtpConfig().getInt("cooldown-seconds", 60);
        long lastUse = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long remaining = (lastUse + cooldownSeconds * 1000L) - System.currentTimeMillis();
        
        if (remaining > 0) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("time", String.valueOf(remaining / 1000));
            plugin.getMessageManager().sendMessage(player, "cooldown", "rtp", placeholders);
            return true;
        }
        
        World world = player.getWorld();
        Location safeLocation = findSafeLocation(world);
        
        if (safeLocation == null) {
            plugin.getMessageManager().sendMessage(player, "no-safe-location", "rtp");
            return true;
        }
        
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        
        plugin.getTeleportManager().startTeleport(player, safeLocation,
            () -> {
                plugin.getMessageManager().sendMessage(player, "teleport-success", "rtp");
            },
            () -> {
                plugin.getMessageManager().sendMessage(player, "teleport-cancelled-movement", "rtp");
            }
        );
        
        return true;
    }
    
    private Location findSafeLocation(World world) {
        int maxRetries = plugin.getConfigManager().getRtpConfig().getInt("max-retries", 50);
        int maxRadius = 5000;
        int minRadius = 100;
        int minY = 63;
        int maxY = 120;
        
        for (int i = 0; i < maxRetries; i++) {
            int x = random.nextInt(maxRadius * 2) - maxRadius;
            int z = random.nextInt(maxRadius * 2) - maxRadius;
            
            double distance = Math.sqrt(x * x + z * z);
            if (distance < minRadius) {
                continue;
            }
            
            int y = minY + random.nextInt(maxY - minY + 1);
            
            Location loc = new Location(world, x, y, z);
            if (isSafeLocation(loc)) {
                return loc;
            }
        }
        
        return null;
    }
    
    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        Block feetBlock = world.getBlockAt(x, y, z);
        Block headBlock = world.getBlockAt(x, y + 1, z);
        Block groundBlock = world.getBlockAt(x, y - 1, z);
        
        if (feetBlock.getType() != Material.AIR && !feetBlock.isPassable()) {
            return false;
        }
        
        if (headBlock.getType() != Material.AIR && !headBlock.isPassable()) {
            return false;
        }
        
        if (!groundBlock.getType().isSolid() && groundBlock.getType() != Material.GRASS_BLOCK) {
            return false;
        }
        
        if (groundBlock.getType() == Material.LAVA || groundBlock.getType() == Material.WATER) {
            return false;
        }
        
        if (world.getNearbyEntities(location, 2, 2, 2).stream().anyMatch(e -> !e.isDead())) {
            return false;
        }
        
        return true;
    }
          }

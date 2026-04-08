package me.aris.core.afk.manager;

import me.aris.core.ArisCore;
import me.aris.core.afk.model.AFKPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKManager {
    private ArisCore plugin;
    private Map<UUID, AFKPlayer> afkPlayers;
    private Location afkLocation;
    private File afkFile;
    private YamlConfiguration afkConfig;
    private int taskId;
    
    public AFKManager(ArisCore plugin) {
        this.plugin = plugin;
        this.afkPlayers = new HashMap<>();
        this.afkFile = new File(plugin.getDataFolder(), "Location/afk.yml");
        loadAFKLocation();
        startAutoAFKTask();
    }
    
    private void loadAFKLocation() {
        if (!afkFile.exists()) {
            try {
                afkFile.createNewFile();
                afkConfig = YamlConfiguration.loadConfiguration(afkFile);
            } catch (IOException e) {
                afkConfig = new YamlConfiguration();
            }
        } else {
            afkConfig = YamlConfiguration.loadConfiguration(afkFile);
        }
        
        if (afkConfig.contains("world")) {
            afkLocation = new Location(
                Bukkit.getWorld(afkConfig.getString("world")),
                afkConfig.getDouble("x"),
                afkConfig.getDouble("y"),
                afkConfig.getDouble("z"),
                (float) afkConfig.getDouble("yaw"),
                (float) afkConfig.getDouble("pitch")
            );
        }
    }
    
    public void saveAFKLocation() {
        if (afkLocation != null && afkLocation.getWorld() != null) {
            afkConfig.set("world", afkLocation.getWorld().getName());
            afkConfig.set("x", afkLocation.getX());
            afkConfig.set("y", afkLocation.getY());
            afkConfig.set("z", afkLocation.getZ());
            afkConfig.set("yaw", afkLocation.getYaw());
            afkConfig.set("pitch", afkLocation.getPitch());
            try {
                afkConfig.save(afkFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save afk.yml");
            }
        }
    }
    
    public void setAFKLocation(Location location) {
        this.afkLocation = location.clone();
        saveAFKLocation();
    }
    
    public void deleteAFKLocation() {
        this.afkLocation = null;
        if (afkFile.exists()) {
            afkFile.delete();
        }
        try {
            afkFile.createNewFile();
            afkConfig = YamlConfiguration.loadConfiguration(afkFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to reset afk.yml");
        }
    }
    
    public Location getAFKLocation() {
        return afkLocation != null ? afkLocation.clone() : null;
    }
    
    public boolean isAFK(Player player) {
        AFKPlayer afkPlayer = afkPlayers.get(player.getUniqueId());
        return afkPlayer != null && afkPlayer.isAfk();
    }
    
    public void setAFK(Player player, boolean afk) {
        AFKPlayer afkPlayer = afkPlayers.computeIfAbsent(player.getUniqueId(), AFKPlayer::new);
        
        if (afk && !afkPlayer.isAfk()) {
            afkPlayer.setAfk(true);
            plugin.getAFKMessageManager().sendMessage(player, "afk-on");
            plugin.getAFKSoundManager().playAFKOn(player);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    plugin.getAFKMessageManager().sendMessage(online, "afk-on-other", "player", player.getName());
                }
            }
            if (afkLocation != null) {
                plugin.getTeleportManager().startTeleport(player, afkLocation, "afk");
            }
        } else if (!afk && afkPlayer.isAfk()) {
            afkPlayer.setAfk(false);
            plugin.getAFKMessageManager().sendMessage(player, "afk-off");
            plugin.getAFKSoundManager().playAFKOff(player);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    plugin.getAFKMessageManager().sendMessage(online, "afk-off-other", "player", player.getName());
                }
            }
        }
    }
    
    private void startAutoAFKTask() {
        int autoAFKTime = plugin.getConfigManager().getModuleConfig("afk").getInt("auto-afk-time", 300);
        int checkInterval = plugin.getConfigManager().getModuleConfig("afk").getInt("check-interval", 10);
        
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    AFKPlayer afkPlayer = afkPlayers.get(player.getUniqueId());
                    if (afkPlayer == null) {
                        afkPlayer = new AFKPlayer(player.getUniqueId());
                        afkPlayers.put(player.getUniqueId(), afkPlayer);
                    }
                    
                    if (!afkPlayer.isAfk()) {
                        long inactiveTime = System.currentTimeMillis() - afkPlayer.getLastActive();
                        if (inactiveTime > autoAFKTime * 1000L) {
                            setAFK(player, true);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, checkInterval * 20L).getTaskId();
    }
    
    public void updateActivity(Player player) {
        AFKPlayer afkPlayer = afkPlayers.computeIfAbsent(player.getUniqueId(), AFKPlayer::new);
        afkPlayer.updateActivity();
        
        if (afkPlayer.isAfk()) {
            setAFK(player, false);
        }
    }
    
    public void shutdown() {
        if (taskId != 0) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
            }

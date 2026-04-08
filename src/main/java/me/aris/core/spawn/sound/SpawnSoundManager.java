package me.aris.core.spawn.sound;

import me.aris.core.ArisCore;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SpawnSoundManager {
    private ArisCore plugin;
    private FileConfiguration soundConfig;
    
    public SpawnSoundManager(ArisCore plugin) {
        this.plugin = plugin;
        this.soundConfig = plugin.getConfigManager().getModuleSound("spawn");
    }
    
    private Sound getSound(String path) {
        String soundName = soundConfig.getString("sounds." + path, "ENTITY_EXPERIENCE_ORB_PICKUP");
        try {
            return Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
    }
    
    public void playSound(Player player, String path) {
        if (player == null) return;
        player.playSound(player.getLocation(), getSound(path), 1.0f, 1.0f);
    }
    
    public void playSpawnSet(Player player) {
        playSound(player, "spawn-set");
    }
    
    public void playSpawnDelete(Player player) {
        playSound(player, "spawn-delete");
    }
    
    public void playCountdown(Player player) {
        playSound(player, "countdown");
    }
    
    public void playTeleportSuccess(Player player) {
        playSound(player, "teleport-success");
    }
    
    public void playTeleportCancel(Player player) {
        playSound(player, "teleport-cancel");
    }
    
    public void playError(Player player) {
        playSound(player, "error");
    }
  }

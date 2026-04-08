package me.aris.core.warp.sound;

import me.aris.core.ArisCore;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class WarpSoundManager {
    private ArisCore plugin;
    private FileConfiguration soundConfig;
    
    public WarpSoundManager(ArisCore plugin) {
        this.plugin = plugin;
        this.soundConfig = plugin.getConfigManager().getModuleSound("warp");
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
    
    public void playWarpSet(Player player) {
        playSound(player, "warp-set");
    }
    
    public void playWarpDelete(Player player) {
        playSound(player, "warp-delete");
    }
    
    public void playError(Player player) {
        playSound(player, "error");
    }
              }

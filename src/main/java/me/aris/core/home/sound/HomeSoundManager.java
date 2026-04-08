package me.aris.core.home.sound;

import me.aris.core.ArisCore;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class HomeSoundManager {
    private ArisCore plugin;
    private FileConfiguration soundConfig;
    
    public HomeSoundManager(ArisCore plugin) {
        this.plugin = plugin;
        this.soundConfig = plugin.getConfigManager().getModuleSound("home");
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
    
    public void playTeleportSuccess(Player player) {
        playSound(player, "teleport-success");
    }
    
    public void playTeleportCancel(Player player) {
        playSound(player, "teleport-cancel");
    }
    
    public void playHomeSet(Player player) {
        playSound(player, "home-set");
    }
    
    public void playHomeDelete(Player player) {
        playSound(player, "home-delete");
    }
    
    public void playError(Player player) {
        playSound(player, "error");
    }
    }

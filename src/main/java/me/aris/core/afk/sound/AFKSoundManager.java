package me.aris.core.afk.sound;

import me.aris.core.ArisCore;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class AFKSoundManager {
    private ArisCore plugin;
    private FileConfiguration soundConfig;
    
    public AFKSoundManager(ArisCore plugin) {
        this.plugin = plugin;
        this.soundConfig = plugin.getConfigManager().getModuleSound("afk");
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
    
    public void playAFKOn(Player player) {
        playSound(player, "afk-on");
    }
    
    public void playAFKOff(Player player) {
        playSound(player, "afk-off");
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
    
    public void playAFKLocationSet(Player player) {
        playSound(player, "afk-location-set");
    }
    
    public void playAFKLocationDelete(Player player) {
        playSound(player, "afk-location-delete");
    }
    
    public void playError(Player player) {
        playSound(player, "error");
    }
}

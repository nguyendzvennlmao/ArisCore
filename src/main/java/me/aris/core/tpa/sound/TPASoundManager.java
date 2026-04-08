package me.aris.core.tpa.sound;

import me.aris.core.ArisCore;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class TPASoundManager {
    private ArisCore plugin;
    private FileConfiguration soundConfig;
    
    public TPASoundManager(ArisCore plugin) {
        this.plugin = plugin;
        this.soundConfig = plugin.getTPAConfigManager().getSoundConfig();
    }
    
    private Sound getSound(String path) {
        if (soundConfig == null) return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
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
    
    public void playRequestSent(Player player) {
        playSound(player, "request-sent");
    }
    
    public void playRequestReceived(Player player) {
        playSound(player, "request-received");
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
    
    public void playAccept(Player player) {
        playSound(player, "accept");
    }
    
    public void playDeny(Player player) {
        playSound(player, "deny");
    }
    
    public void playCancel(Player player) {
        playSound(player, "cancel");
    }
    
    public void playToggleOn(Player player) {
        playSound(player, "toggle-on");
    }
    
    public void playToggleOff(Player player) {
        playSound(player, "toggle-off");
    }
    
    public void playTPAutoOn(Player player) {
        playSound(player, "tpauto-on");
    }
    
    public void playTPAutoOff(Player player) {
        playSound(player, "tpauto-off");
    }
    
    public void playError(Player player) {
        playSound(player, "error");
    }
                                   }

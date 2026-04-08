package me.aris.core.sell.sound;

import me.aris.core.ArisCore;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SellSoundManager {
    private ArisCore plugin;
    private FileConfiguration soundConfig;
    
    public SellSoundManager(ArisCore plugin) {
        this.plugin = plugin;
        this.soundConfig = plugin.getConfigManager().getModuleSound("sell");
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
    
    public void playSellSuccess(Player player) {
        playSound(player, "sell-success");
    }
    
    public void playError(Player player) {
        playSound(player, "error");
    }
}

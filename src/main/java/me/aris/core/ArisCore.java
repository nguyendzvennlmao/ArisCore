package me.aris.core;

import me.aris.core.tpa.commands.*;
import me.aris.core.tpa.config.TPAConfigManager;
import me.aris.core.tpa.gui.TPAGUI;
import me.aris.core.tpa.manager.TPAManager;
import me.aris.core.tpa.message.TPAMessageManager;
import me.aris.core.tpa.sound.TPASoundManager;
import me.aris.core.tpa.teleport.TPATeleportManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ArisCore extends JavaPlugin {

    private static ArisCore instance;
    
    private TPAManager tpaManager;
    private TPAMessageManager messageManager;
    private TPASoundManager soundManager;
    private TPATeleportManager teleportManager;
    private TPAConfigManager configManager;
    private TPAGUI tpaGUI;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        this.configManager = new TPAConfigManager(this);
        this.messageManager = new TPAMessageManager(this);
        this.soundManager = new TPASoundManager(this);
        this.tpaManager = new TPAManager(this);
        this.teleportManager = new TPATeleportManager(this);
        this.tpaGUI = new TPAGUI(this);
        
        registerCommands();
        
        getLogger().info("§aArisCore TPA System has been enabled!");
        getLogger().info("§eCreated by nguyendzvenlimao");
    }

    @Override
    public void onDisable() {
        if (tpaManager != null) {
            tpaManager.clearAllRequests();
        }
        
        getLogger().info("§cArisCore TPA System has been disabled!");
    }
    
    private void registerCommands() {
        getCommand("tpa").setExecutor(new TPACommand(this));
        getCommand("tpahere").setExecutor(new TPAHereCommand(this));
        getCommand("tpaccept").setExecutor(new TPAcceptCommand(this));
        getCommand("tpadeny").setExecutor(new TPDenyCommand(this));
        getCommand("tpacancel").setExecutor(new TPAcancelCommand(this));
        getCommand("tpauto").setExecutor(new TPAAutoCommand(this));
        getCommand("tpatoggle").setExecutor(new TPAToggleCommand(this));
        getCommand("tpaheretoggle").setExecutor(new TPAHereToggleCommand(this));
    }
    
    public static ArisCore getInstance() {
        return instance;
    }
    
    public TPAManager getTPAManager() {
        return tpaManager;
    }
    
    public TPAMessageManager getTPAMessageManager() {
        return messageManager;
    }
    
    public TPASoundManager getTPASoundManager() {
        return soundManager;
    }
    
    public TPATeleportManager getTPATeleportManager() {
        return teleportManager;
    }
    
    public TPAConfigManager getTPAConfigManager() {
        return configManager;
    }
    
    public TPAGUI getTPAGUI() {
        return tpaGUI;
    }
            }

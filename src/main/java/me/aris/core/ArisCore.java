package me.aris.core;

import me.aris.core.tpa.TPAManager;
import me.aris.core.tpa.TPAMessageManager;
import me.aris.core.tpa.TPASoundManager;
import me.aris.core.tpa.TPAGUI;
import me.aris.core.tpa.TPATeleportManager;
import me.aris.core.tpa.config.TPAConfigManager;
import me.aris.core.tpa.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class ArisCore extends JavaPlugin {
    private static ArisCore instance;
    private TPAConfigManager tpaConfigManager;
    private TPAManager tpaManager;
    private TPAMessageManager tpaMessageManager;
    private TPASoundManager tpaSoundManager;
    private TPAGUI tpaGUI;
    private TPATeleportManager tpaTeleportManager;

    @Override
    public void onEnable() {
        instance = this;
        printLogo();
        createFolders();
        
        tpaConfigManager = new TPAConfigManager(this);
        tpaMessageManager = new TPAMessageManager(this);
        tpaSoundManager = new TPASoundManager(this);
        tpaTeleportManager = new TPATeleportManager(this);
        tpaManager = new TPAManager(this);
        tpaGUI = new TPAGUI(this);
        
        registerCommands();
        registerListeners();
        
        getLogger().info(ChatColor.GREEN + "ArisCore has been enabled!");
    }
    
    private void printLogo() {
        String logo = "\n&8&m----------------------------------------\n&eArisCore &fv1.0\n&aAuthor: VennLMAO\n&cSupport: folia - 1.21.x\n&8&m----------------------------------------";
        String[] lines = logo.split("\n");
        for (String line : lines) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }
    
    private void createFolders() {
        String[] folders = {"", "tpa", "tpa/gui"};
        for (String folder : folders) {
            new File(getDataFolder(), folder).mkdirs();
        }
    }
    
    private void registerCommands() {
        getCommand("tpa").setExecutor(new TPACommand(this));
        getCommand("tpahere").setExecutor(new TPAHereCommand(this));
        getCommand("tpaccept").setExecutor(new TPAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TPDenyCommand(this));
        getCommand("tpacancel").setExecutor(new TPAcancelCommand(this));
        getCommand("tpatoggle").setExecutor(new TPAToggleCommand(this));
        getCommand("tpaheretoggle").setExecutor(new TPAHereToggleCommand(this));
        getCommand("tpauto").setExecutor(new TPAutoCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(tpaGUI, this);
    }
    
    @Override
    public void onDisable() {
        if (tpaManager != null) tpaManager.shutdown();
        getLogger().info("ArisCore has been disabled!");
    }
    
    public static ArisCore getInstance() { return instance; }
    public TPAConfigManager getTPAConfigManager() { return tpaConfigManager; }
    public TPAManager getTPAManager() { return tpaManager; }
    public TPAMessageManager getTPAMessageManager() { return tpaMessageManager; }
    public TPASoundManager getTPASoundManager() { return tpaSoundManager; }
    public TPAGUI getTPAGUI() { return tpaGUI; }
    public TPATeleportManager getTPATeleportManager() { return tpaTeleportManager; }
    }

package me.aris.core;

import me.aris.core.commands.afk.AFKCommand;
import me.aris.core.commands.afk.DelAFKCommand;
import me.aris.core.commands.afk.SetAFKCommand;
import me.aris.core.commands.home.DelHomeCommand;
import me.aris.core.commands.home.HomeCommand;
import me.aris.core.commands.home.SetHomeCommand;
import me.aris.core.commands.rtp.RTPCommand;
import me.aris.core.commands.spawn.DelSpawnCommand;
import me.aris.core.commands.spawn.SetSpawnCommand;
import me.aris.core.commands.spawn.SpawnCommand;
import me.aris.core.commands.tpa.*;
import me.aris.core.commands.warp.DelWarpCommand;
import me.aris.core.commands.warp.SetWarpCommand;
import me.aris.core.commands.warp.WarpCommand;
import me.aris.core.gui.ConfirmGUI;
import me.aris.core.gui.HomeGUI;
import me.aris.core.gui.RTPGUI;
import me.aris.core.gui.WarpGUI;
import me.aris.core.listeners.AFKListener;
import me.aris.core.listeners.TeleportListener;
import me.aris.core.managers.*;
import me.aris.core.teleport.TeleportExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class ArisCore extends JavaPlugin {
    private static ArisCore instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private SpawnManager spawnManager;
    private AFKManager afkManager;
    private TPAManager tpaManager;
    private TeleportExecutor teleportExecutor;
    private TeleportManager teleportManager;
    private HomeGUI homeGUI;
    private WarpGUI warpGUI;
    private ConfirmGUI confirmGUI;
    private RTPGUI rtpGUI;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        homeManager = new HomeManager(this);
        warpManager = new WarpManager(this);
        spawnManager = new SpawnManager(this);
        afkManager = new AFKManager(this);
        tpaManager = new TPAManager(this);
        teleportExecutor = new TeleportExecutor(this);
        teleportManager = new TeleportManager(this);
        homeGUI = new HomeGUI(this);
        warpGUI = new WarpGUI(this);
        confirmGUI = new ConfirmGUI(this);
        rtpGUI = new RTPGUI(this);
        
        registerCommands();
        registerListeners();
        
        getLogger().info("ArisCore has been enabled!");
    }

    @Override
    public void onDisable() {
        if (homeManager != null) homeManager.saveHomes();
        if (warpManager != null) warpManager.saveWarps();
        if (tpaManager != null) tpaManager.shutdown();
        if (afkManager != null) afkManager.shutdown();
        getLogger().info("ArisCore has been disabled!");
    }

    private void registerCommands() {
        if (configManager.isModuleEnabled("tpa")) {
            getCommand("tpa").setExecutor(new TPACommand(this));
            getCommand("tpahere").setExecutor(new TPAHereCommand(this));
            getCommand("tpaccept").setExecutor(new TPAcceptCommand(this));
            getCommand("tpdeny").setExecutor(new TPDenyCommand(this));
            getCommand("tpacancel").setExecutor(new TPAcancelCommand(this));
            getCommand("tpatoggle").setExecutor(new TPAToggleCommand(this));
            getCommand("tpaheretoggle").setExecutor(new TPAHereToggleCommand(this));
            getCommand("tpauto").setExecutor(new TPAutoCommand(this));
        }
        
        if (configManager.isModuleEnabled("home")) {
            getCommand("home").setExecutor(new HomeCommand(this));
            getCommand("sethome").setExecutor(new SetHomeCommand(this));
            getCommand("delhome").setExecutor(new DelHomeCommand(this));
        }
        
        if (configManager.isModuleEnabled("warp")) {
            getCommand("warp").setExecutor(new WarpCommand(this));
            getCommand("setwarp").setExecutor(new SetWarpCommand(this));
            getCommand("delwarp").setExecutor(new DelWarpCommand(this));
        }
        
        if (configManager.isModuleEnabled("spawn")) {
            getCommand("spawn").setExecutor(new SpawnCommand(this));
            getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
            getCommand("delspawn").setExecutor(new DelSpawnCommand(this));
        }
        
        if (configManager.isModuleEnabled("afk")) {
            getCommand("afk").setExecutor(new AFKCommand(this));
            getCommand("setafk").setExecutor(new SetAFKCommand(this));
            getCommand("delafk").setExecutor(new DelAFKCommand(this));
        }
        
        if (configManager.isModuleEnabled("rtp")) {
            getCommand("rtp").setExecutor(new RTPCommand(this));
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        if (configManager.isModuleEnabled("afk")) {
            getServer().getPluginManager().registerEvents(new AFKListener(this), this);
        }
        getServer().getPluginManager().registerEvents(homeGUI, this);
        getServer().getPluginManager().registerEvents(confirmGUI, this);
        getServer().getPluginManager().registerEvents(warpGUI, this);
        if (configManager.isModuleEnabled("rtp")) {
            getServer().getPluginManager().registerEvents(rtpGUI, this);
        }
    }

    public static ArisCore getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public HomeManager getHomeManager() { return homeManager; }
    public WarpManager getWarpManager() { return warpManager; }
    public SpawnManager getSpawnManager() { return spawnManager; }
    public AFKManager getAFKManager() { return afkManager; }
    public TPAManager getTPAManager() { return tpaManager; }
    public TeleportExecutor getTeleportExecutor() { return teleportExecutor; }
    public TeleportManager getTeleportManager() { return teleportManager; }
    public HomeGUI getHomeGUI() { return homeGUI; }
    public WarpGUI getWarpGUI() { return warpGUI; }
    public ConfirmGUI getConfirmGUI() { return confirmGUI; }
    public RTPGUI getRTPGUI() { return rtpGUI; }
    }

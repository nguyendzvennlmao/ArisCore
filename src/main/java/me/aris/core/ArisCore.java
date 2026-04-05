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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
        
        printLogo();
        
        createAllConfigs();
        
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
        
        getLogger().info(ChatColor.GREEN + "ArisCore has been enabled!");
    }
    
    private void printLogo() {
        String logo = 
            "\n" +
            "&8&m----------------------------------------\n" +
            "&eArisCore &fv1.0\n" +
            "&aAuthor: VennLMAO\n" +
            "&cSupport: folia - 1.21.x\n" +
            "&8&m----------------------------------------";
        
        String[] lines = logo.split("\n");
        for (String line : lines) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }
    
    private void createAllConfigs() {
        createFolder("");
        createFolder("Location");
        createFolder("Afk");
        createFolder("Home");
        createFolder("Home/gui");
        createFolder("Spawn");
        createFolder("Tpa");
        createFolder("Tpa/gui");
        createFolder("Warp");
        createFolder("Rtp");
        
        createConfig("config.yml", "");
        createConfig("Afk/config.yml", "Afk/config.yml");
        createConfig("Afk/message.yml", "Afk/message.yml");
        createConfig("Home/config.yml", "Home/config.yml");
        createConfig("Home/message.yml", "Home/message.yml");
        createConfig("Home/gui/home.yml", "Home/gui/home.yml");
        createConfig("Home/gui/confirm.yml", "Home/gui/confirm.yml");
        createConfig("Spawn/config.yml", "Spawn/config.yml");
        createConfig("Spawn/message.yml", "Spawn/message.yml");
        createConfig("Tpa/config.yml", "Tpa/config.yml");
        createConfig("Tpa/message.yml", "Tpa/message.yml");
        createConfig("Tpa/gui/tpa.yml", "Tpa/gui/tpa.yml");
        createConfig("Tpa/gui/tpahere.yml", "Tpa/gui/tpahere.yml");
        createConfig("Warp/config.yml", "Warp/config.yml");
        createConfig("Warp/message.yml", "Warp/message.yml");
        createConfig("Warp/gui.yml", "Warp/gui.yml");
        createConfig("Rtp/config.yml", "Rtp/config.yml");
        createConfig("Rtp/message.yml", "Rtp/message.yml");
        createConfig("Rtp/gui.yml", "Rtp/gui.yml");
        
        createLocationFile("spawn.yml");
        createLocationFile("afk.yml");
        createLocationFile("warp.yml");
        createLocationFile("home.yml");
    }
    
    private void createFolder(String path) {
        File folder = new File(getDataFolder(), path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
    
    private void createConfig(String targetPath, String sourcePath) {
        File file = new File(getDataFolder(), targetPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                if (sourcePath != null && !sourcePath.isEmpty()) {
                    InputStream is = getResource(sourcePath);
                    if (is != null) {
                        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(is, StandardCharsets.UTF_8));
                        yaml.save(file);
                    }
                }
            } catch (IOException e) {
                getLogger().warning("Failed to create " + targetPath + ": " + e.getMessage());
            }
        }
    }
    
    private void createLocationFile(String fileName) {
        File file = new File(getDataFolder(), "Location/" + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                getLogger().warning("Failed to create Location/" + fileName + ": " + e.getMessage());
            }
        }
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
        if (configManager != null && configManager.isModuleEnabled("tpa")) {
            getCommand("tpa").setExecutor(new TPACommand(this));
            getCommand("tpahere").setExecutor(new TPAHereCommand(this));
            getCommand("tpaccept").setExecutor(new TPAcceptCommand(this));
            getCommand("tpdeny").setExecutor(new TPDenyCommand(this));
            getCommand("tpacancel").setExecutor(new TPAcancelCommand(this));
            getCommand("tpatoggle").setExecutor(new TPAToggleCommand(this));
            getCommand("tpaheretoggle").setExecutor(new TPAHereToggleCommand(this));
            getCommand("tpauto").setExecutor(new TPAutoCommand(this));
        }
        
        if (configManager != null && configManager.isModuleEnabled("home")) {
            getCommand("home").setExecutor(new HomeCommand(this));
            getCommand("sethome").setExecutor(new SetHomeCommand(this));
            getCommand("delhome").setExecutor(new DelHomeCommand(this));
        }
        
        if (configManager != null && configManager.isModuleEnabled("warp")) {
            getCommand("warp").setExecutor(new WarpCommand(this));
            getCommand("setwarp").setExecutor(new SetWarpCommand(this));
            getCommand("delwarp").setExecutor(new DelWarpCommand(this));
        }
        
        if (configManager != null && configManager.isModuleEnabled("spawn")) {
            getCommand("spawn").setExecutor(new SpawnCommand(this));
            getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
            getCommand("delspawn").setExecutor(new DelSpawnCommand(this));
        }
        
        if (configManager != null && configManager.isModuleEnabled("afk")) {
            getCommand("afk").setExecutor(new AFKCommand(this));
            getCommand("setafk").setExecutor(new SetAFKCommand(this));
            getCommand("delafk").setExecutor(new DelAFKCommand(this));
        }
        
        if (configManager != null && configManager.isModuleEnabled("rtp")) {
            getCommand("rtp").setExecutor(new RTPCommand(this));
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        if (configManager != null && configManager.isModuleEnabled("afk")) {
            getServer().getPluginManager().registerEvents(new AFKListener(this), this);
        }
        getServer().getPluginManager().registerEvents(homeGUI, this);
        getServer().getPluginManager().registerEvents(confirmGUI, this);
        getServer().getPluginManager().registerEvents(warpGUI, this);
        if (configManager != null && configManager.isModuleEnabled("rtp")) {
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

package me.aris.core;

import me.aris.core.commands.afk.AFKCommand;
import me.aris.core.commands.afk.DelAFKCommand;
import me.aris.core.commands.afk.SetAFKCommand;
import me.aris.core.commands.home.DelHomeCommand;
import me.aris.core.commands.home.HomeCommand;
import me.aris.core.commands.home.SetHomeCommand;
import me.aris.core.commands.rtp.RTPCommand;
import me.aris.core.commands.shards.ShardsCommand;
import me.aris.core.commands.shop.ShopCommand;
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
import me.aris.core.shop.ShopGUI;
import me.aris.core.teleport.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ArisCore extends JavaPlugin {
    private static ArisCore instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private SpawnManager spawnManager;
    private AFKManager afkManager;
    private TPAManager tpaManager;
    private ShardsManager shardsManager;
    private TeleportManager teleportManager;
    private HomeTeleport homeTeleport;
    private SpawnTeleport spawnTeleport;
    private WarpTeleport warpTeleport;
    private AfkTeleport afkTeleport;
    private TpaTeleport tpaTeleport;
    private RtpTeleport rtpTeleport;
    private HomeGUI homeGUI;
    private WarpGUI warpGUI;
    private ConfirmGUI confirmGUI;
    private RTPGUI rtpGUI;
    private ShopGUI shopGUI;

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
        shardsManager = new ShardsManager(this);
        teleportManager = new TeleportManager(this);
        homeTeleport = new HomeTeleport(this, teleportManager);
        spawnTeleport = new SpawnTeleport(this, teleportManager);
        warpTeleport = new WarpTeleport(this, teleportManager);
        afkTeleport = new AfkTeleport(this, teleportManager);
        tpaTeleport = new TpaTeleport(this, teleportManager);
        rtpTeleport = new RtpTeleport(this, teleportManager);
        homeGUI = new HomeGUI(this);
        warpGUI = new WarpGUI(this);
        confirmGUI = new ConfirmGUI(this);
        rtpGUI = new RTPGUI(this);
        shopGUI = new ShopGUI(this);
        
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
        createFolder("Shop");
        createFolder("Shop/gui");
        createFolder("Shards");
        
        createConfigFromResource("config.yml", "config.yml");
        
        createConfigFromResource("Afk/config.yml", "Afk/config.yml");
        createConfigFromResource("Afk/message.yml", "Afk/message.yml");
        
        createConfigFromResource("Home/config.yml", "Home/config.yml");
        createConfigFromResource("Home/message.yml", "Home/message.yml");
        createConfigFromResource("Home/gui/home.yml", "Home/gui/home.yml");
        createConfigFromResource("Home/gui/confirm.yml", "Home/gui/confirm.yml");
        
        createConfigFromResource("Spawn/config.yml", "Spawn/config.yml");
        createConfigFromResource("Spawn/message.yml", "Spawn/message.yml");
        
        createConfigFromResource("Tpa/config.yml", "Tpa/config.yml");
        createConfigFromResource("Tpa/message.yml", "Tpa/message.yml");
        createConfigFromResource("Tpa/gui/tpa.yml", "Tpa/gui/tpa.yml");
        createConfigFromResource("Tpa/gui/tpahere.yml", "Tpa/gui/tpahere.yml");
        
        createConfigFromResource("Warp/config.yml", "Warp/config.yml");
        createConfigFromResource("Warp/message.yml", "Warp/message.yml");
        createConfigFromResource("Warp/gui.yml", "Warp/gui.yml");
        
        createConfigFromResource("Rtp/config.yml", "Rtp/config.yml");
        createConfigFromResource("Rtp/message.yml", "Rtp/message.yml");
        createConfigFromResource("Rtp/gui.yml", "Rtp/gui.yml");
        
        createConfigFromResource("Shop/config.yml", "Shop/config.yml");
        createConfigFromResource("Shop/message.yml", "Shop/message.yml");
        createConfigFromResource("Shop/gui/end.yml", "Shop/gui/end.yml");
        createConfigFromResource("Shop/gui/nether.yml", "Shop/gui/nether.yml");
        createConfigFromResource("Shop/gui/gear.yml", "Shop/gui/gear.yml");
        createConfigFromResource("Shop/gui/food.yml", "Shop/gui/food.yml");
        createConfigFromResource("Shop/gui/shards.yml", "Shop/gui/shards.yml");
        
        createConfigFromResource("Shards/config.yml", "Shards/config.yml");
        createConfigFromResource("Shards/message.yml", "Shards/message.yml");
        
        createEmptyFile("Location/spawn.yml");
        createEmptyFile("Location/home.yml");
        createEmptyFile("Location/warp.yml");
        createEmptyFile("Location/afk.yml");
    }
    
    private void createFolder(String path) {
        File folder = new File(getDataFolder(), path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
    
    private void createConfigFromResource(String targetPath, String resourcePath) {
        File targetFile = new File(getDataFolder(), targetPath);
        if (!targetFile.exists()) {
            try {
                targetFile.getParentFile().mkdirs();
                InputStream is = getResource(resourcePath);
                if (is != null) {
                    Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().info("Created config: " + targetPath);
                } else {
                    targetFile.createNewFile();
                    getLogger().warning("Resource not found: " + resourcePath);
                }
            } catch (IOException e) {
                getLogger().warning("Failed to create " + targetPath + ": " + e.getMessage());
            }
        }
    }
    
    private void createEmptyFile(String path) {
        File file = new File(getDataFolder(), path);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                getLogger().warning("Failed to create " + path + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void onDisable() {
        if (homeManager != null) homeManager.saveHomes();
        if (warpManager != null) warpManager.saveWarps();
        if (tpaManager != null) tpaManager.shutdown();
        if (afkManager != null) afkManager.shutdown();
        if (shardsManager != null) shardsManager.saveData();
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
        
        if (configManager != null && configManager.isModuleEnabled("shop")) {
            getCommand("shop").setExecutor(new ShopCommand(this));
        }
        
        if (configManager != null && configManager.isModuleEnabled("shards")) {
            getCommand("shards").setExecutor(new ShardsCommand(this));
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
        if (configManager != null && configManager.isModuleEnabled("shop")) {
            getServer().getPluginManager().registerEvents(shopGUI, this);
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
    public ShardsManager getShardsManager() { return shardsManager; }
    public TeleportManager getTeleportManager() { return teleportManager; }
    public HomeTeleport getHomeTeleport() { return homeTeleport; }
    public SpawnTeleport getSpawnTeleport() { return spawnTeleport; }
    public WarpTeleport getWarpTeleport() { return warpTeleport; }
    public AfkTeleport getAfkTeleport() { return afkTeleport; }
    public TpaTeleport getTpaTeleport() { return tpaTeleport; }
    public RtpTeleport getRtpTeleport() { return rtpTeleport; }
    public HomeGUI getHomeGUI() { return homeGUI; }
    public WarpGUI getWarpGUI() { return warpGUI; }
    public ConfirmGUI getConfirmGUI() { return confirmGUI; }
    public RTPGUI getRTPGUI() { return rtpGUI; }
    public ShopGUI getShopGUI() { return shopGUI; }
    }

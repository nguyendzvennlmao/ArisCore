package me.aris.core;

import me.aris.core.managers.ConfigManager;
import me.aris.core.teleport.TeleportManager;
import me.aris.core.listeners.TeleportListener;
import me.aris.core.tpa.TPAManager;
import me.aris.core.tpa.TPAMessageManager;
import me.aris.core.tpa.TPASoundManager;
import me.aris.core.tpa.TPAGUI;
import me.aris.core.home.HomeManager;
import me.aris.core.home.HomeMessageManager;
import me.aris.core.home.HomeSoundManager;
import me.aris.core.home.HomeGUI;
import me.aris.core.home.ConfirmGUI;
import me.aris.core.warp.WarpManager;
import me.aris.core.warp.WarpMessageManager;
import me.aris.core.warp.WarpSoundManager;
import me.aris.core.spawn.SpawnManager;
import me.aris.core.spawn.SpawnMessageManager;
import me.aris.core.spawn.SpawnSoundManager;
import me.aris.core.afk.AFKManager;
import me.aris.core.afk.AFKMessageManager;
import me.aris.core.afk.AFKSoundManager;
import me.aris.core.afk.AFKListener;
import me.aris.core.rtp.RTPManager;
import me.aris.core.rtp.RTPMessageManager;
import me.aris.core.rtp.RTPSoundManager;
import me.aris.core.rtp.RTPGUI;
import me.aris.core.shop.ShopManager;
import me.aris.core.shop.ShopMessageManager;
import me.aris.core.shop.ShopSoundManager;
import me.aris.core.shop.ShopGUI;
import me.aris.core.shards.ShardsManager;
import me.aris.core.shards.ShardsMessageManager;
import me.aris.core.shards.ShardsSoundManager;
import me.aris.core.sell.SellManager;
import me.aris.core.sell.SellMessageManager;
import me.aris.core.sell.SellSoundManager;
import me.aris.core.sell.SellGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;

public class ArisCore extends JavaPlugin {
    private static ArisCore instance;
    private ConfigManager configManager;
    private TeleportManager teleportManager;
    
    private TPAManager tpaManager;
    private TPAMessageManager tpaMessageManager;
    private TPASoundManager tpaSoundManager;
    private TPAGUI tpaGUI;
    
    private HomeManager homeManager;
    private HomeMessageManager homeMessageManager;
    private HomeSoundManager homeSoundManager;
    private HomeGUI homeGUI;
    private ConfirmGUI confirmGUI;
    
    private WarpManager warpManager;
    private WarpMessageManager warpMessageManager;
    private WarpSoundManager warpSoundManager;
    
    private SpawnManager spawnManager;
    private SpawnMessageManager spawnMessageManager;
    private SpawnSoundManager spawnSoundManager;
    
    private AFKManager afkManager;
    private AFKMessageManager afkMessageManager;
    private AFKSoundManager afkSoundManager;
    private AFKListener afkListener;
    
    private RTPManager rtpManager;
    private RTPMessageManager rtpMessageManager;
    private RTPSoundManager rtpSoundManager;
    private RTPGUI rtpGUI;
    
    private ShopManager shopManager;
    private ShopMessageManager shopMessageManager;
    private ShopSoundManager shopSoundManager;
    private ShopGUI shopGUI;
    
    private ShardsManager shardsManager;
    private ShardsMessageManager shardsMessageManager;
    private ShardsSoundManager shardsSoundManager;
    
    private SellManager sellManager;
    private SellMessageManager sellMessageManager;
    private SellSoundManager sellSoundManager;
    private SellGUI sellGUI;

    @Override
    public void onEnable() {
        instance = this;
        printLogo();
        createFolders();
        
        configManager = new ConfigManager(this);
        teleportManager = new TeleportManager(this);
        
        tpaMessageManager = new TPAMessageManager(this);
        tpaSoundManager = new TPASoundManager(this);
        tpaManager = new TPAManager(this);
        tpaGUI = new TPAGUI(this);
        
        homeMessageManager = new HomeMessageManager(this);
        homeSoundManager = new HomeSoundManager(this);
        homeManager = new HomeManager(this);
        homeGUI = new HomeGUI(this);
        confirmGUI = new ConfirmGUI(this);
        
        warpMessageManager = new WarpMessageManager(this);
        warpSoundManager = new WarpSoundManager(this);
        warpManager = new WarpManager(this);
        
        spawnMessageManager = new SpawnMessageManager(this);
        spawnSoundManager = new SpawnSoundManager(this);
        spawnManager = new SpawnManager(this);
        
        afkMessageManager = new AFKMessageManager(this);
        afkSoundManager = new AFKSoundManager(this);
        afkManager = new AFKManager(this);
        afkListener = new AFKListener(this);
        
        rtpMessageManager = new RTPMessageManager(this);
        rtpSoundManager = new RTPSoundManager(this);
        rtpManager = new RTPManager(this);
        rtpGUI = new RTPGUI(this);
        
        shopMessageManager = new ShopMessageManager(this);
        shopSoundManager = new ShopSoundManager(this);
        shopManager = new ShopManager(this);
        shopGUI = new ShopGUI(this);
        
        shardsMessageManager = new ShardsMessageManager(this);
        shardsSoundManager = new ShardsSoundManager(this);
        shardsManager = new ShardsManager(this);
        
        sellMessageManager = new SellMessageManager(this);
        sellSoundManager = new SellSoundManager(this);
        sellManager = new SellManager(this);
        sellGUI = new SellGUI(this);
        
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
        String[] folders = {"", "Location", "tpa", "tpa/gui", "home", "home/gui", "warp", "spawn", "afk", "rtp", "shop", "shop/gui", "shards", "sell"};
        for (String folder : folders) {
            new File(getDataFolder(), folder).mkdirs();
        }
        
        String[] files = {"config.yml", "Location/spawn.yml", "Location/home.yml", "Location/warp.yml", "Location/afk.yml", "shards-data.yml"};
        for (String file : files) {
            File f = new File(getDataFolder(), file);
            if (!f.exists()) {
                try { f.createNewFile(); } catch (IOException e) {}
            }
        }
    }
    
    private void registerCommands() {
        if (configManager.isModuleEnabled("tpa")) {
            getCommand("tpa").setExecutor(new me.aris.core.tpa.commands.TPACommand(this));
            getCommand("tpahere").setExecutor(new me.aris.core.tpa.commands.TPAHereCommand(this));
            getCommand("tpaccept").setExecutor(new me.aris.core.tpa.commands.TPAcceptCommand(this));
            getCommand("tpdeny").setExecutor(new me.aris.core.tpa.commands.TPDenyCommand(this));
            getCommand("tpacancel").setExecutor(new me.aris.core.tpa.commands.TPAcancelCommand(this));
            getCommand("tpatoggle").setExecutor(new me.aris.core.tpa.commands.TPAToggleCommand(this));
            getCommand("tpaheretoggle").setExecutor(new me.aris.core.tpa.commands.TPAHereToggleCommand(this));
            getCommand("tpauto").setExecutor(new me.aris.core.tpa.commands.TPAutoCommand(this));
        }
        if (configManager.isModuleEnabled("home")) {
            getCommand("home").setExecutor(new me.aris.core.home.commands.HomeCommand(this));
            getCommand("sethome").setExecutor(new me.aris.core.home.commands.SetHomeCommand(this));
            getCommand("delhome").setExecutor(new me.aris.core.home.commands.DelHomeCommand(this));
        }
        if (configManager.isModuleEnabled("warp")) {
            getCommand("warp").setExecutor(new me.aris.core.warp.commands.WarpCommand(this));
            getCommand("setwarp").setExecutor(new me.aris.core.warp.commands.SetWarpCommand(this));
            getCommand("delwarp").setExecutor(new me.aris.core.warp.commands.DelWarpCommand(this));
        }
        if (configManager.isModuleEnabled("spawn")) {
            getCommand("spawn").setExecutor(new me.aris.core.spawn.commands.SpawnCommand(this));
            getCommand("setspawn").setExecutor(new me.aris.core.spawn.commands.SetSpawnCommand(this));
            getCommand("delspawn").setExecutor(new me.aris.core.spawn.commands.DelSpawnCommand(this));
        }
        if (configManager.isModuleEnabled("afk")) {
            getCommand("afk").setExecutor(new me.aris.core.afk.commands.AFKCommand(this));
            getCommand("setafk").setExecutor(new me.aris.core.afk.commands.SetAFKCommand(this));
            getCommand("delafk").setExecutor(new me.aris.core.afk.commands.DelAFKCommand(this));
        }
        if (configManager.isModuleEnabled("rtp")) {
            getCommand("rtp").setExecutor(new me.aris.core.rtp.commands.RTPCommand(this));
        }
        if (configManager.isModuleEnabled("shop")) {
            getCommand("shop").setExecutor(new me.aris.core.shop.commands.ShopCommand(this));
        }
        if (configManager.isModuleEnabled("shards")) {
            getCommand("shards").setExecutor(new me.aris.core.shards.commands.ShardsCommand(this));
        }
        if (configManager.isModuleEnabled("sell")) {
            getCommand("sell").setExecutor(new me.aris.core.sell.commands.SellCommand(this));
        }
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        if (configManager.isModuleEnabled("afk")) {
            getServer().getPluginManager().registerEvents(afkListener, this);
        }
        if (configManager.isModuleEnabled("home")) {
            getServer().getPluginManager().registerEvents(homeGUI, this);
            getServer().getPluginManager().registerEvents(confirmGUI, this);
        }
        if (configManager.isModuleEnabled("tpa")) {
            getServer().getPluginManager().registerEvents(tpaGUI, this);
        }
        if (configManager.isModuleEnabled("rtp")) {
            getServer().getPluginManager().registerEvents(rtpGUI, this);
        }
        if (configManager.isModuleEnabled("shop")) {
            getServer().getPluginManager().registerEvents(shopGUI, this);
        }
        if (configManager.isModuleEnabled("sell")) {
            getServer().getPluginManager().registerEvents(sellGUI, this);
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
    
    public static ArisCore getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public TeleportManager getTeleportManager() { return teleportManager; }
    
    public TPAManager getTPAManager() { return tpaManager; }
    public TPAMessageManager getTPAMessageManager() { return tpaMessageManager; }
    public TPASoundManager getTPASoundManager() { return tpaSoundManager; }
    public TPAGUI getTPAGUI() { return tpaGUI; }
    
    public HomeManager getHomeManager() { return homeManager; }
    public HomeMessageManager getHomeMessageManager() { return homeMessageManager; }
    public HomeSoundManager getHomeSoundManager() { return homeSoundManager; }
    public HomeGUI getHomeGUI() { return homeGUI; }
    public ConfirmGUI getConfirmGUI() { return confirmGUI; }
    
    public WarpManager getWarpManager() { return warpManager; }
    public WarpMessageManager getWarpMessageManager() { return warpMessageManager; }
    public WarpSoundManager getWarpSoundManager() { return warpSoundManager; }
    
    public SpawnManager getSpawnManager() { return spawnManager; }
    public SpawnMessageManager getSpawnMessageManager() { return spawnMessageManager; }
    public SpawnSoundManager getSpawnSoundManager() { return spawnSoundManager; }
    
    public AFKManager getAFKManager() { return afkManager; }
    public AFKMessageManager getAFKMessageManager() { return afkMessageManager; }
    public AFKSoundManager getAFKSoundManager() { return afkSoundManager; }
    
    public RTPManager getRTPManager() { return rtpManager; }
    public RTPMessageManager getRTPMessageManager() { return rtpMessageManager; }
    public RTPSoundManager getRTPSoundManager() { return rtpSoundManager; }
    public RTPGUI getRTPGUI() { return rtpGUI; }
    
    public ShopManager getShopManager() { return shopManager; }
    public ShopMessageManager getShopMessageManager() { return shopMessageManager; }
    public ShopSoundManager getShopSoundManager() { return shopSoundManager; }
    public ShopGUI getShopGUI() { return shopGUI; }
    
    public ShardsManager getShardsManager() { return shardsManager; }
    public ShardsMessageManager getShardsMessageManager() { return shardsMessageManager; }
    public ShardsSoundManager getShardsSoundManager() { return shardsSoundManager; }
    
    public SellManager getSellManager() { return sellManager; }
    public SellMessageManager getSellMessageManager() { return sellMessageManager; }
    public SellSoundManager getSellSoundManager() { return sellSoundManager; }
    public SellGUI getSellGUI() { return sellGUI; }
}

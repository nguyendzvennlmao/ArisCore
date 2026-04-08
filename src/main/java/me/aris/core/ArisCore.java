package me.aris.core;

import me.aris.core.afk.commands.AFKCommand;
import me.aris.core.afk.commands.DelAFKCommand;
import me.aris.core.afk.commands.SetAFKCommand;
import me.aris.core.afk.listener.AFKListener;
import me.aris.core.afk.manager.AFKManager;
import me.aris.core.afk.message.AFKMessageManager;
import me.aris.core.afk.sound.AFKSoundManager;
import me.aris.core.home.commands.DelHomeCommand;
import me.aris.core.home.commands.HomeCommand;
import me.aris.core.home.commands.SetHomeCommand;
import me.aris.core.home.gui.ConfirmGUI;
import me.aris.core.home.gui.HomeGUI;
import me.aris.core.home.manager.HomeManager;
import me.aris.core.home.message.HomeMessageManager;
import me.aris.core.home.sound.HomeSoundManager;
import me.aris.core.managers.ConfigManager;
import me.aris.core.rtp.commands.RTPCommand;
import me.aris.core.rtp.gui.RTPGUI;
import me.aris.core.rtp.manager.RTPManager;
import me.aris.core.rtp.message.RTPMessageManager;
import me.aris.core.rtp.sound.RTPSoundManager;
import me.aris.core.sell.commands.SellCommand;
import me.aris.core.sell.gui.SellGUI;
import me.aris.core.sell.manager.SellManager;
import me.aris.core.sell.message.SellMessageManager;
import me.aris.core.sell.sound.SellSoundManager;
import me.aris.core.shards.commands.ShardsCommand;
import me.aris.core.shards.manager.ShardsManager;
import me.aris.core.shards.message.ShardsMessageManager;
import me.aris.core.shards.sound.ShardsSoundManager;
import me.aris.core.shop.commands.ShopCommand;
import me.aris.core.shop.gui.ShopGUI;
import me.aris.core.shop.manager.ShopManager;
import me.aris.core.shop.message.ShopMessageManager;
import me.aris.core.shop.sound.ShopSoundManager;
import me.aris.core.spawn.commands.DelSpawnCommand;
import me.aris.core.spawn.commands.SetSpawnCommand;
import me.aris.core.spawn.commands.SpawnCommand;
import me.aris.core.spawn.manager.SpawnManager;
import me.aris.core.spawn.message.SpawnMessageManager;
import me.aris.core.spawn.sound.SpawnSoundManager;
import me.aris.core.tpa.commands.*;
import me.aris.core.tpa.gui.TPAGUI;
import me.aris.core.tpa.manager.TPAManager;
import me.aris.core.tpa.message.TPAMessageManager;
import me.aris.core.tpa.sound.TPASoundManager;
import me.aris.core.warp.commands.DelWarpCommand;
import me.aris.core.warp.commands.SetWarpCommand;
import me.aris.core.warp.commands.WarpCommand;
import me.aris.core.warp.gui.WarpGUI;
import me.aris.core.warp.manager.WarpManager;
import me.aris.core.warp.message.WarpMessageManager;
import me.aris.core.warp.sound.WarpSoundManager;
import me.aris.core.listeners.TeleportListener;
import me.aris.core.teleport.TeleportManager;
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
    private WarpGUI warpGUI;
    
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
        createAllConfigs();
        
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
        warpGUI = new WarpGUI(this);
        
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
    
    private void createAllConfigs() {
        createFolder("");
        createFolder("Location");
        createFolder("tpa");
        createFolder("tpa/gui");
        createFolder("home");
        createFolder("home/gui");
        createFolder("warp");
        createFolder("spawn");
        createFolder("afk");
        createFolder("rtp");
        createFolder("shop");
        createFolder("shop/gui");
        createFolder("shards");
        createFolder("sell");
        
        createConfigFromResource("config.yml", "config.yml");
        
        createConfigFromResource("tpa/config.yml", "tpa/config.yml");
        createConfigFromResource("tpa/message.yml", "tpa/message.yml");
        createConfigFromResource("tpa/sound.yml", "tpa/sound.yml");
        createConfigFromResource("tpa/gui/tpa.yml", "tpa/gui/tpa.yml");
        createConfigFromResource("tpa/gui/tpahere.yml", "tpa/gui/tpahere.yml");
        
        createConfigFromResource("home/config.yml", "home/config.yml");
        createConfigFromResource("home/message.yml", "home/message.yml");
        createConfigFromResource("home/sound.yml", "home/sound.yml");
        createConfigFromResource("home/gui/home.yml", "home/gui/home.yml");
        createConfigFromResource("home/gui/confirm.yml", "home/gui/confirm.yml");
        
        createConfigFromResource("warp/config.yml", "warp/config.yml");
        createConfigFromResource("warp/message.yml", "warp/message.yml");
        createConfigFromResource("warp/sound.yml", "warp/sound.yml");
        
        createConfigFromResource("spawn/config.yml", "spawn/config.yml");
        createConfigFromResource("spawn/message.yml", "spawn/message.yml");
        createConfigFromResource("spawn/sound.yml", "spawn/sound.yml");
        
        createConfigFromResource("afk/config.yml", "afk/config.yml");
        createConfigFromResource("afk/message.yml", "afk/message.yml");
        createConfigFromResource("afk/sound.yml", "afk/sound.yml");
        
        createConfigFromResource("rtp/config.yml", "rtp/config.yml");
        createConfigFromResource("rtp/message.yml", "rtp/message.yml");
        createConfigFromResource("rtp/sound.yml", "rtp/sound.yml");
        createConfigFromResource("rtp/gui.yml", "rtp/gui.yml");
        
        createConfigFromResource("shop/config.yml", "shop/config.yml");
        createConfigFromResource("shop/message.yml", "shop/message.yml");
        createConfigFromResource("shop/sound.yml", "shop/sound.yml");
        createConfigFromResource("shop/gui/end.yml", "shop/gui/end.yml");
        createConfigFromResource("shop/gui/nether.yml", "shop/gui/nether.yml");
        createConfigFromResource("shop/gui/gear.yml", "shop/gui/gear.yml");
        createConfigFromResource("shop/gui/food.yml", "shop/gui/food.yml");
        createConfigFromResource("shop/gui/shards.yml", "shop/gui/shards.yml");
        
        createConfigFromResource("shards/config.yml", "shards/config.yml");
        createConfigFromResource("shards/message.yml", "shards/message.yml");
        createConfigFromResource("shards/sound.yml", "shards/sound.yml");
        
        createConfigFromResource("sell/config.yml", "sell/config.yml");
        createConfigFromResource("sell/message.yml", "sell/message.yml");
        createConfigFromResource("sell/sound.yml", "sell/sound.yml");
        createConfigFromResource("sell/prices.yml", "sell/prices.yml");
        createConfigFromResource("sell/gui.yml", "sell/gui.yml");
        
        createEmptyFile("Location/spawn.yml");
        createEmptyFile("Location/home.yml");
        createEmptyFile("Location/warp.yml");
        createEmptyFile("Location/afk.yml");
        createEmptyFile("shards-data.yml");
    }
    
    private void createFolder(String path) {
        File folder = new File(getDataFolder(), path);
        if (!folder.exists()) folder.mkdirs();
    }
    
    private void createConfigFromResource(String targetPath, String resourcePath) {
        File targetFile = new File(getDataFolder(), targetPath);
        if (!targetFile.exists()) {
            try {
                targetFile.getParentFile().mkdirs();
                InputStream is = getResource(resourcePath);
                if (is != null) {
                    Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    targetFile.createNewFile();
                }
            } catch (IOException e) {
                getLogger().warning("Failed to create " + targetPath);
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
                getLogger().warning("Failed to create " + path);
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
        if (configManager.isModuleEnabled("shop")) {
            getCommand("shop").setExecutor(new ShopCommand(this));
        }
        if (configManager.isModuleEnabled("shards")) {
            getCommand("shards").setExecutor(new ShardsCommand(this));
        }
        if (configManager.isModuleEnabled("sell")) {
            getCommand("sell").setExecutor(new SellCommand(this));
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
    public WarpGUI getWarpGUI() { return warpGUI; }
    
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

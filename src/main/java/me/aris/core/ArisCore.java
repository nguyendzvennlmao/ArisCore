package me.aris.core;

import me.aris.core.afk.*;
import me.aris.core.home.*;
import me.aris.core.managers.ConfigManager;
import me.aris.core.managers.MessageManager;
import me.aris.core.managers.SoundManager;
import me.aris.core.rtp.*;
import me.aris.core.sell.*;
import me.aris.core.shards.*;
import me.aris.core.shop.*;
import me.aris.core.spawn.*;
import me.aris.core.tpa.*;
import me.aris.core.warp.*;
import me.aris.core.listeners.TeleportListener;
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
    private SoundManager soundManager;
    private TPAManager tpaManager;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private SpawnManager spawnManager;
    private AFKManager afkManager;
    private RTPManager rtpManager;
    private ShopManager shopManager;
    private ShardsManager shardsManager;
    private SellManager sellManager;
    private TPAGUI tpaGUI;

    @Override
    public void onEnable() {
        instance = this;
        printLogo();
        createAllConfigs();
        
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        soundManager = new SoundManager(this);
        
        tpaManager = new TPAManager(this);
        homeManager = new HomeManager(this);
        warpManager = new WarpManager(this);
        spawnManager = new SpawnManager(this);
        afkManager = new AFKManager(this);
        rtpManager = new RTPManager(this);
        shopManager = new ShopManager(this);
        shardsManager = new ShardsManager(this);
        sellManager = new SellManager(this);
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
            getServer().getPluginManager().registerEvents(new AFKListener(this), this);
        }
        if (configManager.isModuleEnabled("home")) {
            getServer().getPluginManager().registerEvents(new HomeGUI(this), this);
            getServer().getPluginManager().registerEvents(new ConfirmGUI(this), this);
        }
        if (configManager.isModuleEnabled("tpa")) {
            getServer().getPluginManager().registerEvents(tpaGUI, this);
        }
        if (configManager.isModuleEnabled("rtp")) {
            getServer().getPluginManager().registerEvents(new RTPGUI(this), this);
        }
        if (configManager.isModuleEnabled("shop")) {
            getServer().getPluginManager().registerEvents(new ShopGUI(this), this);
        }
        if (configManager.isModuleEnabled("sell")) {
            getServer().getPluginManager().registerEvents(new SellGUI(this), this);
            getServer().getPluginManager().registerEvents(new ItemPriceManager(this), this);
        }
    }

    public static ArisCore getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public SoundManager getSoundManager() { return soundManager; }
    public TPAManager getTPAManager() { return tpaManager; }
    public HomeManager getHomeManager() { return homeManager; }
    public WarpManager getWarpManager() { return warpManager; }
    public SpawnManager getSpawnManager() { return spawnManager; }
    public AFKManager getAFKManager() { return afkManager; }
    public RTPManager getRTPManager() { return rtpManager; }
    public ShopManager getShopManager() { return shopManager; }
    public ShardsManager getShardsManager() { return shardsManager; }
    public SellManager getSellManager() { return sellManager; }
    public TPAGUI getTPAGUI() { return tpaGUI; }
}

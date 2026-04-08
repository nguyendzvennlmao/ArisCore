package me.aris.core.home.gui;

import me.aris.core.ArisCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfirmGUI implements Listener {
    private ArisCore plugin;
    private FileConfiguration guiConfig;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private String pendingDeleteHome = null;
    
    public ConfirmGUI(ArisCore plugin) {
        this.plugin = plugin;
        loadGuiConfig();
    }
    
    private void loadGuiConfig() {
        File guiFile = new File(plugin.getDataFolder(), "home/gui/confirm.yml");
        if (guiFile.exists()) {
            guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        } else {
            guiConfig = new YamlConfiguration();
        }
    }
    
    private String translateHexColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString();
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
    
    public void openConfirmGUI(Player player, String homeName) {
        pendingDeleteHome = homeName;
        
        String title = guiConfig.getString("title", "&8ᴄᴏɴꜰʀɪᴍ");
        int rows = guiConfig.getInt("rows", 3);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateHexColors(title));
        
        int infoSlot = guiConfig.getInt("items.home-info.slot", 13);
        String infoMaterial = guiConfig.getString("items.home-info.material", "BLUE_DYE");
        String infoName = guiConfig.getString("items.home-info.name", "&#0044FCʜᴏᴍᴇ %home%");
        infoName = infoName.replace("%home%", homeName);
        
        int cancelSlot = guiConfig.getInt("items.cancel.slot", 11);
        String cancelMaterial = guiConfig.getString("items.cancel.material", "RED_STAINED_GLASS_PANE");
        String cancelName = guiConfig.getString("items.cancel.name", "&#FF0000ᴄᴀɴᴄᴇʟ");
        List<String> cancelLore = guiConfig.getStringList("items.cancel.lore");
        
        int confirmSlot = guiConfig.getInt("items.confirm.slot", 15);
        String confirmMaterial = guiConfig.getString("items.confirm.material", "LIME_STAINED_GLASS_PANE");
        String confirmName = guiConfig.getString("items.confirm.name", "&#0bf52bᴄᴏɴꜰʀɪᴍ");
        List<String> confirmLore = guiConfig.getStringList("items.confirm.lore");
        
        Material infoMat;
        try {
            infoMat = Material.valueOf(infoMaterial);
        } catch (IllegalArgumentException e) {
            infoMat = Material.BLUE_DYE;
        }
        
        ItemStack infoItem = new ItemStack(infoMat);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(translateHexColors(infoName));
        infoItem.setItemMeta(infoMeta);
        gui.setItem(infoSlot, infoItem);
        
        Material cancelMat;
        try {
            cancelMat = Material.valueOf(cancelMaterial);
        } catch (IllegalArgumentException e) {
            cancelMat = Material.RED_STAINED_GLASS_PANE;
        }
        
        ItemStack cancelItem = new ItemStack(cancelMat);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(translateHexColors(cancelName));
        if (cancelLore != null && !cancelLore.isEmpty()) {
            cancelMeta.setLore(cancelLore.stream().map(this::translateHexColors).toList());
        }
        cancelItem.setItemMeta(cancelMeta);
        gui.setItem(cancelSlot, cancelItem);
        
        Material confirmMat;
        try {
            confirmMat = Material.valueOf(confirmMaterial);
        } catch (IllegalArgumentException e) {
            confirmMat = Material.LIME_STAINED_GLASS_PANE;
        }
        
        ItemStack confirmItem = new ItemStack(confirmMat);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName(translateHexColors(confirmName));
        if (confirmLore != null && !confirmLore.isEmpty()) {
            confirmMeta.setLore(confirmLore.stream().map(this::translateHexColors).toList());
        }
        confirmItem.setItemMeta(confirmMeta);
        gui.setItem(confirmSlot, confirmItem);
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        String title = event.getView().getTitle();
        String guiTitle = translateHexColors(guiConfig.getString("title", "&8ᴄᴏɴꜰʀɪᴍ"));
        
        if (!title.equals(guiTitle)) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        String confirmName = ChatColor.stripColor(translateHexColors(guiConfig.getString("items.confirm.name", "&#0bf52bᴄᴏɴꜰʀɪᴍ")));
        String cancelName = ChatColor.stripColor(translateHexColors(guiConfig.getString("items.cancel.name", "&#FF0000ᴄᴀɴᴄᴇʟ")));
        
        if (displayName.equals(confirmName)) {
            if (pendingDeleteHome != null) {
                player.performCommand("delhome " + pendingDeleteHome);
                pendingDeleteHome = null;
            }
            player.closeInventory();
        } else if (displayName.equals(cancelName)) {
            pendingDeleteHome = null;
            player.closeInventory();
        }
    }
    }

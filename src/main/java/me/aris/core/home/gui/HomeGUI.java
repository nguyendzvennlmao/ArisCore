package me.aris.core.home.gui;

import me.aris.core.ArisCore;
import me.aris.core.home.model.Home;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeGUI implements Listener {
    private ArisCore plugin;
    private FileConfiguration guiConfig;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    public HomeGUI(ArisCore plugin) {
        this.plugin = plugin;
        loadGuiConfig();
    }
    
    private void loadGuiConfig() {
        File guiFile = new File(plugin.getDataFolder(), "home/gui/home.yml");
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
    
    public void openHomeGUI(Player player) {
        loadGuiConfig();
        
        String title = guiConfig.getString("title", "&8ʜᴏᴍᴇꜱ");
        int rows = guiConfig.getInt("rows", 4);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateHexColors(title));
        
        String bedsSlotsStr = guiConfig.getString("slots.beds", "11,12,13,14,15");
        String dyesSlotsStr = guiConfig.getString("slots.dyes", "20,21,22,23,24");
        
        List<Integer> bedSlots = Arrays.stream(bedsSlotsStr.split(","))
            .map(String::trim).map(Integer::parseInt).toList();
        List<Integer> dyeSlots = Arrays.stream(dyesSlotsStr.split(","))
            .map(String::trim).map(Integer::parseInt).toList();
        
        Map<String, Home> homes = plugin.getHomeManager().getHomes(player);
        int maxHomes = plugin.getHomeManager().getMaxHomes(player);
        int currentHomes = homes.size();
        
        List<String> homeNames = new java.util.ArrayList<>(homes.keySet());
        
        for (int i = 0; i < bedSlots.size(); i++) {
            int bedSlot = bedSlots.get(i);
            int dyeSlot = dyeSlots.get(i);
            
            if (i < currentHomes) {
                String homeName = homeNames.get(i);
                gui.setItem(bedSlot, createHomeBedItem(homeName, true, true));
                gui.setItem(dyeSlot, createHomeDyeItem(homeName, true, true));
            } else if (i < maxHomes) {
                gui.setItem(bedSlot, createHomeBedItem("", false, true));
                gui.setItem(dyeSlot, createHomeDyeItem("", false, true));
            } else {
                gui.setItem(bedSlot, createHomeBedItem("", false, false));
                gui.setItem(dyeSlot, createHomeDyeItem("", false, false));
            }
        }
        
        player.openInventory(gui);
    }
    
    private ItemStack createHomeBedItem(String homeName, boolean hasHome, boolean hasPermission) {
        String path;
        if (!hasPermission) {
            path = "icons.no-permission.bed";
        } else if (hasHome) {
            path = "icons.home-set.bed";
        } else {
            path = "icons.home-not-set.bed";
        }
        
        String name = guiConfig.getString(path + ".name", "&7Unknown");
        name = name.replace("%home%", homeName);
        name = translateHexColors(name);
        
        String materialName = guiConfig.getString(path + ".material", hasHome ? "LIGHT_BLUE_BED" : "LIGHT_GRAY_BED");
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.LIGHT_BLUE_BED;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> lore = guiConfig.getStringList(path + ".lore");
        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = lore.stream()
                .map(line -> line.replace("%home%", homeName))
                .map(this::translateHexColors)
                .toList();
            meta.setLore(coloredLore);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createHomeDyeItem(String homeName, boolean hasHome, boolean hasPermission) {
        String path;
        if (!hasPermission) {
            path = "icons.no-permission.dye";
        } else if (hasHome) {
            path = "icons.home-set.dye";
        } else {
            path = "icons.home-not-set.dye";
        }
        
        String name = guiConfig.getString(path + ".name", "&7Unknown");
        name = name.replace("%home%", homeName);
        name = translateHexColors(name);
        
        String materialName = guiConfig.getString(path + ".material", hasHome ? "BLUE_DYE" : "GRAY_DYE");
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.BLUE_DYE;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> lore = guiConfig.getStringList(path + ".lore");
        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = lore.stream()
                .map(line -> line.replace("%home%", homeName))
                .map(this::translateHexColors)
                .toList();
            meta.setLore(coloredLore);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        String title = event.getView().getTitle();
        String guiTitle = translateHexColors(guiConfig.getString("title", "&8ʜᴏᴍᴇꜱ"));
        
        if (!title.equals(guiTitle)) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        String notSetName = ChatColor.stripColor(translateHexColors(guiConfig.getString("icons.home-not-set.bed.name", "&7ɴᴏ ʜᴏᴍᴇ ꜱᴇᴛ")));
        String noPermName = ChatColor.stripColor(translateHexColors(guiConfig.getString("icons.no-permission.bed.name", "&cɴᴏ ᴘᴇʀᴍɪꜱꜱɪᴏɴ")));
        
        if (displayName.contains(notSetName) || displayName.equals(notSetName)) {
            player.closeInventory();
            int nextSlot = plugin.getHomeManager().getHomes(player).size() + 1;
            player.performCommand("sethome " + nextSlot);
        } else if (displayName.contains(noPermName) || displayName.equals(noPermName)) {
            player.closeInventory();
            plugin.getHomeMessageManager().sendMessage(player, "home-limit-reached", 
                Map.of("current", String.valueOf(plugin.getHomeManager().getHomes(player).size()),
                       "max", String.valueOf(plugin.getHomeManager().getMaxHomes(player))));
        } else {
            String rawName = clicked.getItemMeta().getDisplayName();
            String coloredName = translateHexColors(rawName);
            String cleanName = ChatColor.stripColor(coloredName);
            String homeName = cleanName.replaceAll("(?i)home", "").replaceAll("ʜᴏᴍᴇ", "").trim();
            
            if (homeName.isEmpty()) {
                homeName = "1";
            }
            
            if (clicked.getType() == Material.BLUE_DYE || clicked.getType().name().contains("DYE")) {
                player.closeInventory();
                plugin.getConfirmGUI().openConfirmGUI(player, homeName);
            } else {
                player.closeInventory();
                player.performCommand("home " + homeName);
            }
        }
    }
                                      }

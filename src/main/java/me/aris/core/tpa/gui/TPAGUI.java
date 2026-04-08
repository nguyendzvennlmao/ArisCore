package me.aris.core.tpa.gui;

import me.aris.core.ArisCore;
import me.aris.core.tpa.model.TeleportRequest;
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
import org.bukkit.inventory.meta.SkullMeta;
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TPAGUI implements Listener {
    private ArisCore plugin;
    private FileConfiguration tpaGuiConfig;
    private FileConfiguration tpahereGuiConfig;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    public TPAGUI(ArisCore plugin) {
        this.plugin = plugin;
        loadConfigs();
    }
    
    private void loadConfigs() {
        File tpaFile = new File(plugin.getDataFolder(), "tpa/gui/tpa.yml");
        if (tpaFile.exists()) {
            tpaGuiConfig = YamlConfiguration.loadConfiguration(tpaFile);
        } else {
            tpaGuiConfig = new YamlConfiguration();
        }
        
        File tpahereFile = new File(plugin.getDataFolder(), "tpa/gui/tpahere.yml");
        if (tpahereFile.exists()) {
            tpahereGuiConfig = YamlConfiguration.loadConfiguration(tpahereFile);
        } else {
            tpahereGuiConfig = new YamlConfiguration();
        }
    }
    
    private String translateHexColors(String message) {
        if (message == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString();
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
    
    public void openRequestGUI(Player sender, Player target, boolean isHere) {
        FileConfiguration config = isHere ? tpahereGuiConfig : tpaGuiConfig;
        String section = "request-gui";
        
        String title = config.getString(section + ".name", "&8ᴄᴏɴғɪʀᴍ ʀᴇǫᴜᴇsᴛ");
        int rows = config.getInt(section + ".rows", 3);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateHexColors(title));
        
        String cancelSlot = config.getString(section + ".icons.cancel-icon.slot", "10");
        String cancelMaterial = config.getString(section + ".icons.cancel-icon.material", "RED_STAINED_GLASS_PANE");
        String cancelName = config.getString(section + ".icons.cancel-icon.display-name", "&4ᴄᴀɴᴄᴇʟ");
        List<String> cancelLore = config.getStringList(section + ".icons.cancel-icon.lore");
        
        String locationSlot = config.getString(section + ".icons.location-icon.slot", "12");
        String locationName = config.getString(section + ".icons.location-icon.display-name", "&aʟᴏᴄᴀᴛɪᴏɴ");
        List<String> locationLore = config.getStringList(section + ".icons.location-icon.lore");
        
        String playerSlot = config.getString(section + ".icons.player-icon.slot", "13");
        String playerName = config.getString(section + ".icons.player-icon.display-name", "&aᴘʟᴀʏᴇʀ");
        List<String> playerLore = config.getStringList(section + ".icons.player-icon.lore");
        
        String confirmSlot = config.getString(section + ".icons.confirm-icon.slot", "16");
        String confirmMaterial = config.getString(section + ".icons.confirm-icon.material", "LIME_STAINED_GLASS_PANE");
        String confirmName = config.getString(section + ".icons.confirm-icon.display-name", "&aᴄᴏɴғɪʀᴍ");
        List<String> confirmLore = config.getStringList(section + ".icons.confirm-icon.lore");
        
        String flySlot = config.getString(section + ".icons.fly-icon.slot", "14");
        String flyMaterial = config.getString(section + ".icons.fly-icon.material", "FEATHER");
        String flyName = config.getString(section + ".icons.fly-icon.display-name", "&aғʟʏɪɴɢ");
        List<String> flyLore = config.getStringList(section + ".icons.fly-icon.lore");
        
        gui.setItem(Integer.parseInt(cancelSlot), createGuiItem(cancelMaterial, cancelName, cancelLore));
        gui.setItem(Integer.parseInt(locationSlot), createGuiItem(Material.COMPASS, locationName, locationLore, target));
        gui.setItem(Integer.parseInt(playerSlot), createPlayerHead(target, playerName, playerLore));
        gui.setItem(Integer.parseInt(confirmSlot), createGuiItem(confirmMaterial, confirmName, confirmLore, target));
        
        String flyingStatus = (target.isFlying() || target.isGliding()) ? "&aYes" : "&cNo";
        List<String> coloredFlyLore = flyLore.stream().map(line -> line.replace("%is_flying%", translateHexColors(flyingStatus))).map(this::translateHexColors).toList();
        gui.setItem(Integer.parseInt(flySlot), createGuiItem(flyMaterial, flyName, coloredFlyLore));
        
        sender.openInventory(gui);
    }
    
    public void openAcceptGUI(Player target, TeleportRequest request) {
        boolean isHere = request.isHere();
        FileConfiguration config = isHere ? tpahereGuiConfig : tpaGuiConfig;
        String section = "accept-gui";
        
        String title = config.getString(section + ".name", "&8ᴀᴄᴄᴇᴘᴛ ʀᴇǫᴜᴇsᴛ");
        int rows = config.getInt(section + ".rows", 3);
        
        Inventory gui = Bukkit.createInventory(null, rows * 9, translateHexColors(title));
        
        String cancelSlot = config.getString(section + ".icons.cancel-icon.slot", "10");
        String cancelMaterial = config.getString(section + ".icons.cancel-icon.material", "RED_STAINED_GLASS_PANE");
        String cancelName = config.getString(section + ".icons.cancel-icon.display-name", "&4ᴅᴇɴʏ");
        List<String> cancelLore = config.getStringList(section + ".icons.cancel-icon.lore");
        
        String locationSlot = config.getString(section + ".icons.location-icon.slot", "12");
        String locationName = config.getString(section + ".icons.location-icon.display-name", "&aʟᴏᴄᴀᴛɪᴏɴ");
        List<String> locationLore = config.getStringList(section + ".icons.location-icon.lore");
        
        String playerSlot = config.getString(section + ".icons.player-icon.slot", "13");
        String playerName = config.getString(section + ".icons.player-icon.display-name", "&aᴘʟᴀʏᴇʀ");
        List<String> playerLore = config.getStringList(section + ".icons.player-icon.lore");
        
        String confirmSlot = config.getString(section + ".icons.confirm-icon.slot", "16");
        String confirmMaterial = config.getString(section + ".icons.confirm-icon.material", "LIME_STAINED_GLASS_PANE");
        String confirmName = config.getString(section + ".icons.confirm-icon.display-name", "&aᴀᴄᴄᴇᴘᴛ");
        List<String> confirmLore = config.getStringList(section + ".icons.confirm-icon.lore");
        
        String flySlot = config.getString(section + ".icons.fly-icon.slot", "14");
        String flyMaterial = config.getString(section + ".icons.fly-icon.material", "FEATHER");
        String flyName = config.getString(section + ".icons.fly-icon.display-name", "&aғʟʏɪɴɢ");
        List<String> flyLore = config.getStringList(section + ".icons.fly-icon.lore");
        
        Player sender = request.getSender();
        
        gui.setItem(Integer.parseInt(cancelSlot), createGuiItem(cancelMaterial, cancelName, cancelLore));
        gui.setItem(Integer.parseInt(locationSlot), createGuiItem(Material.COMPASS, locationName, locationLore, sender));
        gui.setItem(Integer.parseInt(playerSlot), createPlayerHead(sender, playerName, playerLore));
        gui.setItem(Integer.parseInt(confirmSlot), createGuiItem(confirmMaterial, confirmName, confirmLore, sender));
        
        String flyingStatus = (sender.isFlying() || sender.isGliding()) ? "&aYes" : "&cNo";
        List<String> coloredFlyLore = flyLore.stream().map(line -> line.replace("%is_flying%", translateHexColors(flyingStatus))).map(this::translateHexColors).toList();
        gui.setItem(Integer.parseInt(flySlot), createGuiItem(flyMaterial, flyName, coloredFlyLore));
        
        target.openInventory(gui);
    }
    
    private ItemStack createGuiItem(String materialName, String displayName, List<String> lore) {
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.STONE;
        }
        return createGuiItem(material, displayName, lore);
    }
    
    private ItemStack createGuiItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(translateHexColors(displayName));
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore.stream().map(this::translateHexColors).toList());
        }
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createGuiItem(String materialName, String displayName, List<String> lore, Player player) {
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.STONE;
        }
        return createGuiItem(material, displayName, lore, player);
    }
    
    private ItemStack createGuiItem(Material material, String displayName, List<String> lore, Player player) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(translateHexColors(displayName));
        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = lore.stream()
                .map(line -> line.replace("%world%", player.getWorld().getName()))
                .map(this::translateHexColors).toList();
            meta.setLore(coloredLore);
        }
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createPlayerHead(Player player, String displayName, List<String> lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(translateHexColors(displayName));
        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = lore.stream()
                .map(line -> line.replace("%player%", player.getName()))
                .map(this::translateHexColors).toList();
            meta.setLore(coloredLore);
        }
        head.setItemMeta(meta);
        return head;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        String requestTitle = translateHexColors(tpaGuiConfig.getString("request-gui.name", "&8ᴄᴏɴғɪʀᴍ ʀᴇǫᴜᴇsᴛ"));
        String acceptTitle = translateHexColors(tpaGuiConfig.getString("accept-gui.name", "&8ᴀᴄᴄᴇᴘᴛ ʀᴇǫᴜᴇsᴛ"));
        
        if (title.equals(requestTitle)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            
            String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            String confirmName = ChatColor.stripColor(translateHexColors(tpaGuiConfig.getString("request-gui.icons.confirm-icon.display-name", "&aᴄᴏɴғɪʀᴍ")));
            String cancelName = ChatColor.stripColor(translateHexColors(tpaGuiConfig.getString("request-gui.icons.cancel-icon.display-name", "&4ᴄᴀɴᴄᴇʟ")));
            
            if (displayName.equals(confirmName)) {
                player.closeInventory();
                String targetName = getTargetNameFromGUI(event);
                if (!targetName.isEmpty()) {
                    player.performCommand("tpa " + targetName);
                }
            } else if (displayName.equals(cancelName)) {
                player.closeInventory();
            }
        } else if (title.equals(acceptTitle)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            
            String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            String confirmName = ChatColor.stripColor(translateHexColors(tpaGuiConfig.getString("accept-gui.icons.confirm-icon.display-name", "&aᴀᴄᴄᴇᴘᴛ")));
            String cancelName = ChatColor.stripColor(translateHexColors(tpaGuiConfig.getString("accept-gui.icons.cancel-icon.display-name", "&4ᴅᴇɴʏ")));
            
            if (displayName.equals(confirmName)) {
                player.closeInventory();
                String senderName = getSenderNameFromGUI(event);
                if (!senderName.isEmpty()) {
                    player.performCommand("tpaccept " + senderName);
                }
            } else if (displayName.equals(cancelName)) {
                player.closeInventory();
                String senderName = getSenderNameFromGUI(event);
                if (!senderName.isEmpty()) {
                    player.performCommand("tpdeny " + senderName);
                }
            }
        }
    }
    
    private String getTargetNameFromGUI(InventoryClickEvent event) {
        ItemStack item = event.getInventory().getItem(13);
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = item.getItemMeta().getLore();
            if (lore != null && !lore.isEmpty()) {
                String line = lore.get(0);
                if (line.contains("§f")) {
                    return line.replace("§f", "").trim();
                }
            }
        }
        return "";
    }
    
    private String getSenderNameFromGUI(InventoryClickEvent event) {
        ItemStack item = event.getInventory().getItem(13);
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = item.getItemMeta().getLore();
            if (lore != null && !lore.isEmpty()) {
                String line = lore.get(0);
                if (line.contains("§f")) {
                    return line.replace("§f", "").trim();
                }
            }
        }
        return "";
    }
    }

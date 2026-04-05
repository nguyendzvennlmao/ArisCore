package me.aris.core.gui;

import me.aris.core.ArisCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class ConfirmGUI implements Listener {
    private ArisCore plugin;
    private String pendingDeleteHome = null;
    
    public ConfirmGUI(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    public void openConfirmGUI(Player player, String homeName) {
        pendingDeleteHome = homeName;
        Inventory gui = Bukkit.createInventory(null, 27, "§8ᴄᴏɴꜰʀɪᴍ");
        
        ItemStack homeInfo = new ItemStack(Material.BLUE_DYE);
        ItemMeta homeMeta = homeInfo.getItemMeta();
        homeMeta.setDisplayName("§bʜᴏᴍᴇ " + homeName);
        homeInfo.setItemMeta(homeMeta);
        gui.setItem(13, homeInfo);
        
        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§cᴄᴀɴᴄᴇʟ");
        cancelMeta.setLore(Arrays.asList("§fClick to cancel"));
        cancel.setItemMeta(cancelMeta);
        gui.setItem(11, cancel);
        
        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName("§aᴄᴏɴꜰʀɪᴍ");
        confirmMeta.setLore(Arrays.asList("§fClick to delete"));
        confirm.setItemMeta(confirmMeta);
        gui.setItem(15, confirm);
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals("§8ᴄᴏɴꜰʀɪᴍ")) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.equals("§aᴄᴏɴꜰʀɪᴍ")) {
            if (pendingDeleteHome != null) {
                player.performCommand("delhome " + pendingDeleteHome);
                pendingDeleteHome = null;
            }
            player.closeInventory();
        } else if (displayName.equals("§cᴄᴀɴᴄᴇʟ")) {
            pendingDeleteHome = null;
            player.closeInventory();
        }
    }
    }

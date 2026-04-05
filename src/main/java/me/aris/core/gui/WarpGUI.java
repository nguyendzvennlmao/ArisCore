package me.aris.core.gui;

import me.aris.core.ArisCore;
import me.aris.core.models.Warp;
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
import java.util.Map;

public class WarpGUI implements Listener {
    private ArisCore plugin;
    
    public WarpGUI(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    public void openWarpGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§8ᴡᴀʀᴘꜱ");
        
        Map<String, Warp> warps = plugin.getWarpManager().getWarps();
        int slot = 0;
        
        for (Map.Entry<String, Warp> entry : warps.entrySet()) {
            ItemStack warpItem = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = warpItem.getItemMeta();
            meta.setDisplayName("§6" + entry.getKey());
            meta.setLore(Arrays.asList("§7Click to teleport"));
            warpItem.setItemMeta(meta);
            gui.setItem(slot, warpItem);
            slot++;
            if (slot >= 45) break;
        }
        
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§cClose");
        close.setItemMeta(closeMeta);
        gui.setItem(49, close);
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals("§8ᴡᴀʀᴘꜱ")) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (clicked.hasItemMeta()) {
            String warpName = clicked.getItemMeta().getDisplayName().replace("§6", "");
            player.closeInventory();
            player.performCommand("warp " + warpName);
        }
    }
              }

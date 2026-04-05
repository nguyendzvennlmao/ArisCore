package me.aris.core.gui;

import me.aris.core.ArisCore;
import me.aris.core.models.Home;
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
import java.util.List;
import java.util.Map;

public class HomeGUI implements Listener {
    private ArisCore plugin;
    
    public HomeGUI(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    public void openHomeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, "§8ʜᴏᴍᴇꜱ");
        
        Map<String, Home> homes = plugin.getHomeManager().getHomes(player);
        int maxHomes = plugin.getHomeManager().getMaxHomes(player);
        
        List<Integer> bedSlots = Arrays.asList(11, 12, 13, 14, 15);
        List<Integer> dyeSlots = Arrays.asList(20, 21, 22, 23, 24);
        
        for (int i = 0; i < maxHomes && i < 5; i++) {
            int bedSlot = bedSlots.get(i);
            int dyeSlot = dyeSlots.get(i);
            
            if (i < homes.size()) {
                String homeName = (String) homes.keySet().toArray()[i];
                gui.setItem(bedSlot, createHomeBedItem(homeName, true));
                gui.setItem(dyeSlot, createHomeDyeItem(homeName, true));
            } else {
                gui.setItem(bedSlot, createHomeBedItem("not-set", false));
                gui.setItem(dyeSlot, createHomeDyeItem("not-set", false));
            }
        }
        
        player.openInventory(gui);
    }
    
    private ItemStack createHomeBedItem(String homeName, boolean hasHome) {
        ItemStack item;
        if (hasHome) {
            item = new ItemStack(Material.LIGHT_BLUE_BED);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§bʜᴏᴍᴇ " + homeName);
            meta.setLore(Arrays.asList("§fClick to teleport"));
            item.setItemMeta(meta);
        } else {
            item = new ItemStack(Material.LIGHT_GRAY_BED);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§7ɴᴏ ʜᴏᴍᴇ ꜱᴇᴛ");
            meta.setLore(Arrays.asList("§fClick to save your location"));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createHomeDyeItem(String homeName, boolean hasHome) {
        ItemStack item;
        if (hasHome) {
            item = new ItemStack(Material.BLUE_DYE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§bʜᴏᴍᴇ " + homeName);
            meta.setLore(Arrays.asList("§fClick to delete"));
            item.setItemMeta(meta);
        } else {
            item = new ItemStack(Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§7ɴᴏ ʜᴏᴍᴇ ꜱᴇᴛ");
            meta.setLore(Arrays.asList("§fClick to save your location"));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals("§8ʜᴏᴍᴇꜱ")) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.contains("ɴᴏ ʜᴏᴍᴇ ꜱᴇᴛ")) {
            player.closeInventory();
            player.performCommand("sethome");
        } else if (displayName.contains("ʜᴏᴍᴇ")) {
            String homeName = displayName.substring(displayName.indexOf("ʜᴏᴍᴇ") + 6).trim();
            
            if (clicked.getType() == Material.BLUE_DYE) {
                plugin.getConfirmGUI().openConfirmGUI(player, homeName);
            } else {
                player.closeInventory();
                player.performCommand("home " + homeName);
            }
        }
    }
  }

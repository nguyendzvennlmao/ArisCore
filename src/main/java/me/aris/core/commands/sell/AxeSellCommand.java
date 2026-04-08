package me.aris.core.commands.sell;

import me.aris.core.ArisCore;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;

public class AxeSellCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public AxeSellCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "sell");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.axsell")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "sell");
            return true;
        }
        
        boolean axeEnabled = plugin.getSellManager().isAxeEnabled();
        if (!axeEnabled) {
            plugin.getMessageManager().sendMessage(player, "axe-disabled", "sell");
            return true;
        }
        
        String materialName = plugin.getSellManager().getAxeMaterial();
        Material axeMaterial;
        try {
            axeMaterial = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            axeMaterial = Material.DIAMOND_AXE;
        }
        
        ItemStack axe = new ItemStack(axeMaterial);
        ItemMeta meta = axe.getItemMeta();
        meta.setDisplayName(plugin.getSellManager().getAxeName());
        List<String> lore = plugin.getSellManager().getAxeLore();
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }
        axe.setItemMeta(meta);
        
        player.getInventory().addItem(axe);
        plugin.getMessageManager().sendMessage(player, "axe-received", "sell");
        
        return true;
    }
              }

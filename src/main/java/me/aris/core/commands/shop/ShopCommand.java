package me.aris.core.commands.shop;

import me.aris.core.ArisCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    private ArisCore plugin;
    
    public ShopCommand(ArisCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(null, "player-only", "shop");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("ariscore.shop")) {
            plugin.getMessageManager().sendMessage(player, "no-permission", "shop");
            return true;
        }
        
        plugin.getShopGUI().openMainShop(player);
        
        return true;
    }
            }

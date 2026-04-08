package me.aris.core.sell.model;

import org.bukkit.Material;

public class SellItem {
    private Material material;
    private double price;
    private int amount;
    
    public SellItem(Material material, double price, int amount) {
        this.material = material;
        this.price = price;
        this.amount = amount;
    }
    
    public Material getMaterial() { return material; }
    public double getPrice() { return price; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
}

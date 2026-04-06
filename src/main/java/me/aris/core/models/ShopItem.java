package me.aris.core.models;

public class ShopItem {
    private String category;
    private String itemKey;
    private long price;
    private String materialName;
    private String displayName;
    private String command;
    private int amount;
    
    public ShopItem(String category, String itemKey, long price, String materialName, String displayName, String command, int amount) {
        this.category = category;
        this.itemKey = itemKey;
        this.price = price;
        this.materialName = materialName;
        this.displayName = displayName;
        this.command = command;
        this.amount = amount;
    }
    
    public String getCategory() { return category; }
    public String getItemKey() { return itemKey; }
    public long getPrice() { return price; }
    public String getMaterialName() { return materialName; }
    public String getDisplayName() { return displayName; }
    public String getCommand() { return command; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
  }

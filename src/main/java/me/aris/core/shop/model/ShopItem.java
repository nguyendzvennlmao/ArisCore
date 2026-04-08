package me.aris.core.shop.model;

public class ShopItem {
    private String category;
    private String itemKey;
    private long price;
    private String materialName;
    private String displayName;
    private String command;
    private int amount;
    private String currency;
    
    public ShopItem(String category, String itemKey, long price, String materialName, String displayName, String command, int amount, String currency) {
        this.category = category;
        this.itemKey = itemKey;
        this.price = price;
        this.materialName = materialName;
        this.displayName = displayName;
        this.command = command;
        this.amount = amount;
        this.currency = currency;
    }
    
    public String getCategory() { return category; }
    public String getItemKey() { return itemKey; }
    public long getPrice() { return price; }
    public String getMaterialName() { return materialName; }
    public String getDisplayName() { return displayName; }
    public String getCommand() { return command; }
    public int getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public void setAmount(int amount) { this.amount = amount; }
                           }

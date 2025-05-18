package com.OOP.model.entities;

import com.OOP.model.core.Room;
import com.OOP.model.items.Item;

import java.util.LinkedHashMap;
import java.util.Map;

public class Merchant extends NPC {
    private Map<Item, Integer> catalog; // Item -> Price

    public Merchant(String id, String name, String description, Room currentRoom) {
        super(id, name, description, 100, currentRoom, true); // Merchants are friendly
        this.catalog = new LinkedHashMap<>(); // LinkedHashMap to maintain insertion order
    }

    public void addToCatalog(Item item, int price) {
        catalog.put(item, price);
    }

    public void removeFromCatalog(Item item) {
        catalog.remove(item);
    }

    public Map<Item, Integer> getCatalog() {
        return catalog;
    }

    public Item getItemFromCatalog(String itemName) {
        for (Item item : catalog.keySet()) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return item;
            }
        }
        return null;
    }

    public Integer getPrice(Item item) {
        return catalog.get(item);
    }

    @Override
    public void interact(Player player) {
        System.out.println(getName() + " says: 'Welcome, adventurer! Care to see my wares?'");
        displayWares();
        System.out.println("You can 'buy <item_name>' or 'leave'.");
    }

    public void displayWares() {
        System.out.println("--- " + getName() + "'s Wares ---");
        if (catalog.isEmpty()) {
            System.out.println("Sorry, I'm all out of stock!");
        } else {
            for (Map.Entry<Item, Integer> entry : catalog.entrySet()) {
                System.out.println("- " + entry.getKey().getName() + " (" + entry.getKey().getDescription() + ") - " + entry.getValue() + " gold");
            }
        }
        System.out.println("----------------------");
    }

    public boolean sellToPlayer(String itemName, Player player) {
        Item itemToSell = getItemFromCatalog(itemName);
        if (itemToSell == null) {
            System.out.println(getName() + " says: 'I don't have " + itemName + ".'");
            return false;
        }
        Integer price = getPrice(itemToSell);
        if (player.buyItem(itemToSell, price, this)) { // Player handles gold, adds to inventory
            removeFromCatalog(itemToSell); // Remove from merchant's stock
            System.out.println(getName() + " says: 'A fine choice!'");
            return true;
        } else {
            // Player.buyItem would have printed "not enough gold"
            System.out.println(getName() + " says: 'Come back when you have more coin.'");
            return false;
        }
    }
}

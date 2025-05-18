package com.OOP.model.entities;

import com.OOP.model.core.Room;
import com.OOP.model.items.Gold;
import com.OOP.model.items.Item;
import com.OOP.model.items.Shield;
import com.OOP.model.items.Weapon;

import java.util.stream.Collectors;

public class Player extends LivingBeing {
    private int goldQuantity;

    public Player(String name, int healthPoints, int attackPower, Room currentRoom) {
        super("player_id", name, "The protagonist", healthPoints, attackPower, currentRoom);
        this.goldQuantity = 0;
    }

    public int getGoldQuantity() {
        return goldQuantity;
    }

    public void addGold(int amount) {
        this.goldQuantity += amount;
        System.out.println("You received " + amount + " gold. Total: " + goldQuantity);
    }

    public boolean spendGold(int amount) {
        if (this.goldQuantity >= amount) {
            this.goldQuantity -= amount;
            System.out.println("You spent " + amount + " gold. Remaining: " + goldQuantity);
            return true;
        }
        System.out.println("Not enough gold. You have " + goldQuantity + ", need " + amount + ".");
        return false;
    }

    @Override
    public void pickUpItem(Item item) {
        if (item instanceof Gold) {
            addGold(((Gold) item).getAmount());
            getCurrentRoom().removeItem(item.getName()); // Gold is consumed, not added to inventory
        } else {
            super.pickUpItem(item);
        }
    }

    public void viewInventory() {
        System.out.println("--- Inventory ---");
        if (getInventory().isEmpty()) {
            System.out.println("Empty.");
        } else {
            for (Item item : getInventory()) {
                System.out.println("- " + item.getName() + (item == getEquippedWeapon() || item == getEquippedShield() ? " (Equipped)" : ""));
            }
        }
        System.out.println("Gold: " + goldQuantity);
        System.out.println("Equipped Weapon: " + (getEquippedWeapon() != null ? getEquippedWeapon().getName() : "None"));
        System.out.println("Equipped Shield: " + (getEquippedShield() != null ? getEquippedShield().getName() : "None"));
        System.out.println("-----------------");
    }

    public boolean buyItem(Item item, int price, Merchant merchant) {
        if (spendGold(price)) {
            getInventory().add(item); // Add to player's inventory
            System.out.println("You bought " + item.getName() + ".");
            // Merchant should handle removing from its catalog internally
            return true;
        }
        return false;
    }

    // Sell item not fully implemented as per diagram (merchant doesn't have buy method explicitly)
    // but could be added:
    public void sellItem(Item item, int price, Merchant merchant) {
        if (getInventory().contains(item)) {
            getInventory().remove(item);
            addGold(price); // Typically sell for less than buy price
            System.out.println("You sold " + item.getName() + " for " + price + " gold.");
            merchant.addToCatalog(item, (int)(price * 1.5)); // Merchant buys it and might sell for more
        } else {
            System.out.println("You don't have " + item.getName() + ".");
        }
    }

    @Override
    public void die() {
        System.out.println("Wasted. Game Over.");
        // Game loop should check for player death and terminate
    }
}

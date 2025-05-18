package com.OOP.model.entities;

import com.OOP.model.core.Door;
import com.OOP.model.core.Room;
import com.OOP.model.items.Gold;
import com.OOP.model.items.Item;

import java.awt.*;

public class Player extends LivingBeing {
    private int goldQuantity;
    private int tileX;
    private int tileY;

    // Modify constructor
    public Player(String name, int healthPoints, int attackPower, Room currentRoom) {
        super("player_id", name, "The protagonist", healthPoints, attackPower, currentRoom);
        this.goldQuantity = 0;
        Point startPos = currentRoom.getPlayerStartPosition();
        this.tileX = startPos.x;
        this.tileY = startPos.y;
    }

    // Add getters and setters
    public int getTileX() {
        return tileX;
    }

    public void setTileX(int tileX) {
        this.tileX = tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public void setTileY(int tileY) {
        this.tileY = tileY;
    }

    // When changing rooms, update player's tile position
    // In com.yourgame.model.entities.Player.java

    @Override
    public void setCurrentRoom(Room newRoom) {
        Room oldRoom = this.getCurrentRoom(); // Get current room before changing
        if (this.getCurrentRoom() != null) {
            this.getCurrentRoom().removeLivingBeing(this);
        }
        super.setCurrentRoom(newRoom); // This sets this.currentRoom = newRoom and adds player to newRoom.livingBeings

        if (newRoom != null) {
            // Determine entry point into the new room
            Point entryPoint = null;
            if (oldRoom != null) {
                // Find the door that connects oldRoom and newRoom
                for (Door door : oldRoom.getExits().values()) {
                    if (door.getOppositeRoom(oldRoom) == newRoom) {
                        entryPoint = door.getEntryPointForRoom(newRoom);
                        break;
                    }
                }
                // If door not found from oldRoom's exits, check newRoom's exits (for bidirectional find)
                if (entryPoint == null) {
                    for (Door door : newRoom.getExits().values()) {
                        if (door.getOppositeRoom(newRoom) == oldRoom) {
                            entryPoint = door.getEntryPointForRoom(newRoom); // Still entry point *into* newRoom
                            break;
                        }
                    }
                }
            }

            if (entryPoint != null) {
                this.tileX = entryPoint.x;
                this.tileY = entryPoint.y;
            } else {
                // Fallback if no specific entry point is found (e.g., game start, or teleport)
                Point startPos = newRoom.getPlayerStartPosition(); // 'P' character in layout
                this.tileX = startPos.x;
                this.tileY = startPos.y;
                if (oldRoom != null) { // Log if this fallback is used during normal transition
                    System.err.println("Warning: Could not determine door entry point from " + oldRoom.getName() + " to " + newRoom.getName() + ". Using default 'P'.");
                }
            }
        }
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
    public void pickUpItem(Item item) { // Takes an Item object now
        if (item != null) {
            if (item instanceof Gold) {
                addGold(((Gold) item).getAmount());
                // Gold object doesn't go to inventory, it's just consumed
                // The room should have already removed it
                System.out.println(getName() + " picked up " + ((Gold) item).getAmount() + " gold value from " + item.getName() + ".");
            } else {
                inventory.add(item);
                System.out.println(getName() + " picked up " + item.getName() + ".");
            }
            // The item should be removed from the room's list by the calling logic
            // Example: currentRoom.removeItem(item); happens in GameKeyListener or MainGameLogicController
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
            merchant.addToCatalog(item, (int) (price * 1.5)); // Merchant buys it and might sell for more
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

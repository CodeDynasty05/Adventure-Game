package com.OOP.model.interactables;

import com.OOP.interfaces.Activatable;
import com.OOP.model.entities.Entity;
import com.OOP.model.entities.LivingBeing;
import com.OOP.model.items.Item;

import java.awt.Point; // Import Point
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Chest extends Entity implements Activatable {
    private Lock lock;
    private List<Item> contents;
    private boolean isOpen;
    private int tileX = -1; // Default to off-map
    private int tileY = -1;

    public Chest(String id, String name, String description, Lock lock) {
        super(id, name, description);
        this.lock = lock;
        this.contents = new ArrayList<>();
        this.isOpen = false;
    }

    // Constructor that includes tile coordinates
    public Chest(String id, String name, String description, Lock lock, int tileX, int tileY) {
        this(id, name, description, lock);
        this.tileX = tileX;
        this.tileY = tileY;
    }
    public Chest(String id, String name, String description, Lock lock, Point p) {
        this(id, name, description, lock);
        if (p != null) {
            this.tileX = p.x;
            this.tileY = p.y;
        }
    }


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

    public Point getTileCoordinates() {
        if (tileX == -1 || tileY == -1) return null;
        return new Point(tileX, tileY);
    }

    public void setTileCoordinates(int x, int y) {
        this.tileX = x;
        this.tileY = y;
    }

    public void setTileCoordinates(Point p) {
        if (p != null) {
            this.tileX = p.x;
            this.tileY = p.y;
        } else {
            this.tileX = -1;
            this.tileY = -1;
        }
    }


    public void addItem(Item item) {
        contents.add(item);
    }

    public List<Item> getContents() {
        return contents;
    }

    public List<Item> emptyChest() {
        List<Item> itemsTaken = new ArrayList<>(contents);
        contents.clear();
        return itemsTaken;
    }

    @Override
    public boolean isLocked() {
        return lock != null && lock.isLocked() && !isOpen;
    }

    public boolean isOpen() { return isOpen; }

    // ... activate() method remains the same ...
    @Override
    public boolean activate(LivingBeing activator, String itemIdToUse) {
        if (isOpen) {
            System.out.println("The " + getName() + " is already open.");
            return true;
        }
        if (lock == null || !lock.isLocked()) {
            isOpen = true;
            System.out.println("You open the " + getName() + ".");
            return true;
        }

        Item item = activator.getItemFromInventory(itemIdToUse);
        if (item == null && activator.hasItemByName("crowbar") && (itemIdToUse == null || "crowbar".equalsIgnoreCase(itemIdToUse))) {
            itemIdToUse = "crowbar";
        }


        if (itemIdToUse != null && itemIdToUse.equalsIgnoreCase("crowbar") && activator.hasItemByName("Rusty Crowbar")) { // Or get generic crowbar
            if (lock.tryUnlockWithCrowbar()) {
                isOpen = true;
                System.out.println("You pried open the " + getName() + " with the crowbar!");
                return true;
            } else {
                System.out.println("The crowbar is ineffective against this chest's lock.");
                return false;
            }
        } else if (lock.tryUnlock(item)) { // Pass the key item
            isOpen = true;
            System.out.println("You unlocked and opened the " + getName() + ".");
            return true;
        } else {
            System.out.println("You can't open the " + getName() + ". It's locked or you lack the right tool/key.");
            return false;
        }
    }


    @Override
    public String getDescription() {
        String status;
        if (isOpen) status = " (Open" + (contents.isEmpty() ? ", empty" : "") + ")";
        else if (isLocked()) status = " (Locked)";
        else status = " (Closed)";
        return super.getDescription() + status;
    }
}
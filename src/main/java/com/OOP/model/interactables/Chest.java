package com.OOP.model.interactables;

import com.OOP.interfaces.Activatable;
import com.OOP.model.entities.Entity;
import com.OOP.model.entities.LivingBeing;
import com.OOP.model.items.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Chest extends Entity implements Activatable {
    private Lock lock; // Can be null if not locked
    private List<Item> contents;
    private boolean isOpen;

    public Chest(String id, String name, String description, Lock lock) {
        super(id, name, description);
        this.lock = lock;
        this.contents = new ArrayList<>();
        this.isOpen = false;
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

    @Override
    public boolean activate(LivingBeing activator, String itemIdToUse) { // itemIdToUse = key ID or "crowbar"
        if (isOpen) {
            System.out.println("The " + getName() + " is already open.");
            return true;
        }
        if (lock == null || !lock.isLocked()) { // Not locked or already unlocked by other means
            isOpen = true;
            System.out.println("You open the " + getName() + ".");
            return true;
        }

        // If locked
        Item item = activator.getItemFromInventory(itemIdToUse); // Try to find specific item
        if (item == null && activator.hasItemByName("crowbar") && (itemIdToUse == null || "crowbar".equalsIgnoreCase(itemIdToUse))) {
            // If no specific item or "crowbar" was typed, and player has a crowbar
            itemIdToUse = "crowbar"; // Tentatively try the crowbar
        }


        if (lock.tryUnlock(item)) { // Pass the key item
            isOpen = true;
            System.out.println("You unlocked and opened the " + getName() + ".");
            return true;
        } else if ("crowbar".equalsIgnoreCase(itemIdToUse) && activator.hasItemByName("crowbar")) {
            if (lock.tryUnlockWithCrowbar()) {
                isOpen = true;
                System.out.println("You pried open the " + getName() + " with the crowbar!");
                return true;
            } else {
                System.out.println("The crowbar is ineffective against this chest's lock.");
                return false;
            }
        } else {
            System.out.println("You can't open the " + getName() + ". It's locked.");
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

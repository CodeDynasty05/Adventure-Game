package com.OOP.model.core;

import com.OOP.interfaces.Activatable;
import com.OOP.model.entities.Entity;
import com.OOP.model.entities.LivingBeing;
import com.OOP.model.interactables.Lock;
import com.OOP.model.items.Item;

public class Door extends Entity implements Activatable {
    private Room room1;
    private Room room2;
    private Lock lock; // Can be null if not locked

    public Door(String id, String name, String description, Room room1, Room room2, Lock lock) {
        super(id, name, description);
        this.room1 = room1;
        this.room2 = room2;
        this.lock = lock;
    }

    public Room getOppositeRoom(Room currentRoom) {
        if (currentRoom == room1) return room2;
        if (currentRoom == room2) return room1;
        return null; // Should not happen if logic is correct
    }

    @Override
    public boolean isLocked() {
        return lock != null && lock.isLocked();
    }

    @Override
    public boolean activate(LivingBeing activator, String itemIdToUse) {
        System.out.println("DEBUG: "+ itemIdToUse);
        if (lock == null) { // Not locked
            System.out.println("The " + getName() + " is already unlocked.");
            return true;
        }
        if (lock.isLocked()) {
            Item item = activator.getItemFromInventory(itemIdToUse);
            if (item == null && !"crowbar".equalsIgnoreCase(itemIdToUse) && (activator.hasItemByName("crowbar") && itemIdToUse == null)) {
                // Try crowbar if no specific item given and player has one
                itemIdToUse = "crowbar";
            }


            if (lock.tryUnlock(item)) { // Pass the item (key) or null (for crowbar check)
                System.out.println("You unlocked the " + getName() + ".");
                // lock.unlock(); // tryUnlock should handle this
                return true;
            } else if ("crowbar".equalsIgnoreCase(itemIdToUse) && activator.hasItemByName("crowbar")) {
                if (lock.tryUnlockWithCrowbar()) {
                    System.out.println("You pried open the " + getName() + " with the crowbar!");
                    return true;
                } else {
                    System.out.println("The crowbar is ineffective against this lock.");
                    return false;
                }
            } else {
                System.out.println("You can't unlock the " + getName() + " with that.");
                return false;
            }
        }
        System.out.println("The " + getName() + " is already unlocked.");
        return true; // Already unlocked
    }

    @Override
    public String getDescription() { // Overriding Entity's getDescription for more dynamic info
        return super.getDescription() + (isLocked() ? " (Locked)" : " (Unlocked)");
    }
}

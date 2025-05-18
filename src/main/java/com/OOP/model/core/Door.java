package com.OOP.model.core; // Or com.OOP.model.core

import com.OOP.interfaces.Activatable;
import com.OOP.model.entities.Entity;
import com.OOP.model.entities.LivingBeing;
import com.OOP.model.interactables.Lock;
import com.OOP.model.items.Crowbar;
import com.OOP.model.items.Item;
import com.OOP.model.items.Key;

import java.awt.Point; // << IMPORT THIS

public class Door extends Entity implements Activatable {
    private Room room1;
    private Room room2;
    private Lock lock;
    private Point entryPointToRoom2; // Tile coordinates to place player in room2 when coming from room1 via this door
    private Point entryPointToRoom1; // Tile coordinates to place player in room1 when coming from room2 via this door
    private Point tileCoordinates; // << NEW FIELD: Stores the door's position on the map in room1's context (or a defined context)
    private Point visualTileInRoom1;
    private Point visualTileInRoom2;

    // Original Constructor (can keep for compatibility or remove if all doors will have coords)
    public Door(String id, String name,Point doorTileInRoom1 ,String description, Room room1, Room room2, Lock lock,
                Point visualTileInRoom1, Point entryToRoom2,
                Point visualTileInRoom2, Point entryToRoom1) {
        super(id, name, description);
        this.room1 = room1;
        this.room2 = room2;
        this.lock = lock;
        this.tileCoordinates = doorTileInRoom1; // Position of the door tile itself in room1's map
        this.visualTileInRoom1 = visualTileInRoom1;
        this.entryPointToRoom2 = entryToRoom2;
        this.visualTileInRoom2 = visualTileInRoom2;
        this.entryPointToRoom1 = entryToRoom1;
        // The above fallback for entryPointToRoom1 might be problematic if room2.getPlayerStartPosition() isn't near its side of the door
    }

    // NEW/MODIFIED Constructor with tileCoordinates
    public Door(String id, String name, String description, Room room1, Room room2, Lock lock, Point tileCoordinates) {
        super(id, name, description);
        this.room1 = room1;
        this.room2 = room2;
        this.lock = lock;
        this.tileCoordinates = tileCoordinates; // << ASSIGN THE COORDINATES
    }

    public Point getVisualTileForRoom(Room room) {
        if (room == room1) return visualTileInRoom1;
        if (room == room2) return visualTileInRoom2;
        return null;
    }


    public Room getOppositeRoom(Room currentRoom) {
        if (currentRoom == room1) return room2;
        if (currentRoom == room2) return room1;
        return null;
    }

    public Point getTileCoordinates() { // << NEW GETTER
        return tileCoordinates;
    }

    // It might be useful to know which room these coordinates refer to,
    // or have separate coordinates if the door spans multiple tiles or appears
    // at different coordinates in room2's layout.
    // For simplicity, we assume tileCoordinates are for its primary representation in one room.

    @Override
    public boolean isLocked() {
        return lock != null && lock.isLocked();
    }

    // ... activate() method and other methods remain the same ...
    @Override
    public boolean activate(LivingBeing activator, String itemIdToUseIdentifier) {
        if (lock == null) {
            System.out.println("The " + getName() + " is already unlocked.");
            // Potentially update the tile map representation here if this door changes a 'D' to a '.'
            // This logic is better handled by the GamePanel when it re-renders and checks door state.
            return true;
        }

        if (!lock.isLocked()) {
            System.out.println("The " + getName() + " is already unlocked.");
            return true;
        }

        if (itemIdToUseIdentifier != null) {
            if (itemIdToUseIdentifier.equalsIgnoreCase("crowbar")) {
                Item crowbarInInventory = activator.getInventory().stream()
                        .filter(i -> i instanceof Crowbar)
                        .findFirst().orElse(null);
                if (crowbarInInventory != null) {
                    if (lock.tryUnlockWithCrowbar()) {
                        System.out.println("You pried open the " + getName() + " with the crowbar!");
                        // GamePanel will visually update the door on next repaint
                        return true;
                    } else {
                        System.out.println("The crowbar is ineffective against this lock.");
                        return false;
                    }
                } else {
                    System.out.println("You don't have a crowbar to use.");
                    return false;
                }
            } else {
                Item keyItem = activator.getItemByIdFromInventory(itemIdToUseIdentifier);
                if (keyItem instanceof Key) {
                    if (lock.tryUnlock(keyItem)) {
                        System.out.println("You unlocked the " + getName() + " with the " + keyItem.getName() + ".");
                        // GamePanel will visually update the door on next repaint
                        return true;
                    } else {
                        System.out.println("The " + keyItem.getName() + " doesn't fit this lock.");
                        return false;
                    }
                } else {
                    System.out.println("You can't unlock the " + getName() + " with that item or the item is not a key.");
                    return false;
                }
            }
        } else {
            System.out.println("The " + getName() + " is locked. You might need a key or a tool.");
            return false;
        }
    }

    @Override
    public String getDescription() {
        return super.getDescription() + (isLocked() ? " (Locked)" : " (Unlocked)");
    }

    public Point getEntryPointForRoom(Room targetRoom) {
        if (targetRoom == room2) {
            return entryPointToRoom2;
        } else if (targetRoom == room1) {
            return entryPointToRoom1;
        }
        return null; // Should not happen
    }
}
package com.OOP.model.interactables;

import com.OOP.model.entities.Entity;
import com.OOP.model.items.Item;
import com.OOP.model.items.Key;
import com.OOP.model.items.Crowbar;

import java.util.ArrayList;
import java.util.List;

public class Lock extends Entity {
    private List<String> acceptableKeyIds;
    private boolean isLocked;
    private boolean canBePriedOpen; // Can a crowbar open this?

    public Lock(String id, String description, boolean initiallyLocked) {
        super(id, "Lock", description); // Name is generic "Lock"
        this.acceptableKeyIds = new ArrayList<>();
        this.isLocked = initiallyLocked;
        this.canBePriedOpen = true; // Default to yes, can be set otherwise
    }

    public Lock(String id, String description, boolean initiallyLocked, boolean canBePriedOpen) {
        this(id, description, initiallyLocked);
        this.canBePriedOpen = canBePriedOpen;
    }

    public void addAcceptableKeyId(String keyId) {
        this.acceptableKeyIds.add(keyId);
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void unlock() {
        this.isLocked = false;
        setDescription(getDescription().replace(" (Locked)", "") + " (Unlocked)");
    }

    public void lock() {
        this.isLocked = true;
        setDescription(getDescription().replace(" (Unlocked)", "") + " (Locked)");
    }

    public boolean tryUnlock(Item itemUsed) {
        if (!isLocked) return true; // Already unlocked

        if (itemUsed instanceof Key) {
            Key key = (Key) itemUsed;
            if (acceptableKeyIds.contains(key.getId())) {
                unlock();
                return true;
            }
        }
        return false;
    }

    public boolean tryUnlockWithCrowbar() {
        if (!isLocked) return true;
        if (canBePriedOpen) {
            unlock();
            return true;
        }
        return false;
    }
}

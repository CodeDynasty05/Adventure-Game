package com.OOP.model.entities;

import com.OOP.model.core.Room;

public abstract class NPC extends LivingBeing {
    private boolean isFriendly;

    public NPC(String id, String name, String description, int healthPoints, Room currentRoom, boolean isFriendly) {
        super(id, name, description, healthPoints, 0, currentRoom); // NPCs typically don't attack unless provoked
        this.isFriendly = isFriendly;
    }

    public boolean isFriendly() {
        return isFriendly;
    }

    public void setFriendly(boolean friendly) {
        isFriendly = friendly;
    }

    public abstract void interact(Player player); // Specific interaction logic
}

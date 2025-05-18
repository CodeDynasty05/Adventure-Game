package com.OOP.model.entities;

import com.OOP.model.core.Room;
import com.OOP.model.items.Item;

public class Enemy extends LivingBeing {
    private Item loot; // Simple loot system: one item or null

    public Enemy(String id, String name, String description, int healthPoints, int attackPower, Room currentRoom, Item loot) {
        super(id, name, description, healthPoints, attackPower, currentRoom);
        this.loot = loot;
    }

    @Override
    public void die() {
        super.die(); // Basic die behavior (drops inventory if any, removes from room)
        if (loot != null && getCurrentRoom() != null) {
            getCurrentRoom().addItem(loot);
            System.out.println(getName() + " dropped " + loot.getName() + "!");
        }
    }
}

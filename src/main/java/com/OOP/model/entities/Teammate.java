package com.OOP.model.entities;

import com.OOP.model.core.Room;

public class Teammate extends LivingBeing {
    public Teammate(String id, String name, String description, int healthPoints, int attackPower, Room currentRoom) {
        super(id, name, description, healthPoints, attackPower, currentRoom);
    }

    // Teammates might follow the player or assist in combat.
    // For now, they are just LivingBeings that can be in a room.
    // AI for combat/following is more complex.
    public void assist(LivingBeing target) {
        if (target.getHealthPoints() > 0) {
            System.out.println(getName() + " assists in attacking " + target.getName() + "!");
            attack(target);
        }
    }
}

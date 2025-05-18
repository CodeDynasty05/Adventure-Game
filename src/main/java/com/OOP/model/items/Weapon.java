package com.OOP.model.items;

// Note: The 'use()' from UML for Weapon/Shield is interpreted as 'equip'
// The Executable interface is more for one-shot uses.
// Equipping is handled by LivingBeing.
public class Weapon extends Item {
    private int damage;

    public Weapon(String id, String name, String description, int damage) {
        super(id, name, description);
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    // The "use" of a weapon is being equipped and then its damage adding to player's attack.
    // If Executable was meant for an "attack action", that's handled by LivingBeing.attack()
}

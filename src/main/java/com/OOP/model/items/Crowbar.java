package com.OOP.model.items;

// Crowbar is special, it's used ON locks.
// It doesn't quite fit Executable in the same way as Medicine/Weapon.
// Its "execution" is handled by the Lock or Activatable (Door/Chest) logic.
public class Crowbar extends Item {
    public Crowbar(String id, String name, String description) {
        super(id, name, description);
    }
}

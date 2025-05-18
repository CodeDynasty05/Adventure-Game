package com.OOP.model.items;

// Similar to Weapon, "use" is equipping it.
public class Shield extends Item {
    private int blockValue; // Amount of damage it reduces

    public Shield(String id, String name, String description, int blockValue) {
        super(id, name, description);
        this.blockValue = blockValue;
    }

    public int getBlockValue() {
        return blockValue;
    }
}

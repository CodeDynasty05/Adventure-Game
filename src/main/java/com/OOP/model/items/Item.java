package com.OOP.model.items;

import com.OOP.model.entities.Entity;

public abstract class Item extends Entity {
    public Item(String id, String name, String description) {
        super(id, name, description);
    }
}

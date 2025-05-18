package com.OOP.model.items;

public class Key extends Item {
    private String opensLockId; // The ID of the Lock this key opens

    public Key(String id, String name, String description, String opensLockId) {
        super(id, name, description);
        this.opensLockId = opensLockId;
    }

    public String getOpensLockId() {
        return opensLockId;
    }
}

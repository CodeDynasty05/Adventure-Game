package com.OOP.model.items;

public class Gold extends Item {
    private int amount;

    public Gold(String id, String name, String description, int amount) {
        super(id, name, description);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}

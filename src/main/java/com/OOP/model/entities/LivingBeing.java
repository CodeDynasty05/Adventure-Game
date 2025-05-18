package com.OOP.model.entities;

import com.OOP.model.core.Room;
import com.OOP.model.items.Item;
import com.OOP.model.items.Weapon;
import com.OOP.model.items.Shield;

import java.util.ArrayList;
import java.util.List;

public abstract class LivingBeing extends Entity {
    private int healthPoints;
    private int maxHealthPoints;
    private int attackPower;
    private Room currentRoom;
    private List<Item> inventory;
    private Weapon equippedWeapon;
    private Shield equippedShield;


    public LivingBeing(String id, String name, String description, int healthPoints, int attackPower, Room currentRoom) {
        super(id, name, description);
        this.healthPoints = healthPoints;
        this.maxHealthPoints = healthPoints;
        this.attackPower = attackPower;
        this.currentRoom = currentRoom;
        this.inventory = new ArrayList<>();
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public int getMaxHealthPoints() {
        return maxHealthPoints;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = Math.max(0, healthPoints);
        if (this.healthPoints > this.maxHealthPoints) this.healthPoints = this.maxHealthPoints;
    }

    public int getAttackPower() {
        int totalAttack = attackPower;
        if (equippedWeapon != null) {
            totalAttack += equippedWeapon.getDamage();
        }
        return totalAttack;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        if (this.currentRoom != null) {
            this.currentRoom.removeLivingBeing(this);
        }
        this.currentRoom = currentRoom;
        if (this.currentRoom != null) {
            this.currentRoom.addLivingBeing(this);
        }
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public void pickUpItem(Item item) {
        if (item != null) {
            inventory.add(item);
            currentRoom.removeItem(item.getName()); // remove by name, assumes unique names in room for simplicity
            System.out.println(getName() + " picked up " + item.getName() + ".");
        }
    }

    public Item dropItem(String itemName) {
        Item itemToDrop = getItemFromInventory(itemName);
        if (itemToDrop != null) {
            inventory.remove(itemToDrop);
            currentRoom.addItem(itemToDrop);
            if (itemToDrop == equippedWeapon) unequipWeapon();
            if (itemToDrop == equippedShield) unequipShield();
            System.out.println(getName() + " dropped " + itemToDrop.getName() + ".");
            return itemToDrop;
        }
        System.out.println(getName() + " doesn't have " + itemName + ".");
        return null;
    }

    public boolean hasItemByName(String itemName) {
        return inventory.stream().anyMatch(item -> item.getName().toLowerCase().contains(itemName.toLowerCase()));
    }

    public Item getItemFromInventory(String itemName) {
        if (itemName == null) return null;
        for (Item item : inventory) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return item;
            }
        }
        return null; // Not found
    }

    public Item getItemByIdFromInventory(String itemId) {
        if (itemId == null) return null;
        for (Item item : inventory) {
            if (item.getId().equalsIgnoreCase(itemId)) {
                return item;
            }
        }
        return null;
    }


    public void takeDamage(int damage) {
        int actualDamage = damage;
        if (equippedShield != null) {
            actualDamage = Math.max(0, damage - equippedShield.getBlockValue());
            if (actualDamage < damage) {
                System.out.println(getName() + "'s shield absorbed " + (damage - actualDamage) + " damage!");
            }
        }
        this.healthPoints -= actualDamage;
        System.out.println(getName() + " takes " + actualDamage + " damage.");
        if (this.healthPoints <= 0) {
            die();
        }
    }

    public void attack(LivingBeing target) {
        if (target.getHealthPoints() <= 0) {
            System.out.println(target.getName() + " is already defeated.");
            return;
        }
        System.out.println(getName() + " attacks " + target.getName() + "!");
        target.takeDamage(getAttackPower());
    }

    public void die() {
        System.out.println(getName() + " has been defeated!");
        if (this.currentRoom != null) {
            this.currentRoom.removeLivingBeing(this);
            // Drop all inventory on death
            for (Item item : new ArrayList<>(inventory)) { // Iterate copy to avoid ConcurrentModificationException
                dropItem(item.getName()); // This will add it to the room
            }
        }
        // Further logic (e.g., game over for player) handled in game loop
    }

    public void equipWeapon(Weapon weapon) {
        if (inventory.contains(weapon)) {
            this.equippedWeapon = weapon;
            System.out.println(getName() + " equipped " + weapon.getName() + ".");
        } else {
            System.out.println(getName() + " does not have " + weapon.getName() + " in inventory.");
        }
    }

    public void unequipWeapon() {
        if (this.equippedWeapon != null) {
            System.out.println(getName() + " unequipped " + this.equippedWeapon.getName() + ".");
            this.equippedWeapon = null;
        }
    }

    public Weapon getEquippedWeapon() { return equippedWeapon; }

    public void equipShield(Shield shield) {
        if (inventory.contains(shield)) {
            this.equippedShield = shield;
            System.out.println(getName() + " equipped " + shield.getName() + ".");
        } else {
            System.out.println(getName() + " does not have " + shield.getName() + " in inventory.");
        }
    }

    public void unequipShield() {
        if (this.equippedShield != null) {
            System.out.println(getName() + " unequipped " + this.equippedShield.getName() + ".");
            this.equippedShield = null;
        }
    }
    public Shield getEquippedShield() { return equippedShield; }

}

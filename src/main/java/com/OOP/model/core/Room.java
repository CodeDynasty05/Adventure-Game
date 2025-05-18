package com.OOP.model.core;

import com.OOP.model.entities.Entity;
import com.OOP.model.entities.LivingBeing;
import com.OOP.model.entities.Player;
import com.OOP.model.items.Item;
import com.OOP.model.interactables.Chest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Room extends Entity {
    private Map<String, Door> exits; // Direction -> Door
    private List<Item> itemsOnFloor;
    private List<LivingBeing> livingBeings;
    private List<Chest> chests;
    // isLocked seems to be a property of Door/Chest, not Room itself from diagram

    public Room(String id, String name, String description) {
        super(id, name, description);
        this.exits = new HashMap<>();
        this.itemsOnFloor = new ArrayList<>();
        this.livingBeings = new ArrayList<>();
        this.chests = new ArrayList<>();
    }

    public void addExit(String direction, Door door) {
        exits.put(direction.toLowerCase(), door);
    }

    public Door getExit(String direction) {
        return exits.get(direction.toLowerCase());
    }

    public Map<String, Door> getExits() {
        return exits;
    }

    public void addItem(Item item) {
        itemsOnFloor.add(item);
    }

    public Item removeItem(String itemName) {
        Item itemToRemove = null;
        for (Item item : itemsOnFloor) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                itemToRemove = item;
                break;
            }
        }
        if (itemToRemove != null) {
            itemsOnFloor.remove(itemToRemove);
        }
        return itemToRemove;
    }

    public List<Item> getItemsOnFloor() {
        return itemsOnFloor;
    }

    public void addLivingBeing(LivingBeing being) {
        livingBeings.add(being);
    }

    public void removeLivingBeing(LivingBeing being) {
        livingBeings.remove(being);
    }

    public List<LivingBeing> getLivingBeings() {
        return livingBeings;
    }

    public LivingBeing getLivingBeingByName(String name) {
        for (LivingBeing lb : livingBeings) {
            if (lb.getName().equalsIgnoreCase(name) && !(lb instanceof Player)) { // Don't target self by name usually
                return lb;
            }
        }
        return null;
    }

    public void addChest(Chest chest) {
        chests.add(chest);
    }

    public Chest getChestByName(String name) {
        for (Chest chest : chests) {
            if (chest.getName().equalsIgnoreCase(name)) {
                return chest;
            }
        }
        return null;
    }

    public List<Chest> getChests() { return chests; }

    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are in ").append(getName()).append(". ").append(getDescription()).append("\n");

        if (!itemsOnFloor.isEmpty()) {
            sb.append("You see on the floor: ");
            sb.append(itemsOnFloor.stream().map(Item::getName).collect(Collectors.joining(", "))).append(".\n");
        }

        if (!chests.isEmpty()) {
            sb.append("Chests: ");
            sb.append(chests.stream().map(Chest::getName).collect(Collectors.joining(", "))).append(".\n");
        }

        List<LivingBeing> others = livingBeings.stream()
                .filter(lb -> !(lb instanceof Player))
                .collect(Collectors.toList());
        if (!others.isEmpty()) {
            sb.append("Others here: ");
            sb.append(others.stream().map(LivingBeing::getName).collect(Collectors.joining(", "))).append(".\n");
        }

        sb.append("Exits: ");
        if (exits.isEmpty()) {
            sb.append("None.");
        } else {
            sb.append(exits.keySet().stream().map(String::toUpperCase).collect(Collectors.joining(", ")));
        }
        sb.append("\n");
        return sb.toString();
    }
}

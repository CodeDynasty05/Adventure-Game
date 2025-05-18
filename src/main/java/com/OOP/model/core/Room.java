package com.OOP.model.core;

import com.OOP.model.entities.Entity;
import com.OOP.model.entities.LivingBeing;
import com.OOP.model.entities.Player;
import com.OOP.model.items.Item;
import com.OOP.model.interactables.Chest;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Room extends Entity {
    private Map<String, Door> exits; // Direction -> Door
    private List<Item> itemsOnFloor;
    private List<LivingBeing> livingBeings;
    private static final Random random = new Random();
    private List<Chest> chests;
    private String[] tileMapLayout; // Example: {"WWWWW", "WFPFW", "W###W", "WDFEW", "WWWWW"}
    // W=Wall, F=Floor, P=PlayerStart, D=Door, E=Enemy, C=Chest, K=Key
    public static final int TILE_SIZE = 48; // Or whatever your tile PNGs are sized at
    // isLocked seems to be a property of Door/Chest, not Room itself from diagram

    public Room(String id, String name, String description, String[] tileMapLayout) {
        super(id, name, description);
        this.exits = new HashMap<>();
        this.itemsOnFloor = new ArrayList<>();
        this.livingBeings = new ArrayList<>();
        this.chests = new ArrayList<>();
        this.tileMapLayout = tileMapLayout; // New
    }

    public String[] getTileMapLayout() {
        return tileMapLayout;
    }

    public int getMapWidth() {
        return tileMapLayout != null && tileMapLayout.length > 0 ? tileMapLayout[0].length() : 0;
    }

    public int getMapHeight() {
        return tileMapLayout != null ? tileMapLayout.length : 0;
    }

    // Method to get player starting position from map layout (optional, or pass to player)
    public Point getPlayerStartPosition() {
        if (tileMapLayout == null) return new Point(1,1); // Default
        for (int row = 0; row < tileMapLayout.length; row++) {
            for (int col = 0; col < tileMapLayout[row].length(); col++) {
                if (tileMapLayout[row].charAt(col) == 'P') {
                    return new Point(col, row);
                }
            }
        }
        return new Point(1,1); // Fallback if 'P' not found
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
        Point randomFloorTile = findRandomWalkableFloorTile();
        if (randomFloorTile != null) {
            // Check if another item is already at this spot (optional, could allow stacking)
            boolean spotOccupiedByItem = itemsOnFloor.stream()
                    .anyMatch(i -> i.getTileX() == randomFloorTile.x && i.getTileY() == randomFloorTile.y);

            if (!spotOccupiedByItem) {
                item.setTileCoordinates(randomFloorTile.x, randomFloorTile.y);
                itemsOnFloor.add(item);
            } else {
                // Handle collision: try again, or place nearby, or log warning
                System.err.println("Warning: Could not find free spot for item " + item.getName() + " in " + getName() + ". Trying again (simple)...");
                Point anotherSpot = findRandomWalkableFloorTile(randomFloorTile); // Try to find different spot
                if (anotherSpot != null && !anotherSpot.equals(randomFloorTile)) {
                    item.setTileCoordinates(anotherSpot.x, anotherSpot.y);
                    itemsOnFloor.add(item);
                } else {
                    System.err.println("Failed to place item " + item.getName() + " randomly after retry.");
                    // Fallback: could add to a default spot or not add at all
                }
            }
        } else {
            System.err.println("Warning: Could not find any walkable floor tile to place item " + item.getName() + " in " + getName());
            // Potentially add to player inventory directly or drop at a default (0,0) which is bad
        }
    }

    // Helper to find a random walkable floor tile
    public Point findRandomWalkableFloorTile() {
        return findRandomWalkableFloorTile(null);
    }
    private Point findRandomWalkableFloorTile(Point excludeThisPoint) {
        if (tileMapLayout == null) return null;
        List<Point> floorTiles = new ArrayList<>();
        for (int r = 0; r < getMapHeight(); r++) {
            for (int c = 0; c < getMapWidth(); c++) {
                char tileChar = tileMapLayout[r].charAt(c);
                // Consider 'F' (Floor) and '.' (Open Door) as walkable for items
                if (tileChar == 'F' || tileChar == '.') {
                    Point currentPoint = new Point(c,r);
                    if (excludeThisPoint == null || !excludeThisPoint.equals(currentPoint)) {
                        // Also ensure no entity (player, NPC, chest) is at this exact spot for item placement clarity
                        int finalC = c;
                        int finalR = r;
                        int finalC1 = c;
                        int finalR1 = r;
                        boolean entityAtSpot = livingBeings.stream().anyMatch(lb -> lb instanceof Player ? ((Player)lb).getTileX() == finalC1 && ((Player)lb).getTileY() == finalR1 : false) || // Crude check for player
                                chests.stream().anyMatch(ch -> ch.getTileX() == finalC && ch.getTileY() == finalR); // Assuming Chest has tileX/Y

                        if (!entityAtSpot) {
                            floorTiles.add(currentPoint);
                        }
                    }
                }
            }
        }
        if (floorTiles.isEmpty()) return null;
        return floorTiles.get(random.nextInt(floorTiles.size()));
    }


    // Get item at specific tile coordinates
    public Item getItemAt(int tileX, int tileY) {
        for (Item item : itemsOnFloor) {
            if (item.getTileX() == tileX && item.getTileY() == tileY) {
                return item;
            }
        }
        return null;
    }

    // Overload or change removeItem to remove a specific Item object
    public boolean removeItem(Item itemToRemove) {
        if (itemToRemove != null) {
            itemToRemove.setTileCoordinates(-1,-1); // Mark as off-map
            return itemsOnFloor.remove(itemToRemove);
        }
        return false;
    }

    // Keep the old removeItem by name for compatibility with text commands if needed,
    // but it won't know the item's location for graphical removal unless updated.
    public Item removeItem(String itemName) { // This is now less useful for graphical take
        Item itemToRemove = null;
        for (Item item : itemsOnFloor) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                itemToRemove = item;
                break;
            }
        }
        if (removeItem(itemToRemove)) { // Call the object-based remove
            return itemToRemove;
        }
        return null;
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

    /**
     * Adds a chest to the room. If location is specified, it's placed there.
     * Otherwise, it tries to place it at a 'C' marker in the tileMapLayout,
     * or on a random walkable floor tile if 'C' is not found or occupied.
     *
     * @param chest The Chest object to add.
     * @param location The specific Point (tile coordinates) to place the chest, or null for default placement.
     */
    public void addChest(Chest chest, Point location) {
        Point placementLocation = location;

        if (placementLocation == null) {
            // Default placement logic: Try 'C' then random floor tile
            placementLocation = findCharInLayout('C');
            if (placementLocation == null) {
                placementLocation = findRandomWalkableFloorTile();
            }
        }

        if (placementLocation != null) {
            // Check if the chosen spot is already occupied by another chest
            Point finalPlacementLocation = placementLocation;
            boolean spotOccupiedByChest = this.chests.stream()
                    .anyMatch(existingChest -> existingChest.getTileX() == finalPlacementLocation.x &&
                            existingChest.getTileY() == finalPlacementLocation.y);

            if (!spotOccupiedByChest) {
                chest.setTileCoordinates(placementLocation);
                this.chests.add(chest);
            } else {
                System.err.println("Warning: Chest spot " + placementLocation + " is already occupied. Cannot place " + chest.getName() + " in " + getName());
                // Optionally, try to find another random spot if the default 'C' was taken
                if (location == null) { // Only retry if it was default placement
                    Point alternativeSpot = findRandomWalkableFloorTile(placementLocation); // Exclude the occupied spot
                    if (alternativeSpot != null) {
                        boolean altSpotOccupied = this.chests.stream()
                                .anyMatch(existingChest -> existingChest.getTileX() == alternativeSpot.x &&
                                        existingChest.getTileY() == alternativeSpot.y);
                        if (!altSpotOccupied) {
                            chest.setTileCoordinates(alternativeSpot);
                            this.chests.add(chest);
                            System.out.println("Placed " + chest.getName() + " at alternative spot: " + alternativeSpot);
                        } else {
                            System.err.println("Alternative spot also occupied for " + chest.getName());
                        }
                    } else {
                        System.err.println("No alternative spot found for " + chest.getName());
                    }
                }
            }
        } else {
            System.err.println("Warning: Could not determine a placement location for chest " + chest.getName() + " in " + getName());
        }
    }

    /**
     * Adds a chest to the room using default placement logic (tries 'C' in layout, then random floor).
     * @param chest The Chest object to add.
     */
    public void addChest(Chest chest) {
        addChest(chest, null); // Calls the more specific method with location as null
    }


    public Chest getChestAt(int tileX, int tileY) {
        for (Chest chest : chests) {
            if (chest.getTileX() == tileX && chest.getTileY() == tileY) {
                return chest;
            }
        }
        return null;
    }

    // Helper to find a character in the layout (already exists or should exist)
    private Point findCharInLayout(char targetChar) {
        if (tileMapLayout == null) return null;
        for (int row = 0; row < tileMapLayout.length; row++) {
            for (int col = 0; col < tileMapLayout[row].length(); col++) {
                if (tileMapLayout[row].charAt(col) == targetChar) {
                    return new Point(col, row);
                }
            }
        }
        return null;
    }
}

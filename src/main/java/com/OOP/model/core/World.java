package com.OOP.model.core; // Or com.OOP.model.core if that's your package

import com.OOP.model.entities.*;
import com.OOP.model.interactables.Chest;
import com.OOP.model.interactables.Lock;
import com.OOP.model.items.*;
// Ensure Point is imported if you use it for door coordinates
import java.awt.Point;


import java.util.HashMap;
import java.util.Map;

public class World {
    private Map<String, Room> rooms;
    private Player player;

    public World() {
        this.rooms = new HashMap<>();
    }

    public void setupWorld() {
        // Define Tile Map Layouts
        // P = Player Start, W = Wall, F = Floor, D = Door (closed), . = Open Door/Path
        // E = Enemy Placeholder, C = Chest Placeholder, K = Key Placeholder
        // These placeholders 'E', 'C', 'K' are for initial visual setup.
        // Actual entity drawing should come from game objects having tileX, tileY.

        String[] cellLayout = {    // Room: "cell"
                "WWWWW",    // Row 0
                "WPFDW",    // Row 1: P(1,1), F(2,1), D(3,1) is North Door
                "WFFFW",    // Row 2
                "WCFFW",    // Row 3: C(1,3) for a chest
                "WWWWW"     // Row 4
        };
        String[] hallwayLayout = { // Room: "hallway"
                "WW.WW",    // Row 0: . (1,0) is South entrance from Cell's North Door
                "WGFEW",    // Row 1: G(1,1) Goblin, F(2,1), E(3,1) Orc (example)
                "WFFFW",    // Row 2
                "W.F.W",    // Row 3: . (1,3) West Door, . (3,3) East Door
                "WW.WW"     // Row 4: . (2,4) North Door
        };
        // ... Define other layouts similarly, focusing on walls, floors, and door *markers* ...
        String[] shopLayout = {
                "WWWWW",
                "WFFFW",
                "WFMFW", // M(2,2) Merchant
                "WFFFW",
                "WW.WW"  // . (2,4) South door to hallway
        };

        String[] guardRoomLayout = {
                "WWWWW",
                "WDFCW", // Door West, Floor, Chest
                "WKFEW", // Key, Floor, Enemy
                "WFFFW",
                "WWWWW"
        };
        // ... Define layouts for armory, treasury, shopRoom similarly ...
        String[] armoryLayout = {
                "WWWWW",
                "WFFFW",
                "WFS.W", // S for sword, . for shield (as items), D for door east
                "WFFFW",
                "WWWWW"
        };
        String[] treasuryLayout = {
                "WWWWW",
                "WFFFW",
                "WFFFW", // Could have gold 'G' or other items
                "WDFGW", // D for door south, G for gold placeholder
                "WWWWW"
        };


        // Create Items
        Key cellKey = new Key("key_cell", "Cell Key", "A rusty iron key.", "lock_cell_door");
        Key chestKey = new Key("key_chest_1", "Small Brass Key", "A small, ornate brass key.", "lock_chest_1");
        Crowbar crowbar = new Crowbar("crowbar_1", "Crowbar", "A sturdy steel crowbar. Good for prying.");
        Medicine potion = new Medicine("potion_health_1", "Health Potion", "Restores 25 HP.", 25);
        Weapon sword = new Weapon("sword_rusty", "Rusty Sword", "A basic, somewhat dull sword.", 10);
        Shield woodenShield = new Shield("shield_wood", "Wooden Shield", "A simple wooden shield.", 5);
        Gold goldCoins = new Gold("gold_10", "Gold Coins", "A small pouch of 10 gold coins.", 10);

        // Create Locks
        Lock cellDoorLock = new Lock("lock_cell_door", "cell_key_id", true);
        cellDoorLock.addAcceptableKeyId(cellKey.getId());

        Lock chestLock = new Lock("lock_chest_1", "chest_key_id", true);
        chestLock.addAcceptableKeyId(chestKey.getId());

        // Create Rooms using the 4-argument constructor
        Room cell = new Room("room_cell", "Jail Cell", "A damp and dark jail cell.", cellLayout);
        Room hallway = new Room("room_hallway", "Dim Hallway", "A long, dimly lit hallway.", hallwayLayout);
        Room guardRoom = new Room("room_guard_room", "Guard Room", "Smells of stale ale.", guardRoomLayout);
        Room armory = new Room("room_armory", "Armory", "Weapon racks, mostly empty.", armoryLayout);
        Room treasury = new Room("room_treasury", "Treasury", "Fortified, looks looted.", treasuryLayout);
        Room shopRoom = new Room("room_shop", "Makeshift Shop", "A merchant has wares.", shopLayout);

        rooms.put(cell.getId(), cell);
        rooms.put(hallway.getId(), hallway);
        rooms.put(guardRoom.getId(), guardRoom);
        rooms.put(armory.getId(), armory);
        rooms.put(treasury.getId(), treasury);
        rooms.put(shopRoom.getId(), shopRoom);

        // Create Doors (and link rooms)
        // For graphical representation, doors might need to know their tile coordinates
        // Example: Door(..., Point tileCoords)
        // For 'cellLayout', if 'D' is at (3,1) for the north door:
        Door cellToHallwayDoor = new Door("door_cell_hallway", "Cell Door", "A heavy wooden door.", cell, hallway, cellDoorLock, new Point(3,1));
        cell.addExit("north", cellToHallwayDoor);
        hallway.addExit("south", cellToHallwayDoor); // South entrance in hallway needs its own coords in hallwayLayout

        // Hallway <-> Guard Room (Assume East door in Hallway is at (3,3) in hallwayLayout)
        Door hallwayToGuardRoomDoor = new Door("door_hallway_guard", "Guard Room Door", "Standard wooden door.", hallway, guardRoom, null, new Point(3,3));
        hallway.addExit("east", hallwayToGuardRoomDoor);
        guardRoom.addExit("west", hallwayToGuardRoomDoor); // West entrance in guardRoom needs its coords

        // Hallway <-> Armory (Assume West door in Hallway is at (1,3) in hallwayLayout)
        Door hallwayToArmoryDoor = new Door("door_hallway_armory", "Armory Door", "Reinforced door.", hallway, armory, null, new Point(1,3));
        hallway.addExit("west", hallwayToArmoryDoor);
        armory.addExit("east", hallwayToArmoryDoor);

        // Guard Room <-> Treasury (Assume North door in Guard Room is at (1,1) in guardRoomLayout)
        Door guardToTreasuryDoor = new Door("door_guard_treasury", "Treasury Door", "Heavy iron door.", guardRoom, treasury, chestLock, new Point(1,1));
        guardRoom.addExit("north", guardToTreasuryDoor);
        treasury.addExit("south", guardToTreasuryDoor);

        // Hallway <-> Shop (Assume North door in Hallway is at (3,4) in hallwayLayout)
        Door hallwayToShopDoor = new Door("door_hallway_shop", "Shop Entrance", "Curtained doorway.", hallway, shopRoom, null, new Point(3,4));
        hallway.addExit("north", hallwayToShopDoor);
        shopRoom.addExit("south", hallwayToShopDoor);


        // Populate Rooms with Items
        // For graphical, items also need tileX, tileY within their room
        // potion.setTileCoordinates(x, y); // You'd need to add this method to Item
        cell.addItem(potion);
        guardRoom.addItem(cellKey); // If 'K' in guardRoomLayout is at (1,2) -> cellKey.setTileCoordinates(1,2);
        armory.addItem(sword);    // If 'S' in armoryLayout is at (2,2) -> sword.setTileCoordinates(2,2);
        armory.addItem(woodenShield);
        Crowbar startingCrowbar = new Crowbar("crowbar_start", "Rusty Crowbar", "A rusty but serviceable crowbar.");
        cell.addItem(startingCrowbar); // Place it graphically, e.g., next to player start

        // Create Chests
        // Chests also need tileX, tileY
        // guardChest.setTileCoordinates(x,y); // If 'C' in guardRoomLayout is at (3,1)
        Chest guardChest = new Chest("chest_guard", "Guard's Chest", "A locked wooden chest.", chestLock);
        guardChest.addItem(goldCoins);
        guardChest.addItem(new Medicine("potion_strong", "Strong Potion", "Restores 50HP", 50));
        guardRoom.addChest(guardChest);

        Chest armoryChest = new Chest("chest_armory", "Armory Footlocker", "An unlocked footlocker.", null);
        armoryChest.addItem(new Weapon("axe_battle", "Battle Axe", "A sturdy battle axe.", 15));
        armory.addChest(armoryChest);

        // Create Player
        player = new Player("Hero", 100, 5, cell); // Player constructor now sets tileX/Y from room's 'P'
        cell.addLivingBeing(player); // Player is already added via setCurrentRoom in LivingBeing

        // Create NPCs/Enemies
        // Enemies also need tileX, tileY
        // goblin.setTileCoordinates(x,y); // If 'E' in hallwayLayout is at (4,1)
        Enemy goblin = new Enemy("goblin_1", "Grumpy Goblin", "Small, grumpy goblin.", 30, 8, hallway, null);
        hallway.addLivingBeing(goblin);
        // goblin.setInitialTilePosition(4,1); // You'd need to add this method to LivingBeing

        Enemy orcGuard = new Enemy("orc_guard_1", "Orc Guard", "Brutish orc guard.", 70, 12, guardRoom, chestKey);
        guardRoom.addLivingBeing(orcGuard);
        // orcGuard.setInitialTilePosition(x,y); // Based on 'E' in guardRoomLayout

        Merchant merchant = new Merchant("merchant_bob", "Bob the Merchant", "Friendly merchant.", shopRoom);
        // merchant.setInitialTilePosition(x,y); // Based on 'M' in shopRoomLayout
        merchant.addToCatalog(new Weapon("sword_fine", "Fine Sword", "Well-crafted sword.", 12), 50);
        // ...
        shopRoom.addLivingBeing(merchant);
    }

    public Player getPlayer() {
        return player;
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }
}
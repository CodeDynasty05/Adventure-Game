package com.OOP.model.core;

import com.OOP.model.entities.*;
import com.OOP.model.interactables.Chest;
import com.OOP.model.interactables.Lock;
import com.OOP.model.items.*;

import java.util.HashMap;
import java.util.Map;

public class World {
    private Map<String, Room> rooms;
    private Player player;

    public World() {
        this.rooms = new HashMap<>();
    }

    public void setupWorld() {
        // Create Items
        Key cellKey = new Key("key_cell", "Cell Key", "A rusty iron key.", "lock_cell_door");
        Key chestKey = new Key("key_chest_1", "Small Brass Key", "A small, ornate brass key.", "lock_chest_1");
        Crowbar crowbar = new Crowbar("crowbar_1", "Crowbar", "A sturdy steel crowbar. Good for prying.");
        Medicine potion = new Medicine("potion_health_1", "Health Potion", "Restores 25 HP.", 25);
        Weapon sword = new Weapon("sword_rusty", "Rusty Sword", "A basic, somewhat dull sword.", 10);
        Shield woodenShield = new Shield("shield_wood", "Wooden Shield", "A simple wooden shield.", 5); // 5 damage reduction
        Gold goldCoins = new Gold("gold_10", "Gold Coins", "A small pouch of 10 gold coins.", 10);

        // Create Locks
        Lock cellDoorLock = new Lock("lock_cell_door", "cell_key_id", true); // Requires key_cell
        cellDoorLock.addAcceptableKeyId(cellKey.getId());

        Lock chestLock = new Lock("lock_chest_1", "chest_key_id", true); // Requires key_chest_1
        chestLock.addAcceptableKeyId(chestKey.getId());

        // Create Rooms
        Room cell = new Room("room_cell", "Jail Cell", "A damp and dark jail cell.");
        Room hallway = new Room("room_hallway", "Dim Hallway", "A long, dimly lit hallway.");
        Room guardRoom = new Room("room_guard_room", "Guard Room", "A small room with a table and a chair. Smells of stale ale.");
        Room armory = new Room("room_armory", "Armory", "A room with weapon racks, mostly empty.");
        Room treasury = new Room("room_treasury", "Treasury", "A fortified room. Looks like it was looted.");
        Room shopRoom = new Room("room_shop", "Makeshift Shop", "A quiet corner where a merchant has set up wares.");

        rooms.put(cell.getId(), cell);
        rooms.put(hallway.getId(), hallway);
        rooms.put(guardRoom.getId(), guardRoom);
        rooms.put(armory.getId(), armory);
        rooms.put(treasury.getId(), treasury);
        rooms.put(shopRoom.getId(), shopRoom);


        // Create Doors (and link rooms)
        // Cell <-> Hallway
        Door cellToHallwayDoor = new Door("door_cell_hallway", "Cell Door", "A heavy wooden door.", cell, hallway, cellDoorLock);
        cell.addExit("north", cellToHallwayDoor);
        hallway.addExit("south", cellToHallwayDoor);

        // Hallway <-> Guard Room
        Door hallwayToGuardRoomDoor = new Door("door_hallway_guard", "Guard Room Door", "A standard wooden door.", hallway, guardRoom, null); // Unlocked
        hallway.addExit("east", hallwayToGuardRoomDoor);
        guardRoom.addExit("west", hallwayToGuardRoomDoor);

        // Hallway <-> Armory
        Door hallwayToArmoryDoor = new Door("door_hallway_armory", "Armory Door", "A reinforced door.", hallway, armory, null); // Unlocked for now
        hallway.addExit("west", hallwayToArmoryDoor);
        armory.addExit("east", hallwayToArmoryDoor);

        // Guard Room <-> Treasury
        Door guardToTreasuryDoor = new Door("door_guard_treasury", "Treasury Door", "A heavy iron door.", guardRoom, treasury, chestLock); // Use chestKey
        guardRoom.addExit("north", guardToTreasuryDoor);
        treasury.addExit("south", guardToTreasuryDoor);

        // Hallway <-> Shop
        Door hallwayToShopDoor = new Door("door_hallway_shop", "Shop Entrance", "A simple curtained doorway.", hallway, shopRoom, null);
        hallway.addExit("north", hallwayToShopDoor); // Another north exit from hallway
        shopRoom.addExit("south", hallwayToShopDoor);


        // Populate Rooms with Items
        cell.addItem(potion); // Potion on the floor in the cell
        guardRoom.addItem(cellKey); // Key to the cell is in the guard room
        armory.addItem(sword);
        armory.addItem(woodenShield);
        Crowbar startingCrowbar = new Crowbar("crowbar_start", "Rusty Crowbar", "A rusty but serviceable crowbar.");
        cell.addItem(startingCrowbar); // Player finds a crowbar in the cell

        // Create Chests
        Chest guardChest = new Chest("chest_guard", "Guard's Chest", "A locked wooden chest.", chestLock);
        guardChest.addItem(goldCoins);
        guardChest.addItem(new Medicine("potion_strong", "Strong Potion", "Restores 50HP", 50));
        guardRoom.addChest(guardChest); // Chest in the guard room, needs chest_key_1

        Chest armoryChest = new Chest("chest_armory", "Armory Footlocker", "An unlocked footlocker.", null); // Unlocked
        armoryChest.addItem(new Weapon("axe_battle", "Battle Axe", "A sturdy battle axe.", 15));
        armory.addChest(armoryChest);


        // Create Player
        player = new Player("Hero", 100, 5, cell); // Start in cell, 5 base attack
        cell.addLivingBeing(player);

        // Create NPCs/Enemies
        Enemy goblin = new Enemy("goblin_1", "Grumpy Goblin", "A small, grumpy goblin.", 30, 8, hallway, null); // No special loot
        hallway.addLivingBeing(goblin);

        Enemy orcGuard = new Enemy("orc_guard_1", "Orc Guard", "A brutish orc guard.", 70, 12, guardRoom, chestKey); // Drops chest key
        guardRoom.addLivingBeing(orcGuard);

        Merchant merchant = new Merchant("merchant_bob", "Bob the Merchant", "A friendly-looking merchant.", shopRoom);
        merchant.addToCatalog(new Weapon("sword_fine", "Fine Sword", "A well-crafted sword.", 12), 50);
        merchant.addToCatalog(new Shield("shield_steel", "Steel Shield", "A shiny steel shield.", 10), 70);
        merchant.addToCatalog(new Medicine("potion_greater", "Greater Potion", "Restores 100HP.", 100), 40);
        shopRoom.addLivingBeing(merchant);

        // Teammate (placeholder)
        // Teammate companion = new Teammate("ally_1", "Helpful Companion", "Looks eager to help.", 50, 7, cell);
        // cell.addLivingBeing(companion); // If you want a companion from the start
    }

    public Player getPlayer() {
        return player;
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }
}
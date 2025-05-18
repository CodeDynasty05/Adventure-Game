package com.OOP;

import com.OOP.interfaces.Activatable;
import com.OOP.interfaces.Executable;
import com.OOP.model.core.*;
import com.OOP.model.entities.*;
import com.OOP.model.items.*;
import com.OOP.model.interactables.Chest;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static World world;
    private static Player player;
    private static Scanner scanner = new Scanner(System.in);
    private static boolean gameOver = false;

    public static void main(String[] args) {
        world = new World();
        world.setupWorld();
        player = world.getPlayer();

        System.out.println("Welcome to the Adventure Game!");
        System.out.println("Type 'help' for commands.");

        while (!gameOver) {
            if (player.getHealthPoints() <= 0) {
                // Player.die() prints a message, this ensures game loop termination
                System.out.println("Your journey ends here.");
                gameOver = true;
                break;
            }
            System.out.println("\n-----------------------------------");
            System.out.println(player.getCurrentRoom().getFullDescription());
            System.out.print("> ");
            String input = scanner.nextLine().trim().toLowerCase();
            processInput(input);

            // Simple enemy AI: enemies in the room attack if player is present
            // Only after player's turn
            if (!gameOver) {
                handleEnemyTurns();
            }
        }
        System.out.println("Thanks for playing!");
        scanner.close();
    }

    private static void processInput(String input) {
        String[] parts = input.split("\\s+", 3); // command target [modifier/specification]
        String command = parts[0];
        String targetName = parts.length > 1 ? parts[1] : null;
        String specification = parts.length > 2 ? parts[2] : null; // e.g., "key" in "use key on door"

        switch (command) {
            case "help":
                showHelp();
                break;
            case "look":
                if (targetName == null) {
                    // Already handled by printing room description, or could add more detail
                    System.out.println(player.getCurrentRoom().getFullDescription());
                } else {
                    lookAt(targetName);
                }
                break;
            case "go":
            case "move":
                if (targetName != null) {
                    movePlayer(targetName); // targetName is direction
                } else {
                    System.out.println("Go where? (e.g., go north)");
                }
                break;
            case "take":
            case "get":
                if (targetName != null) {
                    takeItem(targetName);
                } else {
                    System.out.println("Take what?");
                }
                break;
            case "drop":
                if (targetName != null) {
                    player.dropItem(targetName);
                } else {
                    System.out.println("Drop what?");
                }
                break;
            case "i":
            case "inv":
            case "inventory":
                player.viewInventory();
                break;
            case "use":
                if (targetName != null) {
                    // use <item_name> [on <target_entity_name>]
                    // e.g. use potion
                    // e.g. use key on door
                    // e.g. use crowbar on chest
                    String onItemName = targetName;
                    String onEntityName = null;
                    if (specification != null && specification.startsWith("on ") && specification.length() > 3) {
                        onEntityName = specification.substring(3);
                    } else if (specification != null) { // Simple "use item target"
                        onEntityName = specification;
                    }
                    useItem(onItemName, onEntityName);
                } else {
                    System.out.println("Use what? (e.g. use potion, use key on door)");
                }
                break;
            case "open":
                if (targetName != null) {
                    // open <door/chest_name> [with <item_name>]
                    String activatableName = targetName;
                    String withItemName = null;
                    if (specification != null && specification.startsWith("with ") && specification.length() > 5) {
                        withItemName = specification.substring(5);
                    } else if (specification != null) { // open door key
                        withItemName = specification;
                    }
                    openActivatable(activatableName, withItemName);
                } else {
                    System.out.println("Open what? (e.g. open door, open chest with key)");
                }
                break;
            case "attack":
                if (targetName != null) {
                    attackTarget(targetName);
                } else {
                    System.out.println("Attack who?");
                }
                break;
            case "talk":
                if (targetName != null) {
                    talkTo(targetName);
                } else {
                    System.out.println("Talk to who?");
                }
                break;
            case "buy":
                if (targetName != null) {
                    buyFromMerchant(targetName);
                } else {
                    System.out.println("Buy what?");
                }
                break;
            case "equip":
                if(targetName != null) {
                    equipItem(targetName);
                } else {
                    System.out.println("Equip what?");
                }
                break;
            case "unequip":
                if(targetName != null) {
                    unequipItem(targetName);
                } else {
                    System.out.println("Unequip what? (weapon or shield)");
                }
                break;
            case "quit":
            case "exit":
                gameOver = true;
                break;
            default:
                System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
    }

    private static void showHelp() {
        System.out.println("Available commands:");
        System.out.println("  help                      - Show this help message.");
        System.out.println("  look                      - Describe the current room.");
        System.out.println("  look <object/npc/door>    - Describe something specific.");
        System.out.println("  go/move <direction>       - Move in a direction (e.g., go north).");
        System.out.println("  take/get <item_name>      - Pick up an item from the floor or an open chest.");
        System.out.println("  drop <item_name>          - Drop an item from your inventory.");
        System.out.println("  inventory/inv/i           - View your inventory and gold.");
        System.out.println("  use <item_name> [on <target_name>] - Use an item (e.g., use potion, use key on door).");
        System.out.println("  open <door/chest_name> [with <item_name>] - Open a door or chest (e.g. open cell_door with cell_key, open chest_1).");
        System.out.println("  attack <target_name>      - Attack an enemy.");
        System.out.println("  talk <npc_name>           - Talk to an NPC.");
        System.out.println("  buy <item_name>           - Buy an item from a merchant (if talking to one).");
        System.out.println("  equip <weapon/shield_name>- Equip a weapon or shield from your inventory.");
        System.out.println("  unequip <weapon/shield>   - Unequip current weapon or shield.");
        System.out.println("  quit/exit                 - Exit the game.");
    }

    private static void lookAt(String targetName) {
        Room currentRoom = player.getCurrentRoom();
        // Check items on floor
        for (Item item : currentRoom.getItemsOnFloor()) {
            if (item.getName().equalsIgnoreCase(targetName)) {
                System.out.println(item.getDescription());
                return;
            }
        }
        // Check items in inventory
        Item itemInInv = player.getItemFromInventory(targetName);
        if (itemInInv != null) {
            System.out.println(itemInInv.getDescription());
            return;
        }
        // Check doors
        for (Door door : currentRoom.getExits().values()) {
            if (door.getName().equalsIgnoreCase(targetName) ||
                    (door.getId().replace("door_","").replace("_", " ").contains(targetName))) { // simple name check
                System.out.println(door.getDescription());
                return;
            }
        }
        // Check chests
        for (Chest chest : currentRoom.getChests()) {
            if (chest.getName().equalsIgnoreCase(targetName)) {
                System.out.println(chest.getDescription());
                if (chest.isOpen() && !chest.getContents().isEmpty()) {
                    System.out.println("It contains: " + chest.getContents().stream().map(Item::getName).collect(Collectors.joining(", ")));
                } else if (chest.isOpen() && chest.getContents().isEmpty()) {
                    System.out.println("It is empty.");
                }
                return;
            }
        }
        // Check living beings
        for (LivingBeing lb : currentRoom.getLivingBeings()) {
            if (lb.getName().equalsIgnoreCase(targetName)) {
                System.out.println(lb.getDescription() + " (HP: " + lb.getHealthPoints() + "/" + lb.getMaxHealthPoints() + ")");
                return;
            }
        }
        System.out.println("You don't see '" + targetName + "' here.");
    }

    private static void movePlayer(String direction) {
        Room currentRoom = player.getCurrentRoom();
        Door door = currentRoom.getExit(direction);

        if (door == null) {
            System.out.println("You can't go that way.");
            return;
        }

        if (door.isLocked()) {
            System.out.println("The " + door.getName() + " is locked.");
            return;
        }

        Room nextRoom = door.getOppositeRoom(currentRoom);
        if (nextRoom != null) {
            player.setCurrentRoom(nextRoom);
            // Room description will be printed at the start of the next loop
        } else {
            System.out.println("Error: Door leads nowhere?!"); // Should not happen
        }
    }

    private static void takeItem(String itemName) {
        Room currentRoom = player.getCurrentRoom();
        Item itemToTake = currentRoom.removeItem(itemName); // Tries to remove from floor

        if (itemToTake != null) {
            player.pickUpItem(itemToTake); // Handles adding to inventory or gold
            return;
        }

        // Try taking from an open chest
        for (Chest chest : currentRoom.getChests()) {
            if (chest.isOpen()) {
                for (Item itemInChest : new ArrayList<>(chest.getContents())) { // Iterate copy
                    if (itemInChest.getName().equalsIgnoreCase(itemName)) {
                        chest.getContents().remove(itemInChest); // Remove from chest
                        player.pickUpItem(itemInChest);          // Add to player
                        if (chest.getContents().isEmpty()) {
                            chest.setDescription(chest.getDescription().replaceFirst("\\(.*\\)", "(Open, empty)"));
                        }
                        return;
                    }
                }
            }
        }
        System.out.println("There is no '" + itemName + "' here to take, or the container is closed.");
    }

    private static void useItem(String itemName, String onTargetName) {
        Item itemToUse = player.getItemFromInventory(itemName);
        if (itemToUse == null) {
            System.out.println("You don't have '" + itemName + "'.");
            return;
        }

        if (itemToUse instanceof Executable) {
            Executable executableItem = (Executable) itemToUse;
            LivingBeing target = player; // Default target is self (e.g., for potions)
            if (onTargetName != null) {
                target = player.getCurrentRoom().getLivingBeingByName(onTargetName);
                if (target == null) {
                    System.out.println("Cannot find '" + onTargetName + "' to use the item on.");
                    return;
                }
            }
            executableItem.execute(player, target);
            if (itemToUse instanceof Medicine) { // Consumables are removed after use
                player.getInventory().remove(itemToUse);
            }
        } else if (itemToUse instanceof Key || itemToUse instanceof Crowbar) {
            // Handle key/crowbar on door/chest
            if (onTargetName == null) {
                System.out.println("Use " + itemName + " on what? (e.g., use " + itemName + " on <door_name> or <chest_name>)");
                return;
            }
            openActivatable(onTargetName, itemName); // itemName here is the key/crowbar name
        } else {
            System.out.println("You can't 'use' " + itemName + " in that way. Maybe 'equip' or 'open with'?");
        }
    }

    private static void openActivatable(String activatableName, String withItemName) {
        Room currentRoom = player.getCurrentRoom();
        Activatable targetActivatable = null;

        // Check doors
        for (Door door : currentRoom.getExits().values()) {
            if (door.getName().equalsIgnoreCase(activatableName) || door.getId().replace("door_","").replace("_", " ").contains(activatableName)) {
                targetActivatable = door;
                break;
            }
        }
        // Check chests if no door found
        if (targetActivatable == null) {
            for (Chest chest : currentRoom.getChests()) {
                if (chest.getName().equalsIgnoreCase(activatableName)) {
                    targetActivatable = chest;
                    break;
                }
            }
        }

        if (targetActivatable == null) {
            System.out.println("There is no '" + activatableName + "' here to open.");
            return;
        }

        Item itemToUse = null;
        String itemIdToUseOnActivatable = null; // This is what Activatable.activate expects

        if (withItemName != null) {
            itemToUse = player.getItemFromInventory(withItemName);
            if (itemToUse instanceof Key) {
                itemIdToUseOnActivatable = itemToUse.getId();
            } else if (itemToUse instanceof Crowbar) {
                itemIdToUseOnActivatable = "crowbar"; // Special identifier for crowbar
            } else if (itemToUse != null) {
                System.out.println("You can't use '" + withItemName + "' to open things.");
                return;
            } else {
                System.out.println("You don't have '" + withItemName + "'.");
                return;
            }
        } else if (player.hasItemByName("crowbar") && targetActivatable.isLocked()){
            // If no item specified but player has a crowbar, assume trying to use crowbar
            itemIdToUseOnActivatable = "crowbar";
        }


        if (targetActivatable.activate(player, itemIdToUseOnActivatable)) {
            // Success message handled by activate method
            if (targetActivatable instanceof Chest && ((Chest) targetActivatable).isOpen()) {
                Chest chest = (Chest) targetActivatable;
                if (!chest.getContents().isEmpty()) {
                    System.out.println("The " + chest.getName() + " contains: " +
                            chest.getContents().stream().map(Item::getName).collect(Collectors.joining(", ")));
                } else {
                    System.out.println("The " + chest.getName() + " is empty.");
                }
            }
        } else {
            // Failure message handled by activate or here if it's a general failure
            // System.out.println("Could not open " + activatableName); // activate() usually more specific
        }
    }

    private static void attackTarget(String targetName) {
        LivingBeing target = player.getCurrentRoom().getLivingBeingByName(targetName);
        if (target == null) {
            System.out.println("There is no one here named '" + targetName + "' to attack.");
            return;
        }
        if (target instanceof Player) {
            System.out.println("You contemplate attacking yourself, but decide against it.");
            return;
        }
        if (target instanceof NPC && ((NPC)target).isFriendly()) {
            System.out.println(target.getName() + " is friendly. Attacking them might have consequences!");
            // Could add logic to turn them hostile
        }

        player.attack(target); // Player attacks first
        // Enemy retaliation is handled in handleEnemyTurns()
    }

    private static void handleEnemyTurns() {
        Room currentRoom = player.getCurrentRoom();
        List<LivingBeing> beingsInRoom = new ArrayList<>(currentRoom.getLivingBeings()); // Copy to avoid ConcurrentModificationException if one dies

        for (LivingBeing being : beingsInRoom) {
            if (player.getHealthPoints() <= 0) break; // Stop if player died during an earlier enemy's turn

            if (being instanceof Enemy && being.getHealthPoints() > 0) {
                System.out.println(being.getName() + " retaliates!");
                being.attack(player);
            } else if (being instanceof Teammate && being.getHealthPoints() > 0) {
                // Basic Teammate AI: attack any non-player, non-friendly NPC in the room
                Teammate teammate = (Teammate) being;
                LivingBeing enemyTarget = currentRoom.getLivingBeings().stream()
                        .filter(lb -> lb instanceof Enemy && lb.getHealthPoints() > 0)
                        .findFirst()
                        .orElse(null);
                if (enemyTarget != null) {
                    teammate.assist(enemyTarget);
                }
            }
        }
    }

    private static void talkTo(String npcName) {
        LivingBeing being = player.getCurrentRoom().getLivingBeingByName(npcName);
        if (being instanceof NPC) {
            ((NPC) being).interact(player);
            // If it's a merchant, the game state might change to "trading"
            // For now, interact just prints a message or displays wares.
        } else if (being != null) {
            System.out.println("You can't have a meaningful conversation with " + being.getName() + ".");
        } else {
            System.out.println("There's no one here called '" + npcName + "'.");
        }
    }

    private static void buyFromMerchant(String itemName) {
        Merchant merchant = null;
        for (LivingBeing lb : player.getCurrentRoom().getLivingBeings()) {
            if (lb instanceof Merchant) {
                merchant = (Merchant) lb;
                break;
            }
        }

        if (merchant == null) {
            System.out.println("There is no merchant here to buy from.");
            return;
        }
        // If player typed "buy <item>" directly without "talk" first
        merchant.sellToPlayer(itemName, player);
    }

    private static void equipItem(String itemName) {
        Item item = player.getItemFromInventory(itemName);
        if (item == null) {
            System.out.println("You don't have '" + itemName + "'.");
            return;
        }
        if (item instanceof Weapon) {
            player.equipWeapon((Weapon) item);
        } else if (item instanceof Shield) {
            player.equipShield((Shield) item);
        } else {
            System.out.println("You can't equip " + item.getName() + ".");
        }
    }

    private static void unequipItem(String itemType) {
        if ("weapon".equalsIgnoreCase(itemType) && player.getEquippedWeapon() != null) {
            player.unequipWeapon();
        } else if ("shield".equalsIgnoreCase(itemType) && player.getEquippedShield() != null) {
            player.unequipShield();
        } else if (player.getEquippedWeapon() != null && player.getEquippedWeapon().getName().equalsIgnoreCase(itemType)) {
            player.unequipWeapon();
        } else if (player.getEquippedShield() != null && player.getEquippedShield().getName().equalsIgnoreCase(itemType)) {
            player.unequipShield();
        } else {
            System.out.println("You don't have a " + itemType + " equipped or '" + itemType + "' is not 'weapon' or 'shield'.");
        }
    }
}
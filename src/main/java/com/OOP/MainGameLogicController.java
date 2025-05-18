package com.OOP;

import com.OOP.interfaces.Activatable;
import com.OOP.interfaces.Executable;
import com.OOP.model.core.*;
import com.OOP.model.entities.*;
import com.OOP.model.items.*;
import com.OOP.model.interactables.Chest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class MainGameLogicController {

    private World world;
    private Player player; // This is already here
    private Consumer<String> outputConsumer;
    private Runnable statusUpdater;
    private boolean gameOver = false;

    public MainGameLogicController(World world, Player player, Consumer<String> outputConsumer, Runnable statusUpdater) {
        this.world = world;
        this.player = player;
        this.outputConsumer = outputConsumer;
        this.statusUpdater = statusUpdater;
    }

    // *** ADD THIS METHOD ***
    public Player getPlayer() {
        return this.player;
    }

    public void startGame() {
        outputConsumer.accept("Welcome to the Adventure Game!");
        outputConsumer.accept("Type 'help' for commands or use the buttons.");
        outputConsumer.accept("\n-----------------------------------");
        outputConsumer.accept(player.getCurrentRoom().getFullDescription());
        statusUpdater.run();
    }

    public void processInput(String input) {
        if (gameOver) return;
        // We no longer print the command here as GUI does it

        // --- Copy and adapt command processing from your original Main.java ---
        String command = input.trim().toLowerCase();
        String[] parts = command.split("\\s+", 2); // Split into command and the rest
        String action = parts[0];
        String arguments = parts.length > 1 ? parts[1] : null;

        switch (action) {
            case "help":
                showHelp();
                break;
            case "i":
            case "inv":
            case "inventory":
                player.viewInventory(); // This method takes no arguments
                break;
            case "look":
                if (arguments == null) { // "look" by itself
                    outputConsumer.accept(player.getCurrentRoom().getFullDescription());
                } else { // "look health potion" or "look cell door"
                    lookAt(arguments); // Pass the full argument string
                }
                break;
            case "go":
            case "move":
                if (arguments != null) { // "go north"
                    movePlayer(arguments); // 'arguments' is the direction
                } else {
                    outputConsumer.accept("Go where? (e.g., go north)");
                }
                break;
            case "take":
            case "get":
                if (arguments != null) { // "take health potion"
                    takeItem(arguments); // 'arguments' is "health potion"
                } else {
                    outputConsumer.accept("Take what?");
                }
                break;
            case "drop":
                if (arguments != null) { // "drop rusty sword"
                    player.dropItem(arguments);
                } else {
                    outputConsumer.accept("Drop what?");
                }
                break;
            // ... (inventory is fine) ...
            case "use":
                if (arguments != null) { // arguments could be "potion" or "key on door" or "cell key on cell door"
                    // Need more sophisticated parsing for "use X on Y"
                    String itemName;
                    String onTargetName = null;
                    if (arguments.contains(" on ")) {
                        String[] useParts = arguments.split("\\s+on\\s+", 2);
                        itemName = useParts[0].trim();
                        if (useParts.length > 1) {
                            onTargetName = useParts[1].trim();
                        }
                    } else {
                        itemName = arguments.trim();
                    }
                    useItem(itemName, onTargetName);
                } else {
                    outputConsumer.accept("Use what? (e.g. use potion, use key on door)");
                }
                break;
            case "open":
                if (arguments != null) { // arguments could be "cell door" or "chest with key"
                    // Need more sophisticated parsing for "open X with Y"
                    String activatableName;
                    String withItemName = null;
                    if (arguments.contains(" with ")) {
                        String[] openParts = arguments.split("\\s+with\\s+", 2);
                        activatableName = openParts[0].trim();
                        if (openParts.length > 1) {
                            withItemName = openParts[1].trim();
                        }
                    } else {
                        activatableName = arguments.trim();
                    }
                    openActivatable(activatableName, withItemName);
                } else {
                    outputConsumer.accept("Open what? (e.g. open door, open chest with key)");
                }
                break;
            case "attack":
                if (arguments != null) { // "attack grumpy goblin"
                    attackTarget(arguments);
                } else {
                    outputConsumer.accept("Attack who?");
                }
                break;
            case "talk":
                if (arguments != null) { // "talk bob the merchant"
                    talkTo(arguments);
                } else {
                    outputConsumer.accept("Talk to who?");
                }
                break;
            case "buy":
                if (arguments != null) { // "buy fine sword"
                    buyFromMerchant(arguments);
                } else {
                    outputConsumer.accept("Buy what?");
                }
                break;
            case "equip":
                if(arguments != null) { // "equip rusty sword"
                    equipItem(arguments);
                } else {
                    outputConsumer.accept("Equip what?");
                }
                break;
            case "unequip":
                if(arguments != null) { // "unequip weapon" or "unequip rusty sword"
                    unequipItem(arguments);
                } else {
                    outputConsumer.accept("Unequip what? (weapon or shield)");
                }
                break;
            case "quit":
            case "exit":
                outputConsumer.accept("Quitting game...");
                gameOver = true;
                // GUI will handle actual closing, or you can call System.exit(0) after a delay
                // For now, just stops processing further commands.
                break;
            default:
                outputConsumer.accept("Unknown command. Type 'help' for a list of commands.");
        }

        // After processing player input, handle enemy turns and check game over
        if (!gameOver) {
            handleEnemyTurns();
            if (player.getHealthPoints() <= 0) {
                // Player.die() prints a message
                outputConsumer.accept("Your journey ends here.");
                gameOver = true;
            }
        }

        // Always update room description and status after an action
        if (!gameOver) {
            outputConsumer.accept("\n-----------------------------------");
            outputConsumer.accept(player.getCurrentRoom().getFullDescription());
        }
        statusUpdater.run(); // Update HP, Gold, Room name, button states etc.
    }


    // --- PASTE ALL YOUR HELPER METHODS (showHelp, lookAt, movePlayer, etc.) HERE ---
    // --- from your original Main.java, but change System.out.println() to outputConsumer.accept() ---

    private void showHelp() {
        outputConsumer.accept("Available commands (or use buttons):");
        outputConsumer.accept("  help                      - Show this help message.");
        outputConsumer.accept("  look                      - Describe the current room.");
        outputConsumer.accept("  look <object/npc/door>    - Describe something specific.");
        outputConsumer.accept("  go/move <direction>       - Move in a direction (e.g., go north).");
        outputConsumer.accept("  take/get <item_name>      - Pick up an item from the floor or an open chest.");
        outputConsumer.accept("  drop <item_name>          - Drop an item from your inventory.");
        outputConsumer.accept("  inventory/inv/i           - View your inventory and gold.");
        outputConsumer.accept("  use <item_name> [on <target_name>] - Use an item (e.g., use potion, use key on door).");
        outputConsumer.accept("  open <door/chest_name> [with <item_name>] - Open a door or chest (e.g. open cell_door with cell_key, open chest_1).");
        outputConsumer.accept("  attack <target_name>      - Attack an enemy.");
        outputConsumer.accept("  talk <npc_name>           - Talk to an NPC.");
        outputConsumer.accept("  buy <item_name>           - Buy an item from a merchant (if talking to one).");
        outputConsumer.accept("  equip <weapon/shield_name>- Equip a weapon or shield from your inventory.");
        outputConsumer.accept("  unequip <weapon/shield>   - Unequip current weapon or shield.");
        outputConsumer.accept("  quit/exit                 - Exit the game.");
    }

    private void lookAt(String targetName) {
        Room currentRoom = player.getCurrentRoom();
        // Check items on floor
        for (Item item : currentRoom.getItemsOnFloor()) {
            if (item.getName().equalsIgnoreCase(targetName)) {
                outputConsumer.accept(item.getDescription());
                return;
            }
        }
        // Check items in inventory
        Item itemInInv = player.getItemFromInventory(targetName);
        if (itemInInv != null) {
            outputConsumer.accept(itemInInv.getDescription());
            return;
        }
        // Check doors
        for (Door door : currentRoom.getExits().values()) {
            if (door.getName().equalsIgnoreCase(targetName) ||
                    (door.getId().replace("door_","").replace("_", " ").contains(targetName))) {
                outputConsumer.accept(door.getDescription());
                return;
            }
        }
        // Check chests
        for (Chest chest : currentRoom.getChests()) {
            if (chest.getName().equalsIgnoreCase(targetName)) {
                outputConsumer.accept(chest.getDescription());
                if (chest.isOpen() && !chest.getContents().isEmpty()) {
                    outputConsumer.accept("It contains: " + chest.getContents().stream().map(Item::getName).collect(Collectors.joining(", ")));
                } else if (chest.isOpen() && chest.getContents().isEmpty()) {
                    outputConsumer.accept("It is empty.");
                }
                return;
            }
        }
        // Check living beings
        for (LivingBeing lb : currentRoom.getLivingBeings()) {
            if (lb.getName().equalsIgnoreCase(targetName)) {
                outputConsumer.accept(lb.getDescription() + " (HP: " + lb.getHealthPoints() + "/" + lb.getMaxHealthPoints() + ")");
                return;
            }
        }
        outputConsumer.accept("You don't see '" + targetName + "' here.");
    }

    private void movePlayer(String direction) {
        Room currentRoom = player.getCurrentRoom();
        Door door = currentRoom.getExit(direction);

        if (door == null) {
            outputConsumer.accept("You can't go that way.");
            return;
        }

        if (door.isLocked()) {
            outputConsumer.accept("The " + door.getName() + " is locked.");
            return;
        }

        Room nextRoom = door.getOppositeRoom(currentRoom);
        if (nextRoom != null) {
            player.setCurrentRoom(nextRoom);
            // Room description will be printed by the main loop after this method
        } else {
            outputConsumer.accept("Error: Door leads nowhere?!");
        }
    }

    private void takeItem(String itemName) {
        Room currentRoom = player.getCurrentRoom();
        Item itemToTake = null; // Initialize

        // First, try to find the item directly on the floor
        for (Item item : currentRoom.getItemsOnFloor()) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                itemToTake = item;
                break;
            }
        }

        if (itemToTake != null) {
            currentRoom.removeItem(itemToTake.getName()); // Remove from room
            player.pickUpItem(itemToTake); // This method handles System.out.println
            return;
        }

        // If not on floor, try taking from an open chest
        for (Chest chest : currentRoom.getChests()) {
            if (chest.isOpen()) {
                for (Item itemInChest : new ArrayList<>(chest.getContents())) {
                    if (itemInChest.getName().equalsIgnoreCase(itemName)) {
                        chest.getContents().remove(itemInChest);
                        player.pickUpItem(itemInChest); // This handles output
                        if (chest.getContents().isEmpty()) {
                            // Already handled by System.out in player.pickUpItem
                        }
                        return;
                    }
                }
            }
        }
        outputConsumer.accept("There is no '" + itemName + "' here to take, or the container is closed.");
    }

    private void useItem(String itemName, String onTargetName) {
        Item itemToUse = player.getItemFromInventory(itemName);
        if (itemToUse == null) {
            outputConsumer.accept("You don't have '" + itemName + "'.");
            return;
        }

        if (itemToUse instanceof Executable) {
            Executable executableItem = (Executable) itemToUse;
            LivingBeing target = player;
            if (onTargetName != null) {
                target = player.getCurrentRoom().getLivingBeingByName(onTargetName);
                if (target == null) {
                    outputConsumer.accept("Cannot find '" + onTargetName + "' to use the item on.");
                    return;
                }
            }
            executableItem.execute(player, target); // Assumes execute prints its own messages
            if (itemToUse instanceof Medicine) {
                player.getInventory().remove(itemToUse);
            }
        } else if (itemToUse instanceof Key || itemToUse instanceof Crowbar) {
            if (onTargetName == null) {
                outputConsumer.accept("Use " + itemName + " on what? (e.g., use " + itemName + " on <door_name> or <chest_name>)");
                return;
            }
            openActivatable(onTargetName, itemName);
        } else {
            outputConsumer.accept("You can't 'use' " + itemName + " in that way. Maybe 'equip' or 'open with'?");
        }
    }

    private void openActivatable(String activatableName, String withItemName) {
        Room currentRoom = player.getCurrentRoom();
        Activatable targetActivatable = null;

        for (Door door : currentRoom.getExits().values()) {
            if (door.getName().equalsIgnoreCase(activatableName) || door.getId().replace("door_","").replace("_", " ").contains(activatableName)) {
                targetActivatable = door;
                break;
            }
        }
        if (targetActivatable == null) {
            for (Chest chest : currentRoom.getChests()) {
                if (chest.getName().equalsIgnoreCase(activatableName)) {
                    targetActivatable = chest;
                    break;
                }
            }
        }

        if (targetActivatable == null) {
            outputConsumer.accept("There is no '" + activatableName + "' here to open.");
            return;
        }

        Item itemToUse = null;
        String itemIdToUseOnActivatable = null;

        if (withItemName != null) {
            itemToUse = player.getItemFromInventory(withItemName);
            if (itemToUse instanceof Key) {
                itemIdToUseOnActivatable = itemToUse.getId();
            } else if (itemToUse instanceof Crowbar) {
                itemIdToUseOnActivatable = "crowbar";
            } else if (itemToUse != null) {
                outputConsumer.accept("You can't use '" + withItemName + "' to open things.");
                return;
            } else {
                outputConsumer.accept("You don't have '" + withItemName + "'.");
                return;
            }
        } else if (player.hasItemByName("crowbar") && targetActivatable.isLocked()){
            itemIdToUseOnActivatable = "crowbar";
        }

        if (targetActivatable.activate(player, itemIdToUseOnActivatable)) { // activate prints its messages
            if (targetActivatable instanceof Chest && ((Chest) targetActivatable).isOpen()) {
                Chest chest = (Chest) targetActivatable;
                if (!chest.getContents().isEmpty()) {
                    outputConsumer.accept("The " + chest.getName() + " contains: " +
                            chest.getContents().stream().map(Item::getName).collect(Collectors.joining(", ")));
                } else {
                    outputConsumer.accept("The " + chest.getName() + " is empty.");
                }
            }
        }
        // else: activate method handles failure messages
    }

    // In MainGameLogicController.java -> attackTarget(String targetNameFromInput)
    private void attackTarget(String targetNameFromInput) {
        LivingBeing target = null;
        String searchName = targetNameFromInput.replace("_", " "); // Convert underscores back to spaces

        // Try exact match first (case-insensitive)
        for (LivingBeing lb : player.getCurrentRoom().getLivingBeings()) {
            if (lb.getName().equalsIgnoreCase(searchName) && lb instanceof Enemy) {
                target = lb;
                break;
            }
        }

        // If no exact match, maybe a partial match or if input was just "goblin" for "Grumpy Goblin"
        if (target == null) {
            for (LivingBeing lb : player.getCurrentRoom().getLivingBeings()) {
                if (lb.getName().toLowerCase().contains(searchName.toLowerCase()) && lb instanceof Enemy) {
                    target = lb;
                    System.out.println("(Interpreted target as: " + lb.getName() + ")"); // Optional feedback
                    break;
                }
            }
        }


        if (target == null) {
            outputConsumer.accept("There is no one here named '" + searchName + "' to attack.");
            return;
        }
        // ... (rest of attack logic: check if friendly, player.attack(target), etc.) ...
        player.attack(target);
        // Enemy retaliation is handled in handleEnemyTurns(), which should be called after player's action.
    }

    private void handleEnemyTurns() {
        Room currentRoom = player.getCurrentRoom();
        List<LivingBeing> beingsInRoom = new ArrayList<>(currentRoom.getLivingBeings());

        for (LivingBeing being : beingsInRoom) {
            if (player.getHealthPoints() <= 0) break;

            if (being instanceof Enemy && being.getHealthPoints() > 0) {
                // outputConsumer.accept(being.getName() + " retaliates!"); // attack method prints messages
                being.attack(player);
            } else if (being instanceof Teammate && being.getHealthPoints() > 0) {
                Teammate teammate = (Teammate) being;
                LivingBeing enemyTarget = currentRoom.getLivingBeings().stream()
                        .filter(lb -> lb instanceof Enemy && lb.getHealthPoints() > 0)
                        .findFirst()
                        .orElse(null);
                if (enemyTarget != null) {
                    teammate.assist(enemyTarget); // assist prints its own messages
                }
            }
        }
    }

    private void talkTo(String npcName) {
        LivingBeing being = player.getCurrentRoom().getLivingBeingByName(npcName);
        if (being instanceof NPC) {
            ((NPC) being).interact(player); // interact prints its own messages
        } else if (being != null) {
            outputConsumer.accept("You can't have a meaningful conversation with " + being.getName() + ".");
        } else {
            outputConsumer.accept("There's no one here called '" + npcName + "'.");
        }
    }

    private void buyFromMerchant(String itemName) {
        Merchant merchant = null;
        for (LivingBeing lb : player.getCurrentRoom().getLivingBeings()) {
            if (lb instanceof Merchant) {
                merchant = (Merchant) lb;
                break;
            }
        }

        if (merchant == null) {
            outputConsumer.accept("There is no merchant here to buy from.");
            return;
        }
        merchant.sellToPlayer(itemName, player); // sellToPlayer prints messages
    }

    private void equipItem(String itemName) {
        Item item = player.getItemFromInventory(itemName);
        if (item == null) {
            outputConsumer.accept("You don't have '" + itemName + "'.");
            return;
        }
        if (item instanceof Weapon) {
            player.equipWeapon((Weapon) item); // equipWeapon prints messages
        } else if (item instanceof Shield) {
            player.equipShield((Shield) item); // equipShield prints messages
        } else {
            outputConsumer.accept("You can't equip " + item.getName() + ".");
        }
    }

    private void unequipItem(String itemType) {
        // unequip methods print their own messages
        if ("weapon".equalsIgnoreCase(itemType) && player.getEquippedWeapon() != null) {
            player.unequipWeapon();
        } else if ("shield".equalsIgnoreCase(itemType) && player.getEquippedShield() != null) {
            player.unequipShield();
        } else if (player.getEquippedWeapon() != null && player.getEquippedWeapon().getName().equalsIgnoreCase(itemType)) {
            player.unequipWeapon();
        } else if (player.getEquippedShield() != null && player.getEquippedShield().getName().equalsIgnoreCase(itemType)) {
            player.unequipShield();
        } else {
            outputConsumer.accept("You don't have a " + itemType + " equipped or '" + itemType + "' is not 'weapon' or 'shield'.");
        }
    }
}

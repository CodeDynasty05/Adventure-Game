package com.OOP;

import com.OOP.model.core.World;
import com.OOP.model.entities.Enemy;
import com.OOP.model.entities.LivingBeing;
import com.OOP.model.entities.Player;
import com.OOP.model.interactables.Chest;
import com.OOP.model.items.Item;
import com.OOP.view.GamePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class GameGUI extends JFrame {

    private JTextArea outputArea;
    private JTextField inputField;
    private GamePanel gamePanel; // << NEW

    // ... (other UI components as before, maybe simplified if map is primary)
    private JPanel statusPanel;
    private JLabel roomLabel, healthLabel, goldLabel, equippedWeaponLabel, equippedShieldLabel;
    // Action buttons might be less prominent now, or removed if actions are key-based

    private World world;
    private Player player;
    private MainGameLogicController gameLogicController;

    public GameGUI() {
        // ... (LnF setup) ...
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.err.println("Nimbus L&F not found.");
        }


        world = new World();
        world.setupWorld();
        player = world.getPlayer();

        gameLogicController = new MainGameLogicController(world, player, this::updateGameOutput, this::updatePlayerStatusAndMap);

        setTitle("Graphical Adventure");
        // Size will be determined by GamePanel + other components
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setLocationRelativeTo(null); // Done after pack() or setSize()

        initComponents(); // This will create gamePanel
        redirectSystemStreams();

        // Add KeyListener to the component that will have focus for game play (GamePanel or JFrame)
        this.addKeyListener(new GameKeyListener());
        this.setFocusable(true); // JFrame needs to be focusable to receive key events
        this.requestFocusInWindow(); // Request focus for the JFrame

        gameLogicController.startGame(); // This will call updatePlayerStatusAndMap
        pack(); // Adjust window size to fit components
        setLocationRelativeTo(null); // Center after packing
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameGUI gui = new GameGUI();
            gui.setVisible(true);
        });
    }

    private void initComponents() {
        JPanel mainContentPane = new JPanel(new BorderLayout(5, 5));
        mainContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainContentPane);

        // --- Game Panel (Center) ---
        gamePanel = new GamePanel(player); // Initialize GamePanel
        mainContentPane.add(gamePanel, BorderLayout.CENTER);

        // --- Output Area (South or East) ---
        outputArea = new JTextArea(10, 30); // Rows, Columns
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Game Log"));
        // Decide where to put it, e.g., SOUTH
        JPanel bottomPanel = new JPanel(new BorderLayout()); // Panel to hold log and input
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        mainContentPane.add(bottomPanel, BorderLayout.SOUTH);


        // --- Input Field (Below Log) ---
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createTitledBorder("Enter Command (or use keys)"));
        inputField.addActionListener(e -> processPlayerTextInput());
        bottomPanel.add(inputField, BorderLayout.SOUTH);
        // Make sure gamePanel gets focus back after typing in inputField
        inputField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                // If focus is lost to something other than game panel, try to give it back
                // This is tricky; usually, you want the game panel to always have focus
                // Or, handle key events at JFrame level.
            }
        });


        // --- Status Panel (East or West) ---
        statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        // ... (initialize status labels as before) ...
        roomLabel = createStatusLabel("Room: N/A");
        healthLabel = createStatusLabel("HP: N/A");
        goldLabel = createStatusLabel("Gold: N/A");
        equippedWeaponLabel = createStatusLabel("Weapon: None");
        equippedShieldLabel = createStatusLabel("Shield: None");

        statusPanel.add(roomLabel);
        statusPanel.add(healthLabel);
        statusPanel.add(goldLabel);
        statusPanel.add(equippedWeaponLabel);
        statusPanel.add(equippedShieldLabel);
        mainContentPane.add(statusPanel, BorderLayout.EAST); // Example placement

        // Action buttons could be here too, or removed if focusing on key input
    }

    private JLabel createStatusLabel(String initialText) { /* ... same as before ... */
        JLabel label = new JLabel(initialText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setBorder(new EmptyBorder(2, 5, 2, 5));
        return label;
    }

    private void processPlayerTextInput() {
        String command = inputField.getText().trim();
        if (!command.isEmpty()) {
            outputArea.append("\n> " + command + "\n");
            gameLogicController.processInput(command); // Your existing logic controller
            inputField.setText("");
        }
        this.requestFocusInWindow(); // Try to return focus to JFrame for key events
    }

    public void updateGameOutput(String text) { /* ... same as before ... */
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    // Modified status updater
    public void updatePlayerStatusAndMap() {
        SwingUtilities.invokeLater(() -> {
            if (player != null && player.getCurrentRoom() != null) {
                roomLabel.setText("Room: " + player.getCurrentRoom().getName());
                healthLabel.setText("HP: " + player.getHealthPoints() + "/" + player.getMaxHealthPoints());
                goldLabel.setText("Gold: " + player.getGoldQuantity());
                equippedWeaponLabel.setText("Weapon: " + (player.getEquippedWeapon() != null ? player.getEquippedWeapon().getName() : "None"));
                equippedShieldLabel.setText("Shield: " + (player.getEquippedShield() != null ? player.getEquippedShield().getName() : "None"));

                if (gamePanel.currentRoom != player.getCurrentRoom()) {
                    gamePanel.updateRoom(player.getCurrentRoom());
                }
                gamePanel.repaint(); // Repaint the map and entities
            }
        });
    }

    private void redirectSystemStreams() { /* ... same as before ... */
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateGUIOutputCharByChar(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateGUIOutputCharByChar(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void updateGUIOutputCharByChar(final String text) { /* ... same as before ... */
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    // --- INNER CLASS FOR KEYBOARD INPUT ---
    private class GameKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int newPlayerX = player.getTileX();
            int newPlayerY = player.getTileY();
            boolean playerMoved = false;

            int keyCode = e.getKeyCode();

            // Allow typing in inputField without triggering game actions
            if (inputField.hasFocus() && keyCode != KeyEvent.VK_ENTER) {
                return;
            }
            // If Enter is pressed in inputField, processPlayerTextInput handles it.

            switch (keyCode) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    newPlayerY--;
                    playerMoved = true;
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    newPlayerY++;
                    playerMoved = true;
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    newPlayerX--;
                    playerMoved = true;
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    newPlayerX++;
                    playerMoved = true;
                    break;
                case KeyEvent.VK_SPACE: // Example: Action key (Interact/Open)
                case KeyEvent.VK_E: // Interact / Take Item / Open Chest
                    // Priority: 1. Item on current tile, 2. Chest on current tile
                    Item itemOnTile = player.getCurrentRoom().getItemAt(player.getTileX(), player.getTileY());
                    Chest chestOnTile = player.getCurrentRoom().getChestAt(player.getTileX(), player.getTileY());

                    if (itemOnTile != null) {
                        outputArea.append("\n> Taking " + itemOnTile.getName() + "...\n");
                        player.pickUpItem(itemOnTile);
                        player.getCurrentRoom().removeItem(itemOnTile);
                        updatePlayerStatusAndMap();
                    } else if (chestOnTile != null) {
                        if (!chestOnTile.isOpen()) {
                            outputArea.append("\n> Trying to open " + chestOnTile.getName() + "...\n");
                            // Attempt to open. Player might need a key or crowbar.
                            // The "open" command string needs to be specific or the logic improved.
                            // For 'E' key, let's try a generic open attempt without specifying 'with item'.
                            // The activate method in Chest/Door should handle if a crowbar is available.
                            gameLogicController.processInput("open " + chestOnTile.getName().toLowerCase());
                            // processInput will eventually call updatePlayerStatusAndMap()
                        } else {
                            // Chest is open, try to take items from it.
                            // This needs a UI for selecting items from chest or taking all.
                            // Simplification: Take the first item from the open chest.
                            if (!chestOnTile.getContents().isEmpty()) {
                                Item itemInChest = chestOnTile.getContents().get(0);
                                outputArea.append("\n> Taking " + itemInChest.getName() + " from " + chestOnTile.getName() + "...\n");
                                player.pickUpItem(itemInChest);
                                chestOnTile.getContents().remove(itemInChest); // Remove from chest
                                updatePlayerStatusAndMap();
                                if (chestOnTile.getContents().isEmpty()) {
                                    outputArea.append(chestOnTile.getName() + " is now empty.\n");
                                }
                            } else {
                                outputArea.append("\n" + chestOnTile.getName() + " is empty.\n");
                            }
                        }
                    } else {
                        // No item or chest on current tile. Check for Doors to interact with?
                        // Or try interacting with entity in front (NPC talk) - this needs facing direction.
                        // For now:
                        outputArea.append("\nNothing to directly interact with here using 'E'.\n");
                    }
                    break;

                case KeyEvent.VK_F: // Attack
                    // Find the first enemy in the room (simplistic targeting)
                    Enemy targetEnemy = null;
                    if (player.getCurrentRoom().getLivingBeings() != null) {
                        for (LivingBeing being : player.getCurrentRoom().getLivingBeings()) {
                            if (being instanceof Enemy && being.getHealthPoints() > 0) {
                                targetEnemy = (Enemy) being;
                                break;
                            }
                        }
                    }

                    if (targetEnemy != null) {
                        outputArea.append("\n> Attacking " + targetEnemy.getName() + "!\n");
                        // Use the "attack <target_name>" command for consistency with text input
                        // and to trigger enemy retaliation logic within gameLogicController.
                        gameLogicController.processInput("attack " + targetEnemy.getName().toLowerCase().replace(" ", "_"));
                        // gameLogicController.processInput will call updatePlayerStatusAndMap()
                    } else {
                        outputArea.append("\nNo enemies to attack here.\n");
                    }
                    break;
                case KeyEvent.VK_I:
                    gameLogicController.processInput("inventory");
                    break;
                // Add more key bindings for other commands (attack, use item, etc.)
            }

            if (playerMoved) {
                // Check for collision before updating player's actual tile position
                if (!gamePanel.isTileSolid(newPlayerX, newPlayerY)) {
                    // Check if moving onto a door tile
                    char targetTileChar = player.getCurrentRoom().getTileMapLayout()[newPlayerY].charAt(newPlayerX);
                    if (targetTileChar == 'D') {
                        // This is where "go north/south/etc." logic is effectively happening
                        // Find the direction moved
                        String direction = "";
                        if (newPlayerY < player.getTileY()) direction = "north";
                        else if (newPlayerY > player.getTileY()) direction = "south";
                        else if (newPlayerX < player.getTileX()) direction = "west";
                        else if (newPlayerX > player.getTileX()) direction = "east";

                        if (!direction.isEmpty()) {
                            // Use existing "go <direction>" logic which handles locked doors and room changes
                            gameLogicController.processInput("go " + direction);
                            // Player's tileX, tileY will be updated by setCurrentRoom if successful
                        }
                    } else {
                        // Normal move to a non-door tile
                        player.setTileX(newPlayerX);
                        player.setTileY(newPlayerY);
                        // Update status and map (includes repaint)
                        // Also, need to trigger any "on enter tile" logic if you have it
                        updatePlayerStatusAndMap();
                        // No enemy turn here unless your game is real-time, for turn-based, wait for specific action.
                    }
                } else {
                    outputArea.append("\nOuch! Can't move there.\n");
                    updatePlayerStatusAndMap(); // Repaint to ensure player hasn't visually "passed through"
                }
            } else if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_E || keyCode == KeyEvent.VK_I) {
                // For non-movement actions that might change game state
                updatePlayerStatusAndMap(); // Ensure UI reflects changes
            }
        }

        // Helper to get the tile the player is "facing" - very basic
        // Assumes player has a "last moved direction" or a fixed facing
        private Point getPlayerFacingTile() {
            // This needs a proper facing direction for the player.
            // For simplicity, let's check the tile directly in front based on last move,
            // or just check the current tile for interactables if no facing.
            // For now, let's assume an action key works on the player's current tile or adjacent.
            // This part needs significant fleshing out for good interaction.
            // Example: return the tile player is standing on to interact with items/chests there.
            // Or, if player has a facing direction (e.g. lastMoveWasNorth), return tileY-1 etc.
            return new Point(player.getTileX(), player.getTileY()); // Simplistic: interact with current tile
        }
    }
}
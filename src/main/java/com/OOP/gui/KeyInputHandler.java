package com.OOP.gui;

import com.OOP.MainGameLogicController;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyInputHandler implements KeyListener {

    private MainGameLogicController gameLogic;
    private RenderPanel renderPanel; // To request UI updates if needed

    // Flags for movement (if you implement visual player movement within the panel)
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    public KeyInputHandler(MainGameLogicController gameLogic, RenderPanel renderPanel) {
        this.gameLogic = gameLogic;
        this.renderPanel = renderPanel;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Example direct movement (if player visually moves on screen)
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) upPressed = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) downPressed = true;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) leftPressed = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = true;

        // Game Logic Actions
        if (code == KeyEvent.VK_E) { // Interact / Use / Open
            // This needs more context: what is the player trying to interact with?
            // For now, let's assume a generic "interact with what's in front"
            // Or, we map 'E' to a specific command that the game logic interprets.
            // Simplification: For now, 'E' could try to open the first door or chest.
            // gameLogic.processInput("open <first_door_or_chest>"); // Too vague
            // Better: "E" could trigger a context-sensitive interaction.
            // For now, let's have E attempt to 'use' a default item or 'open' a default door
            // This part is tricky without knowing what player is facing
            System.out.println("E pressed - interaction logic TBD");
        }
        if (code == KeyEvent.VK_I) { // Inventory
            gameLogic.processInput("inventory"); // This will print to console via System.out redirection
            renderPanel.repaint(); // Ensure UI updates if inventory affects display
        }
        if (code == KeyEvent.VK_SPACE) { // Attack
            // gameLogic.processInput("attack <nearest_enemy>"); // Needs target selection
            System.out.println("Space pressed - attack logic TBD");
        }
        if (code == KeyEvent.VK_ENTER) {
            // Could be used to confirm dialogue, or submit a typed command if we had a text field
        }

        // For navigation based on game logic rooms (not visual movement)
        // These should call methods that change the player's currentRoom in gameLogic
        if (code == KeyEvent.VK_NUMPAD8 || code == KeyEvent.VK_N) { // Go North (example)
            gameLogic.processInput("go north");
            renderPanel.refreshRoomGraphics(); // Update background, items, etc.
        }
        if (code == KeyEvent.VK_NUMPAD2 || code == KeyEvent.VK_S) { // Go South
            gameLogic.processInput("go south");
            renderPanel.refreshRoomGraphics();
        }
        // Add E, W for East, West if using N,S,E,W keys
        if (code == KeyEvent.VK_NUMPAD6 || code == KeyEvent.VK_D) { // Go East
            gameLogic.processInput("go east");
            renderPanel.refreshRoomGraphics();
        }
        if (code == KeyEvent.VK_NUMPAD4 || code == KeyEvent.VK_A) { // Go West
            gameLogic.processInput("go west");
            renderPanel.refreshRoomGraphics();
        }


        // More commands:
        // 'T' for Take: gameLogic.processInput("take <item_player_is_over>");
        // This requires knowing what item the player sprite is near/on.
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) upPressed = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) downPressed = false;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) leftPressed = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = false;
    }
}

package com.OOP.gui;

import com.OOP.MainGameLogicController;
import com.OOP.model.entities.LivingBeing;
import com.OOP.model.entities.Player; // Your existing Player logic class
import com.OOP.model.core.Room;
import com.OOP.model.entities.Enemy; // Your existing Enemy logic class
import com.OOP.model.items.Item;   // Your existing Item logic class
import com.OOP.utils.ImageLoader;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class RenderPanel extends JPanel implements Runnable {

    // Screen dimensions (can be adjusted)
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    public static final int TILE_SIZE = 48; // For scaling sprites

    private Thread gameThread;
    private MainGameLogicController gameLogic; // Reference to your game's logic

    // Images for entities (can be expanded to use sprite sheets for animation)
    private BufferedImage playerSprite;
    private Map<String, BufferedImage> enemySprites = new HashMap<>(); // Key: enemy name/ID
    private Map<String, BufferedImage> itemSprites = new HashMap<>();  // Key: item name/ID
    private BufferedImage currentRoomBackground;

    // Player screen position (can be fixed or dynamic)
    private int playerScreenX = SCREEN_WIDTH / 2 - TILE_SIZE / 2;
    private int playerScreenY = SCREEN_HEIGHT / 2 - TILE_SIZE / 2;
    // For world scrolling, we'd need worldX, worldY for player and offset calculations.
    // For simplicity now, we'll assume fixed positions for items/enemies in a static room view.


    public RenderPanel(MainGameLogicController gameLogic) {
        this.gameLogic = gameLogic;
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true); // Important for KeyListener

        loadResources();
    }

    private void loadResources() {
        // Load player sprite (example)
        playerSprite = ImageLoader.loadImage("/player/boy_down_1.png");
        if (playerSprite != null) {
            playerSprite = ImageLoader.scaleImage(playerSprite, TILE_SIZE, TILE_SIZE);
        }

        // Pre-load some common enemy/item sprites (expand this)
        BufferedImage slimeSprite = ImageLoader.loadImage("/monster/greenslime_down_1.png");
        if (slimeSprite != null) enemySprites.put("Grumpy Goblin", ImageLoader.scaleImage(slimeSprite, TILE_SIZE, TILE_SIZE)); // Map by name for now

        BufferedImage keySprite = ImageLoader.loadImage("/object/key.png");
        if (keySprite != null) itemSprites.put("Cell Key", ImageLoader.scaleImage(keySprite, TILE_SIZE, TILE_SIZE));

        BufferedImage potionSprite = ImageLoader.loadImage("/object/potion_red.png");
        if (potionSprite != null) itemSprites.put("Health Potion", ImageLoader.scaleImage(potionSprite, TILE_SIZE, TILE_SIZE));

        // Example: Load a default room background (replace with dynamic loading)
        currentRoomBackground = ImageLoader.loadImage("/tiles/floor01.png"); // A generic floor tile
        // For a full background image:
        // currentRoomBackground = ImageLoader.loadImage("/map_backgrounds/cell.png");
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / 60.0; // 60 FPS
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update(); // Update game state (minimal for now, mostly for animations if any)
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        // For now, this can be simple. Later, handle animations or simple movements.
        // The core game logic is updated by MainGameLogicController via KeyInputHandler.
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 1. Draw Room Background
        if (currentRoomBackground != null) {
            // If it's a tile, tile it
            if (currentRoomBackground.getWidth() <= TILE_SIZE * 2) { // Heuristic for a tile
                for (int y = 0; y < SCREEN_HEIGHT; y += currentRoomBackground.getHeight()) {
                    for (int x = 0; x < SCREEN_WIDTH; x += currentRoomBackground.getWidth()) {
                        g2.drawImage(currentRoomBackground, x, y, this);
                    }
                }
            } else { // Assume it's a full background image
                g2.drawImage(currentRoomBackground, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, this);
            }
        } else {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }


        Player currentPlayer = gameLogic.getPlayer(); // Get player from logic
        Room currentRoom = currentPlayer.getCurrentRoom();

        // 2. Draw Items on the floor
        int itemX = 50; // Simple positioning for now
        int itemY = SCREEN_HEIGHT - TILE_SIZE - 50;
        for (Item item : currentRoom.getItemsOnFloor()) {
            BufferedImage sprite = itemSprites.get(item.getName());
            if (sprite != null) {
                g2.drawImage(sprite, itemX, itemY, TILE_SIZE, TILE_SIZE, this);
                g2.setColor(Color.WHITE);
                g2.drawString(item.getName(), itemX, itemY + TILE_SIZE + 15);
                itemX += TILE_SIZE + 20;
            }
        }

        // 3. Draw Enemies
        int enemyX = SCREEN_WIDTH - TILE_SIZE - 50;
        int enemyY = 100;
        for (LivingBeing lb : currentRoom.getLivingBeings()) {
            if (lb instanceof Enemy) {
                Enemy enemy = (Enemy) lb;
                BufferedImage sprite = enemySprites.get(enemy.getName()); // Use a consistent identifier
                if (sprite == null) sprite = enemySprites.get("Grumpy Goblin"); // Fallback
                if (sprite != null) {
                    g2.drawImage(sprite, enemyX, enemyY, TILE_SIZE, TILE_SIZE, this);
                    g2.setColor(Color.WHITE);
                    g2.drawString(enemy.getName() + " (HP: " + enemy.getHealthPoints() + ")", enemyX - 30, enemyY - 10);
                    enemyY += TILE_SIZE + 30;
                }
            }
        }

        // 4. Draw Player
        if (playerSprite != null) {
            // For now, player is always center-ish.
            // Later, player's worldX, worldY would map to screen coordinates,
            // or the world would scroll around a screen-centered player.
            g2.drawImage(playerSprite, playerScreenX, playerScreenY, TILE_SIZE, TILE_SIZE, this);
            g2.setColor(Color.WHITE);
            g2.drawString(currentPlayer.getName() + " (HP: " + currentPlayer.getHealthPoints() + ")", playerScreenX, playerScreenY - 10);

        }

        // 5. Draw Game Messages (from a message queue or last message)
        // This part needs to be connected to your outputConsumer in MainGameLogicController
        // For now, a placeholder:
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        // String lastMessage = gameLogic.getLastMessage(); // You'd need to implement this
        // if (lastMessage != null) g2.drawString(lastMessage, 20, 30);


        g2.dispose();
    }

    // Call this when the room changes or items/enemies appear/disappear
    public void refreshRoomGraphics() {
        Player player = gameLogic.getPlayer();
        if (player == null) return;
        Room room = player.getCurrentRoom();
        if (room == null) return;

        // Example: Change background based on room name (very basic)
        String roomName = room.getName().toLowerCase();
        if (roomName.contains("cell")) {
            currentRoomBackground = ImageLoader.loadImage("/tiles/wall.png"); // Example for cell
        } else if (roomName.contains("hallway")) {
            currentRoomBackground = ImageLoader.loadImage("/tiles/wood.png"); // Example for hallway
        } else {
            currentRoomBackground = ImageLoader.loadImage("/tiles/grass00.png"); // Default
        }
        if (currentRoomBackground != null) {
            // Optional: scale if it's a single image meant to be a tile
            // currentRoomBackground = ImageLoader.scaleImage(currentRoomBackground, TILE_SIZE, TILE_SIZE);
        }
        repaint();
    }
}

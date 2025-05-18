package com.OOP.view;

import com.OOP.model.core.Door;
import com.OOP.model.core.Room;
import com.OOP.model.entities.Enemy;
import com.OOP.model.entities.LivingBeing;
import com.OOP.model.entities.Merchant;
import com.OOP.model.entities.Player;
import com.OOP.model.interactables.Chest;
import com.OOP.model.items.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class GamePanel extends JPanel {
    public static final int TILE_SIZE = Room.TILE_SIZE; // Use TILE_SIZE from Room
    public Room currentRoom;
    private Player player;
    // Tile and entity images (Load these once)
    private Map<Character, Tile> tileAtlas;
    private BufferedImage playerImage;
    private BufferedImage enemyImage; // Simple enemy sprite
    private BufferedImage goblinImage;
    private BufferedImage shieldImage;
    private BufferedImage potionImage;
    private BufferedImage merchantImage;
    private BufferedImage chestImage;
    private BufferedImage keyImage;
    private BufferedImage weaponImage;
    // Add more for other items/entities
    private BufferedImage crowbarImage;

    public GamePanel(Player player) {
        this.player = player;
        this.currentRoom = player.getCurrentRoom();
        setPreferredSize(new Dimension(currentRoom.getMapWidth() * TILE_SIZE, currentRoom.getMapHeight() * TILE_SIZE));
        setBackground(Color.BLACK);
        setDoubleBuffered(true); // For smoother rendering

        loadResources();
    }

    private void loadResources() {
        tileAtlas = new HashMap<>();
        // Assuming image paths are relative to the classpath root (e.g., /res/tiles/floor.png)
        tileAtlas.put('F', new Tile("floor", "/res/tiles/floor.png", false));
        tileAtlas.put('W', new Tile("wall", "/res/tiles/wall.png", true));
        tileAtlas.put('D', new Tile("door_closed", "/res/object/door.png", true)); // Initially a collision
        tileAtlas.put('.', new Tile("door_open", "/res/object/door_iron.png", false)); // '.' for open door
        // 'P' (Player start) will be treated as floor for rendering, player drawn on top
        // 'E', 'C', 'K' will also be floor, with entities drawn on top

        try {
            playerImage = loadImage("/res/entities/player.png");
            enemyImage = loadImage("/res/monster/orc_attack_down_1.png"); // Example
            chestImage = loadImage("/res/object/chest.png"); // Example
            goblinImage = loadImage("/res/monster/goblin.png");
            keyImage = loadImage("/res/object/key.png");     // Example
            weaponImage = loadImage("/res/object/sword_normal.png");
            crowbarImage = loadImage("/res/object/pickaxe.png");
            shieldImage = loadImage("/res/object/shield_wood.png");
            potionImage = loadImage("/res/object/potion_red.png");
            merchantImage = loadImage("/res/object/oldman_down_1.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage loadImage(String path) throws IOException {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("Cannot find resource: " + path);
        }
        return ImageIO.read(stream);
    }

    public void updateRoom(Room newRoom) {
        this.currentRoom = newRoom;
        // Adjust panel size if rooms can have different dimensions
        setPreferredSize(new Dimension(currentRoom.getMapWidth() * TILE_SIZE, currentRoom.getMapHeight() * TILE_SIZE));
        revalidate(); // Tell layout manager to recalculate
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (currentRoom == null || currentRoom.getTileMapLayout() == null) {
            g2d.setColor(Color.RED);
            g2d.drawString("Error: No room data to display.", 20, 20);
            return;
        }

        // 1. Draw Tiles (Door logic will be key here)
        String[] layout = currentRoom.getTileMapLayout();
        for (int row = 0; row < currentRoom.getMapHeight(); row++) {
            for (int col = 0; col < currentRoom.getMapWidth(); col++) {
                char tileChar = layout[row].charAt(col);
                Tile tileToDraw = null;

                // Basic tile lookup
                if (tileAtlas.containsKey(tileChar)) {
                    tileToDraw = tileAtlas.get(tileChar);
                }

                // Special handling for 'D' (Door) tiles
                if (tileChar == 'D') {
                    Door gameDoorObject = findDoorObjectForTile(col, row);
                    if (gameDoorObject != null && !gameDoorObject.isLocked()) {
                        tileToDraw = tileAtlas.get('.'); // Open door tile
                    } else {
                        tileToDraw = tileAtlas.get('D'); // Closed door tile (or if object not found)
                    }
                }
                // For entities P, E, G, M, C, K - draw floor beneath them
                else if ("PEGMCK".indexOf(tileChar) != -1) {
                    tileToDraw = tileAtlas.get('F'); // Draw floor
                }


                if (tileToDraw != null && tileToDraw.image != null) {
                    g2d.drawImage(tileToDraw.image, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                } else { // Fallback for unmapped chars or missing images
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    if (tileToDraw == null) { // If char not in atlas
                        g2d.setColor(Color.PINK);
                        g2d.drawString(String.valueOf(tileChar), col * TILE_SIZE + TILE_SIZE / 3, row * TILE_SIZE + TILE_SIZE * 2 / 3);
                    }
                }
            }
        }

        // 2. Draw Items on Floor
        if (currentRoom != null && currentRoom.getItemsOnFloor() != null) {
            for (Item item : currentRoom.getItemsOnFloor()) {
                if (item.getTileX() != -1 && item.getTileY() != -1) {
                    BufferedImage itemImg = null;
                    if (item instanceof Key) itemImg = keyImage;
                    else if (item instanceof Crowbar) itemImg = crowbarImage;
                    else if (item instanceof Medicine) itemImg = potionImage;
                    else if (item instanceof Weapon) itemImg = weaponImage;
                    else if (item instanceof Shield) itemImg = shieldImage;
                    // Add more specific item images here

                    if (itemImg != null) {
                        g2d.drawImage(itemImg, item.getTileX() * TILE_SIZE, item.getTileY() * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                    } else { // Fallback drawing for unknown items
                        g2d.setColor(Color.MAGENTA);
                        g2d.fillOval(item.getTileX() * TILE_SIZE + TILE_SIZE / 3, item.getTileY() * TILE_SIZE + TILE_SIZE / 3, TILE_SIZE / 3, TILE_SIZE / 3);
                    }
                }
            }
        }

        // 3. Draw Chests
        if (currentRoom != null && currentRoom.getChests() != null) {
            for (Chest chest : currentRoom.getChests()) {
                if (chest.getTileX() != -1 && chest.getTileY() != -1 && chestImage != null) {
                    g2d.drawImage(chestImage, chest.getTileX() * TILE_SIZE, chest.getTileY() * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                    if (chest.isLocked() && !chest.isOpen()) {
                        g2d.setColor(new Color(255, 0, 0, 80)); // Red tint for locked
                        g2d.fillRect(chest.getTileX() * TILE_SIZE, chest.getTileY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    } else if (chest.isOpen()) {
                        // Optional: different image for open chest or tint
                        g2d.setColor(new Color(0, 255, 0, 80)); // Green tint for open
                        g2d.fillRect(chest.getTileX() * TILE_SIZE, chest.getTileY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }

        // 4. Draw NPCs/Enemies (using their stored tileX, tileY)
        if (currentRoom != null && currentRoom.getLivingBeings() != null) {
            for (LivingBeing being : currentRoom.getLivingBeings()) {
                if (being instanceof Player) continue; // Player drawn last

                // LIVING BEINGS (Enemy, Merchant, Teammate) MUST HAVE tileX, tileY fields
                // and these must be set in World.java when they are created/placed.
                int beingTileX = -1, beingTileY = -1;
                // A common interface or base class method would be better:
                // if (being instanceof Positionable) {
                //    Point p = ((Positionable)being).getTileCoordinates();
                //    beingTileX = p.x; beingTileY = p.y;
                // }
                // For now, specific checks:
                if (being instanceof Enemy) { // Assuming Enemy now has getTileX/Y
                    beingTileX = ((Enemy) being).getTileX();
                    beingTileY = ((Enemy) being).getTileY();
                } else if (being instanceof Merchant) { // Assuming Merchant has getTileX/Y
                    beingTileX = ((Merchant) being).getTileX();
                    beingTileY = ((Merchant) being).getTileY();
                }
                // Add for Teammate if it also needs to be drawn

                if (beingTileX != -1 && beingTileY != -1) { // If position is valid
                    BufferedImage beingImg = null;
                    if (being instanceof Merchant && merchantImage != null) {
                        beingImg = merchantImage;
                    } else if (being instanceof Enemy) {
                        // Check for specific enemy types before default
                        if (being.getName().toLowerCase().contains("goblin") && goblinImage != null) {
                            beingImg = goblinImage;
                        } else if (enemyImage != null) { // Default enemy image (e.g., Orc)
                            beingImg = enemyImage;
                        }
                    }

                    if (beingImg != null) {
                        g2d.drawImage(beingImg, beingTileX * TILE_SIZE, beingTileY * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                        // Optional: Health bar for enemies
                        if (being instanceof Enemy && being.getHealthPoints() > 0 && being.getMaxHealthPoints() > 0) {
                            g2d.setColor(Color.RED);
                            int hpBarWidth = TILE_SIZE * being.getHealthPoints() / being.getMaxHealthPoints();
                            g2d.fillRect(beingTileX * TILE_SIZE, beingTileY * TILE_SIZE - 6, hpBarWidth, 5);
                            g2d.setColor(Color.BLACK);
                            g2d.drawRect(beingTileX * TILE_SIZE, beingTileY * TILE_SIZE - 6, TILE_SIZE, 5);
                        }
                    } else { // Fallback if no specific image for this being type
                        g2d.setColor(Color.ORANGE);
                        g2d.fillRect(beingTileX * TILE_SIZE + TILE_SIZE / 4, beingTileY * TILE_SIZE + TILE_SIZE / 4, TILE_SIZE / 2, TILE_SIZE / 2);
                    }
                }
            }
        }

        // 5. Draw Player
        if (playerImage != null) {
            g2d.drawImage(playerImage, player.getTileX() * TILE_SIZE, player.getTileY() * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
        }

        g2d.dispose();
    }

    // Helper to find the first occurrence of a character in the layout (very basic)
    // You'll need a more robust way to map game entities to screen coordinates
    private Point findCharInLayout(char targetChar) {
        String[] layout = currentRoom.getTileMapLayout();
        for (int row = 0; row < layout.length; row++) {
            for (int col = 0; col < layout[row].length(); col++) {
                if (layout[row].charAt(col) == targetChar) {
                    return new Point(col, row);
                }
            }
        }
        return null;
    }

    // Helper to find a Door object at specific map coordinates (conceptual)
    // This requires doors to store their map coordinates or be identified differently.
    private Door findDoorAt(int col, int row) {
        if (currentRoom == null) return null;

        Point targetPoint = new Point(col, row);
        for (Door door : currentRoom.getExits().values()) {
            if (door.getTileCoordinates() != null && door.getTileCoordinates().equals(targetPoint)) {
                return door;
            }
        }
        return null; // No door object found with these exact coordinates in its 'tileCoordinates' field
    }


    // Method to check for collision (basic)
    public boolean isTileSolid(int tileX, int tileY) {
        if (currentRoom == null || currentRoom.getTileMapLayout() == null ||
                tileY < 0 || tileY >= currentRoom.getMapHeight() ||
                tileX < 0 || tileX >= currentRoom.getMapWidth()) {
            return true; // Out of bounds is solid
        }
        char tileChar = currentRoom.getTileMapLayout()[tileY].charAt(tileX);
        Tile tile = tileAtlas.get(tileChar);

        if (tileChar == 'D') { // Special check for doors
            Door gameDoor = findDoorAt(tileX, tileY);
            if (gameDoor != null) return gameDoor.isLocked(); // Solid if locked
            return true; // Default to solid if door object not found for 'D'
        }

        return tile != null && tile.collision;
    }

    private Door findDoorObjectForTile(int tileCol, int tileRow) {
        if (currentRoom == null) return null;
        Point targetTile = new Point(tileCol, tileRow);

        for (Door door : currentRoom.getExits().values()) {
            // A door's tileCoordinates should represent its position in ONE of the rooms it connects.
            // We need to check if this door's 'tileCoordinates' field matches targetTile
            // AND if this door is an exit *from* the currentRoom.
            // (The second part is implicit if tileCoordinates are always set from room1's perspective
            // and currentRoom is room1, or from room2's perspective if currentRoom is room2, etc.)

            // Simplest check: if the door's stored tile coordinates match.
            if (door.getTileCoordinates() != null && door.getTileCoordinates().equals(targetTile)) {
                // This assumes door.tileCoordinates is relevant for the currentRoom being rendered.
                return door;
            }
            // More complex: If a door connects currentRoom and its 'other side' has coords matching
            // targetTile (this would mean door.tileCoordinates isn't what we need here, but rather
            // some other stored coordinate for its representation in the *other* room).
            // For now, we rely on `door.getTileCoordinates()` being the location of 'D' in currentRoom's map.
        }
        // Fallback or if a 'D' exists in layout but no Door object is mapped to it:
        // This can happen if layouts are manually made inconsistent with Door objects.
        // System.err.println("Warning: 'D' tile at " + targetTile + " in " + currentRoom.getName() + " does not map to a known Door object's primary coordinates.");
        return null;
    }
}

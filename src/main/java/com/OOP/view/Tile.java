package com.OOP.view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Tile {
    public BufferedImage image;
    public boolean collision = false; // Is this tile a wall/obstacle?
    public String type; // "floor", "wall", "door_closed", "door_open" etc.

    public Tile(String type, String imagePath, boolean collision) {
        this.type = type;
        this.collision = collision;
        try {
            InputStream stream = getClass().getResourceAsStream(imagePath);
            if (stream == null) {
                throw new IOException("Cannot find resource: " + imagePath);
            }
            image = ImageIO.read(stream);
        } catch (IOException e) {
            System.err.println("Error loading tile image: " + imagePath);
            e.printStackTrace();
            // Consider a default placeholder image if loading fails
            try {
                image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB); // Default size
            } catch (Exception ex) {/*ignore*/}
        }
    }
}

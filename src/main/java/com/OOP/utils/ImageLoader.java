package com.OOP.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ImageLoader {

    public static BufferedImage loadImage(String path) {
        BufferedImage image = null;
        try {
            // The path should be relative to the 'res' folder, starting with "/"
            // e.g., "/player/boy_down_1.png"
            InputStream is = ImageLoader.class.getResourceAsStream(path);
            if (is == null) {
                System.err.println("Error: Could not load image at path: " + path);
                // Return a small placeholder or throw an exception
                return new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            }
            image = ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Error loading image " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
        return image;
    }

    // Optional: A utility to scale images if needed
    public static BufferedImage scaleImage(BufferedImage originalImage, int width, int height) {
        if (originalImage == null) return null;
        BufferedImage scaledImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D g2 = scaledImage.createGraphics();
        g2.drawImage(originalImage, 0, 0, width, height, null);
        g2.dispose();
        return scaledImage;
    }
}

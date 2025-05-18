package com.OOP.model.items;

import com.OOP.model.entities.Entity;
import java.awt.Point; // Import Point

public abstract class Item extends Entity {
    private int tileX = -1; // Default to off-map
    private int tileY = -1;

    public Item(String id, String name, String description) {
        super(id, name, description);
    }

    public int getTileX() {
        return tileX;
    }

    public void setTileX(int tileX) {
        this.tileX = tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public void setTileY(int tileY) {
        this.tileY = tileY;
    }

    public Point getTileCoordinates() {
        if (tileX == -1 || tileY == -1) return null;
        return new Point(tileX, tileY);
    }

    public void setTileCoordinates(int x, int y) {
        this.tileX = x;
        this.tileY = y;
    }
    public void setTileCoordinates(Point p) {
        if (p != null) {
            this.tileX = p.x;
            this.tileY = p.y;
        } else {
            this.tileX = -1;
            this.tileY = -1;
        }
    }
}

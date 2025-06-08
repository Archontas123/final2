package com.tavuc.models.items;

import com.tavuc.models.GameObject;

/** Client-side representation of a coin drop on the ground. */
public class CoinDrop extends GameObject {
    private final String id;
    private final int amount;

    public CoinDrop(String id, int x, int y, int amount) {
        super(x, y, 20, 20);
        this.id = id;
        this.amount = amount;
    }

    public String getId() { return id; }
    public int getAmount() { return amount; }

    public void update() {
        // Static item - no behaviour
    }

    @Override
    public void draw(java.awt.Graphics2D g2d, double offsetX, double offsetY) {
        g2d.setColor(java.awt.Color.YELLOW);
        g2d.fillOval((int)(x - offsetX), (int)(y - offsetY), width, height);
    }
}

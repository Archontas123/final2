package com.tavuc.models.items;

import com.tavuc.models.GameObject;

/** Simple coin drop item that can be picked up by players. */
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

    @Override
    public void update() {
        // Coin drops have no behaviour on their own
    }
}

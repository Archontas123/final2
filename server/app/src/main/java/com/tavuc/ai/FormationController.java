package com.tavuc.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Server side formation controller providing slot offsets.
 */
public class FormationController {
    private final List<int[]> slots = new ArrayList<>();

    public FormationController(int squadSize) {
        for (int i = 0; i < squadSize; i++) {
            slots.add(new int[]{i * 30, 0});
        }
    }

    public int[] getSlot(int index) {
        if (slots.isEmpty()) {
            return new int[]{0, 0};
        }
        return slots.get(Math.floorMod(index, slots.size()));
    }
}

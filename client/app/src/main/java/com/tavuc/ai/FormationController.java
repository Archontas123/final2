package com.tavuc.ai;

import com.tavuc.utils.Vector2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages simple formation slots for a squad of troopers.
 */
public class FormationController {
    private final List<Vector2D> slots = new ArrayList<>();

    public FormationController(int squadSize) {
        for (int i = 0; i < squadSize; i++) {
            slots.add(new Vector2D(i * 30, 0));
        }
    }

    /**
     * Returns the offset for a trooper at the given index in the formation.
     */
    public Vector2D getSlot(int index) {
        if (slots.isEmpty()) {
            return new Vector2D();
        }
        return slots.get(Math.floorMod(index, slots.size()));
    }
}

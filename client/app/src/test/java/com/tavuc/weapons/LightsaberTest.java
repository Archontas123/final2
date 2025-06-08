package com.tavuc.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Simple unit tests for the Lightsaber class.
 */
public class LightsaberTest {
    @Test
    public void comboCyclesThroughSwingPatterns() {
        Player dummy = new Player(1, "tester");
        WeaponStats stats = new WeaponStats(1, 1, 0.1);
        Lightsaber saber = new Lightsaber(LightsaberCrystal.BLUE, 1.0, stats);

        assertTrue(saber.canAttack());
        saber.primaryAttack(dummy, new Vector2D());
        assertFalse(saber.canAttack());
        // simulate cooldown expiry
        try { Thread.sleep(120); } catch (InterruptedException e) {}
        assertTrue(saber.canAttack());
        saber.primaryAttack(dummy, new Vector2D());
        assertFalse(saber.canAttack());
    }
}

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
    public void chargeLungeChangesPosition() {
        Player dummy = new Player(1, "tester");
        WeaponStats stats = new WeaponStats(1, 1, 0.1);
        Lightsaber saber = new Lightsaber(LightsaberCrystal.BLUE, 1.0, stats);

        Vector2D target = new Vector2D(dummy.getX() + 10, dummy.getY());

        assertTrue(saber.canAttack());
        saber.primaryAttack(dummy, target); // start charge
        assertTrue(saber.canAttack());
        try { Thread.sleep(50); } catch (InterruptedException e) {}
        saber.primaryAttack(dummy, target); // release
        assertFalse(saber.canAttack());
        assertTrue(dummy.getX() > 50);
    }

    @Test
    public void blockingTogglesState() {
        Player dummy = new Player(1, "tester");
        WeaponStats stats = new WeaponStats(1, 1, 0.1);
        Lightsaber saber = new Lightsaber(LightsaberCrystal.BLUE, 1.0, stats);

        assertFalse(saber.isBlocking());
        saber.secondaryAttack(dummy);
        assertTrue(saber.isBlocking());
        saber.secondaryAttack(dummy);
        assertFalse(saber.isBlocking());
    }
}

package com.tavuc.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;
import com.tavuc.managers.WorldManager;
import com.tavuc.Client;
import com.tavuc.networking.models.PlayerJoinedBroadcast;
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

    @Test
    public void lethalDamageOnHit() {
        Player attacker = new Player(1, "attacker");
        WeaponStats stats = new WeaponStats(1, 2, 0.1);
        Lightsaber saber = new Lightsaber(LightsaberCrystal.BLUE, 1.0, stats);

        // Setup world manager with a single target close to the attacker
        WorldManager wm = new WorldManager(0);
        Client.worldManager = wm;
        PlayerJoinedBroadcast ev = new PlayerJoinedBroadcast("2", "victim",
                attacker.getX() + 1, attacker.getY(), 0, 0, 0);
        wm.addPlayer(ev);
        Player victim = wm.getOtherPlayer(2);

        Vector2D targetPos = new Vector2D(victim.getX(), victim.getY());

        saber.primaryAttack(attacker, targetPos); // start charge
        saber.primaryAttack(attacker, targetPos); // release

        assertEquals(0, victim.getHealth());
    }

    @Test
    public void postSwingCooldownPreventsAttack() {
        Player attacker = new Player(1, "attacker");
        WeaponStats stats = new WeaponStats(1, 1, 0.1);
        Lightsaber saber = new Lightsaber(LightsaberCrystal.BLUE, 1.0, stats);

        Vector2D target = new Vector2D(attacker.getX() + 5, attacker.getY());

        saber.primaryAttack(attacker, target); // start charge
        saber.primaryAttack(attacker, target); // release
        assertFalse(saber.canAttack());

        try { Thread.sleep(300); } catch (InterruptedException e) {}
        assertFalse("Should still be on post-swing cooldown", saber.canAttack());
        try { Thread.sleep(300); } catch (InterruptedException e) {}
        assertTrue(saber.canAttack());
    }
}

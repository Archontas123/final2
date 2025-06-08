package com.tavuc.ecs.systems;

import com.tavuc.models.entities.Player;
import org.junit.Test;
import static org.junit.Assert.*;

public class WeakPointSystemTest {
    @Test
    public void behindAttackGivesBonus() {
        Player target = new Player(1, "t");
        Player attacker = new Player(2, "a");
        // target faces east (0 radians)
        target.setDirection(0);
        attacker.setX(target.getX() - 5); // behind
        attacker.setY(target.getY());

        WeakPointSystem wps = new WeakPointSystem();
        assertTrue(wps.isBehind(attacker, target));
        int dmg = wps.applyWeakPointDamage(attacker, target, 10);
        assertTrue(dmg > 10);
    }

    @Test
    public void frontAttackNoBonus() {
        Player target = new Player(1, "t");
        Player attacker = new Player(2, "a");
        target.setDirection(0); // facing east
        attacker.setX(target.getX() + 5); // in front
        attacker.setY(target.getY());

        WeakPointSystem wps = new WeakPointSystem();
        assertFalse(wps.isBehind(attacker, target));
        int dmg = wps.applyWeakPointDamage(attacker, target, 10);
        assertEquals(10, dmg);
    }
}

package com.tavuc.ai;

import com.tavuc.models.entities.Player;
import com.tavuc.models.entities.enemies.BasicTrooper;
import com.tavuc.models.entities.enemies.TrooperWeapon;
import com.tavuc.managers.WorldManager;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TacticalCoordinatorTest {
    @Test
    public void shareTargetAssignsSameTarget() {
        WorldManager wm = new WorldManager(0);
        BasicTrooper t1 = new BasicTrooper(0,0, wm, TrooperWeapon.RIFLE,0);
        BasicTrooper t2 = new BasicTrooper(10,0, wm, TrooperWeapon.RIFLE,1);
        Player p = new Player(1, "p");
        TacticalCoordinator coord = new TacticalCoordinator(List.of(t1, t2));
        coord.shareTarget(p);
        assertSame(p, t1.getTarget());
        assertSame(p, t2.getTarget());
    }

    @Test
    public void flankOffsetVariesByIndex() {
        TacticalCoordinator coord = new TacticalCoordinator(List.of());
        int[] a = coord.getFlankOffset(0);
        int[] b = coord.getFlankOffset(1);
        assertNotEquals(a[0], b[0]);
    }
}

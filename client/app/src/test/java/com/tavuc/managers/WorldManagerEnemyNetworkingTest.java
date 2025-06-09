package com.tavuc.managers;

import com.tavuc.networking.models.EnemySpawnedBroadcast;
import com.tavuc.networking.models.EnemyUpdateBroadcast;
import com.tavuc.networking.models.EnemyRemovedBroadcast;
import org.junit.Test;
import static org.junit.Assert.*;

public class WorldManagerEnemyNetworkingTest {
    @Test
    public void enemySpawnUpdateRemoveFlow() {
        WorldManager wm = new WorldManager(0);
        EnemySpawnedBroadcast spawn = new EnemySpawnedBroadcast();
        spawn.enemyId = "5";
        spawn.enemyType = "BasicTrooper";
        spawn.x = 10;
        spawn.y = 20;
        spawn.health = 3;
        spawn.width = 20;
        spawn.height = 20;
        wm.handleEnemySpawned(spawn);
        assertEquals(1, wm.getEnemies().size());

        EnemyUpdateBroadcast update = new EnemyUpdateBroadcast();
        update.enemyId = "5";
        update.x = 15;
        update.y = 25;
        update.dx = 1;
        update.dy = 1;
        update.direction = 0.5;
        update.health = 2;
        wm.handleEnemyUpdate(update);
        com.tavuc.models.entities.enemies.Enemy e = wm.getEnemies().get(0);
        assertEquals(15, e.getX());
        assertEquals(25, e.getY());
        assertEquals(2, e.getHealth());

        EnemyRemovedBroadcast rem = new EnemyRemovedBroadcast();
        rem.enemyId = "5";
        wm.handleEnemyRemoved(rem);
        assertEquals(0, wm.getEnemies().size());
    }
}

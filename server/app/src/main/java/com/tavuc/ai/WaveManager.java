package com.tavuc.ai;

import com.tavuc.models.entities.enemies.*;
import com.tavuc.models.entities.Entity;
import java.util.*;

/**
 * Manages spawning waves of enemies on the server.
 */
public class WaveManager {
    private WaveConfiguration currentWave;
    private List<WaveConfiguration> waves = new ArrayList<>();
    private int index = 0;

    public void setWaves(List<WaveConfiguration> waves) {
        this.waves = waves;
        this.index = 0;
    }

    public List<Enemy> spawnNextWave(boolean[][] blocked, Entity target) {
        if (index >= waves.size()) return Collections.emptyList();
        currentWave = waves.get(index++);
        List<Enemy> spawned = new ArrayList<>();
        for (EnemySpawnData data : currentWave.getEnemies()) {
            for (int i = 0; i < data.count(); i++) {
                int x = 50 + i*5;
                int y = 50 + i*5;
                if (data.type() == EnemyType.TROOPER) {
                    spawned.add(new BasicTrooper(i, "t", x, y, TrooperWeapon.BLASTER, blocked, target, i));
                } else if (data.type() == EnemyType.MECH) {
                    spawned.add(new BasicMech(i, "m", x, y, blocked, target));
                }
            }
        }
        return spawned;
    }

    public WaveConfiguration getCurrentWave() {
        return currentWave;
    }
}

package com.tavuc.ai;

import com.tavuc.managers.WorldManager;
import com.tavuc.models.entities.enemies.*;
import java.util.*;

/**
 * Manages spawning waves of enemies.
 */
public class WaveManager {
    private WaveConfiguration currentWave;
    private List<WaveConfiguration> waves = new ArrayList<>();
    private int index = 0;

    public void setWaves(List<WaveConfiguration> waves) {
        this.waves = waves;
        this.index = 0;
    }

    public List<Enemy> spawnNextWave(WorldManager world) {
        if (index >= waves.size()) return Collections.emptyList();
        currentWave = waves.get(index++);
        List<Enemy> spawned = new ArrayList<>();
        for (EnemySpawnData data : currentWave.getEnemies()) {
            for (int i = 0; i < data.count(); i++) {
                double x = 50 + i*5;
                double y = 50 + i*5;
                if (data.type() == EnemyType.TROOPER) {
                    spawned.add(new BasicTrooper(x, y, world, TrooperWeapon.BLASTER));
                } else if (data.type() == EnemyType.MECH) {
                    spawned.add(new BasicMech(x, y, world));
                }
            }
        }
        return spawned;
    }

    public WaveConfiguration getCurrentWave() {
        return currentWave;
    }

    public void setCurrentWave(WaveConfiguration wave) {
        this.currentWave = wave;
    }
}

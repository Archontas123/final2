package com.tavuc.ai;

import com.tavuc.managers.WorldManager;
import com.tavuc.models.entities.enemies.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Manages spawning waves of enemies.
 */
public class WaveManager {
    private WaveConfiguration currentWave;
    private List<WaveConfiguration> waves = new ArrayList<>();
    private int index = 0;
    private Instant waveStart;

    public void setWaves(List<WaveConfiguration> waves) {
        this.waves = waves;
        this.index = 0;
    }

    public List<Enemy> spawnNextWave(WorldManager world) {
        if (index >= waves.size()) return Collections.emptyList();
        currentWave = waves.get(index++);
        waveStart = Instant.now();

        double difficulty = 1.0 + (currentWave.getWaveNumber() - 1) * 0.5;
        List<Enemy> spawned = new ArrayList<>();
        for (EnemySpawnData data : currentWave.getEnemies()) {
            int count = (int) Math.ceil(data.count() * difficulty);
            List<double[]> positions = generatePositions(count, currentWave.getSpawnPattern(), world);
            for (int i = 0; i < count; i++) {
                double[] pos = positions.get(i);
                double x = pos[0];
                double y = pos[1];
                if (data.type() == EnemyType.TROOPER) {
                    spawned.add(new BasicTrooper(x, y, world, TrooperWeapon.BLASTER, i));
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

    /** Returns true if the current wave exceeded its time limit. */
    public boolean isCurrentWaveTimedOut() {
        return currentWave != null && currentWave.getTimeLimit() != null &&
                Instant.now().isAfter(waveStart.plus(currentWave.getTimeLimit()));
    }

    /** Whether more waves remain to be spawned. */
    public boolean hasMoreWaves() {
        return index < waves.size();
    }

    private List<double[]> generatePositions(int count, SpawnPattern pattern, WorldManager world) {
        List<double[]> pos = new ArrayList<>();
        Random r = new Random();
        switch (pattern) {
            case PERIMETER -> {
                for (int i = 0; i < count; i++) {
                    int side = i % 4;
                    int p = (i / 4) * 10;
                    switch (side) {
                        case 0 -> pos.add(new double[]{0, p});
                        case 1 -> pos.add(new double[]{100, p});
                        case 2 -> pos.add(new double[]{p, 0});
                        default -> pos.add(new double[]{p, 100});
                    }
                }
            }
            case DROP_POD -> {
                for (int i = 0; i < count; i++) {
                    pos.add(new double[]{50 + r.nextInt(11) - 5, 50 + r.nextInt(11) - 5});
                }
            }
            case TELEPORT -> {
                double baseX = 50;
                double baseY = 50;
                for (int i = 0; i < count; i++) {
                    pos.add(new double[]{baseX + r.nextInt(11) - 5, baseY + r.nextInt(11) - 5});
                }
            }
            case REINFORCEMENT -> {
                for (int i = 0; i < count; i++) {
                    pos.add(new double[]{10 + r.nextInt(10), 10 + r.nextInt(10)});
                }
            }
            default -> {
                for (int i = 0; i < count; i++) {
                    pos.add(new double[]{10 + r.nextInt(10), 10 + r.nextInt(10)});
                }
            }
        }
        return pos;
    }
}

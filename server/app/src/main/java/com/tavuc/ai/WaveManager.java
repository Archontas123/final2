package com.tavuc.ai;

import com.tavuc.models.entities.enemies.*;
import com.tavuc.models.entities.Entity;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Manages spawning waves of enemies on the server.
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

    public List<Enemy> spawnNextWave(boolean[][] blocked, Entity target) {
        if (index >= waves.size()) return Collections.emptyList();
        currentWave = waves.get(index++);
        waveStart = Instant.now();

        double difficulty = 1.0 + (currentWave.getWaveNumber() - 1) * 0.5;
        List<Enemy> spawned = new ArrayList<>();
        for (EnemySpawnData data : currentWave.getEnemies()) {
            int count = (int) Math.ceil(data.count() * difficulty);
            List<int[]> positions = generatePositions(count, currentWave.getSpawnPattern(), target);
            for (int i = 0; i < count; i++) {
                int[] pos = positions.get(i);
                int x = pos[0];
                int y = pos[1];
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

    /** Returns true if the current wave has exceeded its time limit. */
    public boolean isCurrentWaveTimedOut() {
        return currentWave != null && currentWave.getTimeLimit() != null &&
                Instant.now().isAfter(waveStart.plus(currentWave.getTimeLimit()));
    }

    /** Whether there are more waves remaining. */
    public boolean hasMoreWaves() {
        return index < waves.size();
    }

    private List<int[]> generatePositions(int count, SpawnPattern pattern, Entity target) {
        List<int[]> pos = new ArrayList<>();
        Random r = new Random();
        switch (pattern) {
            case PERIMETER -> {
                for (int i = 0; i < count; i++) {
                    int side = i % 4;
                    int p = (i / 4) * 10;
                    switch (side) {
                        case 0 -> pos.add(new int[]{0, p});
                        case 1 -> pos.add(new int[]{100, p});
                        case 2 -> pos.add(new int[]{p, 0});
                        default -> pos.add(new int[]{p, 100});
                    }
                }
            }
            case DROP_POD -> {
                for (int i = 0; i < count; i++) {
                    pos.add(new int[]{50 + r.nextInt(11) - 5, 50 + r.nextInt(11) - 5});
                }
            }
            case TELEPORT -> {
                int baseX = target != null ? target.getX() : 50;
                int baseY = target != null ? target.getY() : 50;
                for (int i = 0; i < count; i++) {
                    pos.add(new int[]{baseX + r.nextInt(11) - 5, baseY + r.nextInt(11) - 5});
                }
            }
            case REINFORCEMENT -> {
                for (int i = 0; i < count; i++) {
                    pos.add(new int[]{10 + r.nextInt(10), 10 + r.nextInt(10)});
                }
            }
            default -> {
                for (int i = 0; i < count; i++) {
                    pos.add(new int[]{10 + r.nextInt(10), 10 + r.nextInt(10)});
                }
            }
        }
        return pos;
    }
}

package com.tavuc.ai;

import java.time.Duration;
import java.util.List;

/**
 * Configuration for a single wave of enemies.
 */
public class WaveConfiguration {
    private int waveNumber;
    private List<EnemySpawnData> enemies;
    private SpawnPattern spawnPattern;
    private Object events;
    private Duration timeLimit;

    public int getWaveNumber() {
        return waveNumber;
    }

    public void setWaveNumber(int waveNumber) {
        this.waveNumber = waveNumber;
    }

    public List<EnemySpawnData> getEnemies() {
        return enemies;
    }

    public void setEnemies(List<EnemySpawnData> enemies) {
        this.enemies = enemies;
    }

    public SpawnPattern getSpawnPattern() {
        return spawnPattern;
    }

    public void setSpawnPattern(SpawnPattern spawnPattern) {
        this.spawnPattern = spawnPattern;
    }

    public Duration getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Duration timeLimit) {
        this.timeLimit = timeLimit;
    }
}

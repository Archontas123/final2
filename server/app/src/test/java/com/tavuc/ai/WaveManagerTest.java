package com.tavuc.ai;

import com.tavuc.models.entities.Player;
import com.tavuc.models.entities.enemies.Enemy;
import com.tavuc.models.entities.enemies.EnemyType;
import org.junit.Test;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class WaveManagerTest {
    @Test
    public void waveProgressionAndScaling() {
        WaveConfiguration w1 = new WaveConfiguration();
        w1.setWaveNumber(1);
        w1.setTimeLimit(Duration.ofSeconds(1));
        w1.setSpawnPattern(SpawnPattern.PERIMETER);
        w1.setEnemies(List.of(new EnemySpawnData(EnemyType.TROOPER, 1)));

        WaveConfiguration w2 = new WaveConfiguration();
        w2.setWaveNumber(2);
        w2.setTimeLimit(Duration.ofSeconds(1));
        w2.setSpawnPattern(SpawnPattern.PERIMETER);
        w2.setEnemies(List.of(new EnemySpawnData(EnemyType.TROOPER, 1)));

        WaveManager wm = new WaveManager();
        wm.setWaves(Arrays.asList(w1, w2));

        boolean[][] blocked = new boolean[10][10];
        Player target = new Player(1, "p", "pw");

        List<Enemy> first = wm.spawnNextWave(blocked, target);
        assertEquals(1, first.size());
        assertEquals(1, wm.getCurrentWave().getWaveNumber());

        List<Enemy> second = wm.spawnNextWave(blocked, target);
        assertEquals(2, second.size());
        assertEquals(2, wm.getCurrentWave().getWaveNumber());
    }

    @Test
    public void waveTimeLimitExpires() throws Exception {
        WaveConfiguration w = new WaveConfiguration();
        w.setWaveNumber(1);
        w.setTimeLimit(Duration.ofMillis(1));
        w.setSpawnPattern(SpawnPattern.PERIMETER);
        w.setEnemies(List.of(new EnemySpawnData(EnemyType.TROOPER, 1)));

        WaveManager wm = new WaveManager();
        wm.setWaves(List.of(w));
        boolean[][] blocked = new boolean[1][1];
        Player target = new Player(1, "p", "pw");

        wm.spawnNextWave(blocked, target);
        Thread.sleep(5);
        assertTrue(wm.isCurrentWaveTimedOut());
    }
}

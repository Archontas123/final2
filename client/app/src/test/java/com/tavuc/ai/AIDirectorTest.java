package com.tavuc.ai;

import org.junit.Test;
import static org.junit.Assert.*;

public class AIDirectorTest {
    @Test
    public void difficultyAdjustsWithKills() {
        AIDirector director = new AIDirector();
        DifficultyScaling scaling = new DifficultyScaling();
        director.recordKill();
        director.recordKill();
        director.recordDamageDealt(20);
        director.updateDifficulty(scaling);
        assertTrue(scaling.getMultiplier() > 1.0);
    }
}

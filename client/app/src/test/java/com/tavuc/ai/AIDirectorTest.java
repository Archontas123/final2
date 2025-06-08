package com.tavuc.ai;

import org.junit.Test;
import static org.junit.Assert.*;

public class AIDirectorTest {
    @Test
    public void difficultyAdjustsWithKills() {
        AIDirector director = new AIDirector();
        director.recordKill();
        director.recordKill();
        director.updateDifficulty();
        assertTrue(director.getDifficultyMultiplier() > 1.0);
    }
}

package com.tavuc.ai;

/**
 * Coordinates high level AI features such as dynamic difficulty.
 */
public class AIDirector {
    private int playerKills;
    private int playerDeaths;
    private double difficultyMultiplier = 1.0;

    /** Records a player kill for difficulty analysis. */
    public void recordKill() { playerKills++; }

    /** Records a player death for difficulty analysis. */
    public void recordDeath() { playerDeaths++; }

    /**
     * Simple dynamic difficulty calculation adjusting multiplier between 0.5
     * and 2 based on kill/death ratio.
     */
    public void updateDifficulty() {
        double ratio = playerDeaths == 0 ? playerKills : (double) playerKills / playerDeaths;
        difficultyMultiplier = Math.max(0.5, Math.min(2.0, 1.0 + (ratio - 1) * 0.5));
    }

    public double getDifficultyMultiplier() { return difficultyMultiplier; }
}

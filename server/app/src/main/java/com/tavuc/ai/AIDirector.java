package com.tavuc.ai;

/**
 * Coordinates high level AI features on the server.
 */
public class AIDirector {
    private int playerKills;
    private int playerDeaths;
    private double difficultyMultiplier = 1.0;

    public void recordKill() { playerKills++; }
    public void recordDeath() { playerDeaths++; }

    public void updateDifficulty() {
        double ratio = playerDeaths == 0 ? playerKills : (double) playerKills / playerDeaths;
        difficultyMultiplier = Math.max(0.5, Math.min(2.0, 1.0 + (ratio - 1) * 0.5));
    }

    public double getDifficultyMultiplier() { return difficultyMultiplier; }
}

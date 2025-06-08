package com.tavuc.ai;

/**
 * Coordinates high level AI features such as dynamic difficulty.
 */
public class AIDirector {
    private int playerKills;
    private int playerDeaths;
    private double damageDealt;
    private double damageTaken;
    private double difficultyMultiplier = 1.0;

    /** Records a player kill for difficulty analysis. */
    public void recordKill() { playerKills++; }

    /** Records a player death for difficulty analysis. */
    public void recordDeath() { playerDeaths++; }
    public void recordDamageDealt(double dmg) { damageDealt += dmg; }
    public void recordDamageTaken(double dmg) { damageTaken += dmg; }

    /**
     * Simple dynamic difficulty calculation adjusting multiplier between 0.5
     * and 2 based on kill/death ratio.
     */
    public void updateDifficulty(DifficultyScaling scaling) {
        double kdRatio = playerDeaths == 0 ? playerKills : (double) playerKills / playerDeaths;
        double dmgRatio = damageTaken == 0 ? damageDealt : damageDealt / damageTaken;
        double ratio = (kdRatio + dmgRatio) / 2.0;
        difficultyMultiplier = Math.max(0.5, Math.min(2.0, 1.0 + (ratio - 1) * 0.5));
        scaling.setMultiplier(difficultyMultiplier);
    }

    public double getDifficultyMultiplier() { return difficultyMultiplier; }
}

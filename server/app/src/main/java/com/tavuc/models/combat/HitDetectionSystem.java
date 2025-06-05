package com.tavuc.models.combat;

import java.util.ArrayList;
import java.util.List;

import com.tavuc.managers.GameManager;
import com.tavuc.models.entities.Player;
import com.tavuc.models.combat.weapons.MeleeWeapon;
import com.tavuc.utils.Vector2D;

/**
 * Simple hit detection system for melee attacks.
 */
public class HitDetectionSystem {

    /**
     * Detects players hit by a melee attack.
     *
     * @param attacker The attacking player.
     * @param direction Direction of the attack (normalized).
     * @param gameManager Game manager providing access to other players.
     * @return List of players that were hit.
     */
    public List<Player> detectMeleeHits(Player attacker, Vector2D direction, GameManager gameManager) {
        List<Player> hits = new ArrayList<>();
        if (attacker == null || direction == null || gameManager == null) {
            return hits;
        }

        PlayerCombatComponent combat = attacker.getCombatComponent();
        if (combat == null || combat.getEquippedWeapon() == null) {
            return hits;
        }

        float range = combat.getEquippedWeapon().getRange();
        float arc = 0f;
        if (combat.getEquippedWeapon() instanceof MeleeWeapon) {
            arc = ((MeleeWeapon) combat.getEquippedWeapon()).getSwingArc();
        }

        Vector2D dirNorm = direction.normalize();

        for (Player target : gameManager.getPlayersInGame()) {
            if (target == attacker) continue;
            Vector2D toTarget = new Vector2D(target.getX() - attacker.getX(), target.getY() - attacker.getY());
            double distance = toTarget.magnitude();
            if (distance > range) continue;

            Vector2D toTargetNorm = toTarget.normalize();
            double dot = dirNorm.x * toTargetNorm.x + dirNorm.y * toTargetNorm.y;
            double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dot)));
            if (angle <= arc / 2.0) {
                hits.add(target);
            }
        }
        return hits;
    }
}

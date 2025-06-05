package com.tavuc.combat;

import java.util.ArrayList;
import java.util.List;

import com.tavuc.combat.weapons.MeleeWeapon;
import com.tavuc.combat.weapons.WeaponBase;
import com.tavuc.managers.GameManager;
import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

public class HitDetectionSystem {
    public List<Player> detectMeleeHits(Player attacker, Vector2D direction, GameManager game) {
        List<Player> hitPlayers = new ArrayList<>();
        PlayerCombatComponent attackerCombat = attacker.getCombatComponent();
        if (attackerCombat == null || !attackerCombat.isAttacking()) return hitPlayers;

        WeaponBase weapon = attackerCombat.getEquippedWeapon();
        if (!(weapon instanceof MeleeWeapon)) return hitPlayers;

        MeleeWeapon melee = (MeleeWeapon) weapon;
        float attackRange = melee.getRange();
        float swingArc = melee.getSwingArc();

        List<Player> potential = game.getPlayersInGame();
        Vector2D attackerPos = new Vector2D(attacker.getX(), attacker.getY());
        double attackerAngle = Math.atan2(direction.y, direction.x);

        for (Player target : potential) {
            if (target == attacker) continue;
            Vector2D targetPos = new Vector2D(target.getX(), target.getY());
            Vector2D toTarget = targetPos.subtract(attackerPos);
            double distance = toTarget.magnitude();
            if (distance <= attackRange) {
                double angleToTarget = Math.atan2(toTarget.y, toTarget.x);
                double angleDiff = Math.abs(normalizeAngle(angleToTarget - attackerAngle));
                if (angleDiff <= swingArc / 2) {
                    hitPlayers.add(target);
                }
            }
        }
        return hitPlayers;
    }

    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
}

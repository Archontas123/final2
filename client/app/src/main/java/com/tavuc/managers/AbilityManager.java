package com.tavuc.managers;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;
import com.tavuc.models.entities.Player;
import com.tavuc.Client;

/**
 * Basic ability manager handling simple force abilities. This implementation
 * runs entirely on the client for demonstration purposes.
 */
public class AbilityManager {
    private final Player player;
    private final WorldManager worldManager;
    private final InputManager input;
    private double mana = 10.0;
    private static final double MAX_MANA = 10.0;
    private int dashTicks = 0;
    private int chokeTicks = 0;
    private Player activeTarget = null;
    private Player selectedTarget = null;
    private boolean tabPrev = false;
    private int[] cooldowns = new int[7];

    public enum AbilityType {
        PULL, PUSH, CHOKE, DASH, HEAL, SHIELD, GRAB
    }

    public AbilityManager(Player player, WorldManager worldManager, InputManager input) {
        this.player = player;
        this.worldManager = worldManager;
        this.input = input;
    }

    private Player getClosestEntity() {
        if (worldManager == null) return null;
        List<Player> others = worldManager.getOtherPlayers();
        Player closest = null;
        double closestDist = Double.MAX_VALUE;
        for (Player p : others) {
            double dx = p.getX() - player.getX();
            double dy = p.getY() - player.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < closestDist) {
                closestDist = dist;
                closest = p;
            }
        }
        return closest;
    }

    private Player getDirectionalTarget() {
        if (worldManager == null) return null;
        List<Player> others = worldManager.getOtherPlayers();
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        double dir = player.getDirection();
        for (Player p : others) {
            double dx = p.getX() - player.getX();
            double dy = p.getY() - player.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            double ang = Math.atan2(dy, dx);
            double diff = Math.abs(normalizeAngle(dir - ang));
            if (diff < Math.toRadians(45) && dist < bestDist) {
                bestDist = dist;
                best = p;
            }
        }
        return best;
    }

    private double normalizeAngle(double a) {
        while (a < -Math.PI) a += Math.PI * 2;
        while (a > Math.PI) a -= Math.PI * 2;
        return a;
    }

    public Player getCurrentTarget() {
        Player dirTarget = getDirectionalTarget();
        if (dirTarget != null) return dirTarget;
        if (selectedTarget != null) return selectedTarget;
        return getClosestEntity();
    }

    private void cycleTarget() {
        if (worldManager == null) return;
        List<Player> others = new ArrayList<>(worldManager.getOtherPlayers());
        if (others.isEmpty()) {
            selectedTarget = null;
            return;
        }
        others.sort((a,b)->{
            double da = Math.hypot(a.getX()-player.getX(), a.getY()-player.getY());
            double db = Math.hypot(b.getX()-player.getX(), b.getY()-player.getY());
            return Double.compare(da, db);
        });
        int idx = selectedTarget == null ? 0 : others.indexOf(selectedTarget)+1;
        if (idx < 0 || idx >= others.size()) idx = 0;
        selectedTarget = others.get(idx);
    }

    public void update() {
        // regenerate mana
        if (mana < MAX_MANA) {
            mana = Math.min(MAX_MANA, mana + 0.02);
        }

        // handle cooldown timers
        for (int i = 0; i < cooldowns.length; i++) {
            if (cooldowns[i] > 0) cooldowns[i]--;
        }

        boolean tab = input.isKeyPressed(KeyEvent.VK_TAB);
        if (tab && !tabPrev) {
            cycleTarget();
        }
        tabPrev = tab;

        // highlight selected target each frame
        Player highlight = getCurrentTarget();
        if (highlight != null) highlight.triggerGlow(2);

        if (dashTicks > 0) {
            dashTicks--;
            if (dashTicks == 0) {
                player.setDx(0);
                player.setDy(0);
            }
        }

        if (chokeTicks > 0 && activeTarget != null) {
            chokeTicks--;
            activeTarget.setDx(0);
            activeTarget.setDy(0);
            player.setDx(0);
            player.setDy(0);
            if (chokeTicks % 60 == 0) {
                activeTarget.takeDamage(1);
            }
            activeTarget.triggerGlow(5);
        }

        if (input.isKeyPressed(KeyEvent.VK_F1)) {
            useForcePull();
        }
        if (input.isKeyPressed(KeyEvent.VK_F2)) {
            useForcePush();
        }
        if (input.isKeyPressed(KeyEvent.VK_F3)) {
            startForceChoke();
        } else {
            chokeTicks = 0;
        }
        if (input.isKeyPressed(KeyEvent.VK_F4)) {
            useForceDash();
        }
        if (input.isKeyPressed(KeyEvent.VK_F5)) {
            useForceHeal();
        }
        if (input.isKeyPressed(KeyEvent.VK_F6)) {
            handleForceShield();
        }
        if (input.isKeyPressed(KeyEvent.VK_F7)) {
            useForceGrab();
        }

        player.setMana(mana);
    }

    private void useForcePull() {
        if (mana <= 0.5 || cooldowns[0] > 0) return;
        Player target = getCurrentTarget();
        if (target == null || target == player) return;
        double dx = player.getX() - target.getX();
        double dy = player.getY() - target.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) return;
        double factor = 0.3;
        target.setDx(dx / dist * factor);
        target.setDy(dy / dist * factor);
        mana -= 0.5 / 60.0;
        target.triggerGlow(5);
        cooldowns[0] = 60;
        Client.sendAbilityUse(player.getPlayerId(), target.getPlayerId(), "PULL");
        if (Client.currentGamePanel != null) Client.currentGamePanel.triggerScreenShake(5,5);
    }

    private void useForcePush() {
        if (mana <= 0.5 || cooldowns[1] > 0) return;
        Player target = getCurrentTarget();
        if (target == null || target == player) return;
        double dx = target.getX() - player.getX();
        double dy = target.getY() - player.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) return;
        double factor = 0.3;
        target.setDx(dx / dist * factor);
        target.setDy(dy / dist * factor);
        mana -= 0.5 / 60.0;
        target.triggerGlow(5);
        cooldowns[1] = 60;
        Client.sendAbilityUse(player.getPlayerId(), target.getPlayerId(), "PUSH");
        if (Client.currentGamePanel != null) Client.currentGamePanel.triggerScreenShake(5,5);
    }

    private void startForceChoke() {
        if (mana <= 0.0 || cooldowns[2] > 0) return;
        if (chokeTicks == 0) {
            activeTarget = getCurrentTarget();
            chokeTicks = 60;
        }
        mana -= 3.0 / 60.0;
        cooldowns[2] = 60;
        if (activeTarget != null) Client.sendAbilityUse(player.getPlayerId(), activeTarget.getPlayerId(), "CHOKE");
    }

    private void useForceDash() {
        if (dashTicks > 0 || mana <= 0.5 || cooldowns[3] > 0) return;
        double angle = player.getDirection();
        player.setDx(Math.cos(angle) * 10);
        player.setDy(Math.sin(angle) * 10);
        dashTicks = 10;
        mana -= 0.5;
        cooldowns[3] = 60;
        Client.sendAbilityUse(player.getPlayerId(), null, "DASH");
        if (Client.currentGamePanel != null) Client.currentGamePanel.triggerScreenShake(5,5);
    }

    private void useForceHeal() {
        if (mana <= 2.0 || cooldowns[4] > 0) return;
        if (player.getHealth() < player.getMaxHealth()) {
            player.heal(1);
            mana -= 2.0 / 60.0;
        }
        cooldowns[4] = 60;
        Client.sendAbilityUse(player.getPlayerId(), null, "HEAL");
    }

    private void handleForceShield() {
        if (!player.isForceShieldActive()) {
            if (mana <= 1.0 || cooldowns[5] > 0) return;
            player.setForceShieldActive(true);
            cooldowns[5] = 60;
            Client.sendAbilityUse(player.getPlayerId(), null, "SHIELD");
        }
        mana -= 1.0 / 60.0;
    }

    private void useForceGrab() {
        if (mana <= 2.0 || cooldowns[6] > 0) return;
        Player target = getCurrentTarget();
        if (target == null) return;
        double angle = player.getDirection();
        double dist = 100;
        target.setX(player.getX() + Math.cos(angle) * dist);
        target.setY(player.getY() + Math.sin(angle) * dist);
        target.triggerGlow(5);
        mana -= 2.0 / 60.0;
        cooldowns[6] = 60;
        Client.sendAbilityUse(player.getPlayerId(), target.getPlayerId(), "GRAB");
        if (Client.currentGamePanel != null) Client.currentGamePanel.triggerScreenShake(5,5);
    }

    /** Returns current mana value. */
    public double getMana() {
        return mana;
    }

    /** Returns remaining cooldown ticks for ability index. */
    public int getCooldown(int idx) {
        if (idx < 0 || idx >= cooldowns.length) return 0;
        return cooldowns[idx];
    }
}

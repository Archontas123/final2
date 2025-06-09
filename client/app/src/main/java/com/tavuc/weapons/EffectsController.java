package com.tavuc.weapons;

import com.tavuc.Client;
import com.tavuc.ui.effects.MuzzleFlashParticle;
import com.tavuc.ui.effects.BladeTrailParticle;
import com.tavuc.ui.effects.ForceEffectParticle;
import com.tavuc.ui.effects.Particle;
import com.tavuc.ui.lights.DynamicLight;

/**
 * Simple dispatcher for spawning weapon visual effects.
 */
public class EffectsController {

    /** Spawn an effect at the given world coordinates. */
    public void spawn(String effect, double x, double y) {
        if (Client.currentGamePanel == null) return;
        Particle p = null;
        DynamicLight light = null;
        switch (effect) {
            case "muzzle_flash", "charged_muzzle_flash" -> {
                p = new MuzzleFlashParticle(x, y);
                light = new DynamicLight(x, y, 40f, 1f);
            }
            case "blade_trail" -> p = new BladeTrailParticle(x, y);
            case "force_effect" -> {
                p = new ForceEffectParticle(x, y);
                light = new DynamicLight(x, y, 60f, 0.8f);
            }
            case "slam_effect" -> {
                p = new ForceEffectParticle(x, y, new java.awt.Color(255, 80, 80), 20);
                light = new DynamicLight(x, y, 80f, 0.8f);
            }
            case "push_effect" -> {
                p = new ForceEffectParticle(x, y, new java.awt.Color(80, 255, 80));
                light = new DynamicLight(x, y, 60f, 0.8f);
            }
            case "choke_effect" -> {
                p = new ForceEffectParticle(x, y, new java.awt.Color(150, 50, 200));
                light = new DynamicLight(x, y, 60f, 0.8f);
            }
            // Additional effects can be added here
        }
        if (p != null) {
            Client.currentGamePanel.addParticle(p);
        }
        if (light != null) {
            Client.currentGamePanel.addLight(light);
        }
    }

    /** Backwards compatibility helper. */
    public void spawn(String effect) {
        spawn(effect, 0, 0);
    }
}

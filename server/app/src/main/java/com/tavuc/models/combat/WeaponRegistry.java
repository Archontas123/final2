package com.tavuc.models.combat;

import java.util.HashMap;
import java.util.Map;

import com.tavuc.models.combat.weapons.LightsaberWeapon;
import com.tavuc.models.combat.weapons.WeaponBase;

/**
 * Registry and factory for weapons.
 */
public class WeaponRegistry {
    private static final Map<String, WeaponBase> templates = new HashMap<>();

    static {
        registerWeapon(new LightsaberWeapon("lightsaber_1", "Basic Lightsaber", 25.0f, 2.5f));
    }

    public static void registerWeapon(WeaponBase weapon) {
        templates.put(weapon.getId(), weapon);
    }

    public static WeaponBase createWeaponInstance(String id) {
        WeaponBase template = templates.get(id);
        if (template == null) {
            return null;
        }
        try {
            return template.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}

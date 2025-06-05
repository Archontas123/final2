package com.tavuc.combat.weapons;

import java.util.HashMap;
import java.util.Map;

public class WeaponRegistry {
    private static Map<String, WeaponBase> weaponTemplates = new HashMap<>();

    static {
        registerWeapon(new LightsaberWeapon("lightsaber_1", "Basic Lightsaber", 25.0f, 2.5f));
        registerWeapon(new LightsaberWeapon("lightsaber_2", "Dual Lightsaber", 20.0f, 2.0f));
    }

    public static void registerWeapon(WeaponBase weapon) {
        weaponTemplates.put(weapon.id, weapon);
    }

    public static WeaponBase getWeaponTemplate(String id) {
        return weaponTemplates.get(id);
    }

    public static WeaponBase createWeaponInstance(String id) {
        WeaponBase template = weaponTemplates.get(id);
        if (template == null) return null;
        try {
            return template.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}

package com.tavuc.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.managers.WorldManager;
import com.tavuc.models.entities.enemies.BasicTrooper;
import com.tavuc.models.entities.enemies.TrooperWeapon;
import com.tavuc.weapons.ForceAlignment;
import com.tavuc.weapons.ForcePowers;
import com.tavuc.weapons.WeaponStats;
import com.tavuc.Client;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;
import static org.junit.Assert.*;

public class ForcePowersEnemyTargetTest {
    @Test
    public void forceAbilitiesTargetEnemies() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        java.lang.reflect.Field outField = Client.class.getDeclaredField("out");
        outField.setAccessible(true);
        outField.set(null, pw);

        WorldManager wm = new WorldManager(0);
        Client.worldManager = wm;
        Player player = new Player(1, "hero");
        BasicTrooper enemy = new BasicTrooper(5, player.getX()+1, player.getY(), wm, TrooperWeapon.BLASTER,0);
        wm.addEnemy(enemy);

        ForcePowers fp = new ForcePowers(100, ForceAlignment.LIGHT, new WeaponStats(1, player.getAttackRange(), 0));
        fp.primaryAttack(player, null); // should target enemy

        String out = sw.toString();
        assertTrue(out.contains("FORCE_ABILITY_REQUEST"));
        assertTrue(out.contains("\"targetId\":\"5\""));
    }
}

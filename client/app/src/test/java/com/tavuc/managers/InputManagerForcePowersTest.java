package com.tavuc.managers;

import com.tavuc.models.entities.Player;
import com.tavuc.weapons.ForceAlignment;
import com.tavuc.weapons.ForcePowers;
import com.tavuc.weapons.WeaponStats;
import com.tavuc.networking.models.PlayerJoinedBroadcast;
import com.tavuc.Client;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

public class InputManagerForcePowersTest {
    @Test
    public void pressingFKeysSendsAbilityRequests() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        java.lang.reflect.Field outField = Client.class.getDeclaredField("out");
        outField.setAccessible(true);
        outField.set(null, pw);

        WorldManager wm = new WorldManager(0);
        Client.worldManager = wm;
        Player player = new Player(1, "tester");
        wm.addPlayer(new PlayerJoinedBroadcast("2", "enemy", player.getX() + 1, player.getY(), 0, 0, 0));

        InputManager im = InputManager.getInstance();
        im.setPlayerTarget(player);
        im.setControlTarget(InputManager.ControlTargetType.PLAYER);
        ForcePowers fp = new ForcePowers(100, ForceAlignment.LIGHT, new WeaponStats(1, player.getAttackRange(), 0));
        im.setForcePowers(fp);

        Canvas c = new Canvas();
        im.keyPressed(new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F1, KeyEvent.CHAR_UNDEFINED));
        im.keyPressed(new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F2, KeyEvent.CHAR_UNDEFINED));
        im.keyPressed(new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F3, KeyEvent.CHAR_UNDEFINED));

        String out = sw.toString();
        assertTrue(out.contains("FORCE_ABILITY_REQUEST"));
        assertTrue(out.contains("FORCE_SLAM"));
        assertTrue(out.contains("FORCE_PUSH"));
        assertTrue(out.contains("FORCE_CHOKE"));
    }
}

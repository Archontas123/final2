package com.tavuc;

import com.tavuc.managers.GameManager;
import com.tavuc.models.entities.Player;
import com.tavuc.models.planets.Planet;
import com.tavuc.models.planets.PlanetType;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoinDropTest {
    private static class DummySession extends com.tavuc.networking.ClientSession {
        DummySession() { super(new java.net.Socket(), new com.tavuc.managers.AuthManager(), new com.tavuc.managers.LobbyManager()); }
        @Override public void sendMessage(Object o) {}
    }

    @Test
    public void coinDropSpawnedOnDeath() {
        GameManager gm = new GameManager();
        Planet p = new Planet(1, "test", PlanetType.Desert, 10, 10, 1, 1, 1L, 0, 0);
        gm.initialize(1, p, 2);
        Player a = new Player(1, "a", "pw");
        Player b = new Player(2, "b", "pw");
        gm.addPlayer(a, new DummySession());
        gm.addPlayer(b, new DummySession());
        b.addCoins(5);
        b.setHealth(0.2);
        gm.handlePlayerAttack(a.getId(), b.getId());
        assertEquals(1, gm.getCoinDrops().size());
        assertEquals(0, b.getCoins());
    }
}

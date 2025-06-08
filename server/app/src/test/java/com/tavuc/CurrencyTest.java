package com.tavuc;

import com.tavuc.models.entities.Player;
import org.junit.Test;
import static org.junit.Assert.*;

public class CurrencyTest {
    @Test
    public void coinsIncreaseAndResetOnExtraction() {
        Player p = new Player(1, "tester", "pw");
        p.addCoins(10);
        assertEquals(10, p.getCoins());
        int ex = p.extractCoins();
        assertEquals(10, ex);
        assertEquals(0, p.getCoins());
    }
}

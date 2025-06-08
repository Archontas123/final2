package com.tavuc.ai;

import com.tavuc.managers.WorldManager;
import com.tavuc.utils.Vector2D;
import org.junit.Test;

import static org.junit.Assert.*;

public class BasicPathfindingAgentTest {
    @Test
    public void simpleStraightPath() {
        WorldManager wm = new WorldManager(0);
        BasicPathfindingAgent agent = new BasicPathfindingAgent(wm);
        Vector2D move = agent.getNextMove(new Vector2D(0,0), new Vector2D(4,0));
        assertTrue(move.getX() > 0);
    }
}

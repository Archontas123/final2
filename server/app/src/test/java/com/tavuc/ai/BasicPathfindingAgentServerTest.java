package com.tavuc.ai;

import org.junit.Test;
import static org.junit.Assert.*;

public class BasicPathfindingAgentServerTest {
    @Test
    public void blockedTileNextToStart() {
        boolean[][] blocked = new boolean[3][3];
        blocked[1][0] = true; // block direct path
        BasicPathfindingAgent agent = new BasicPathfindingAgent(blocked);
        int[] mv = agent.getNextMove(0,0,2,0);
        assertArrayEquals(new int[]{0,1}, mv);
    }

    @Test
    public void dynamicObstacleUpdate() {
        boolean[][] blocked = new boolean[3][3];
        BasicPathfindingAgent agent = new BasicPathfindingAgent(blocked);
        // initially clear path
        int[] mv1 = agent.getNextMove(0,0,2,0);
        assertArrayEquals(new int[]{1,0}, mv1);
        // now add obstacle and path should change
        blocked[1][0] = true;
        int[] mv2 = agent.getNextMove(0,0,2,0);
        assertArrayEquals(new int[]{0,1}, mv2);
    }
}

package com.tavuc.ai;

import java.util.*;

/**
 * Lightweight A* pathfinding agent for the server. It operates on a simple
 * boolean grid provided at construction where {@code true} indicates a
 * blocked tile.
 */
public class BasicPathfindingAgent implements PathfindingAgent {

    /**
     * Grid of blocked tiles. Entries are {@code true} when the
     * corresponding tile is obstructed. The array reference itself is
     * final but its contents may be mutated by callers to represent
     * dynamic obstacles.
     */
    private final boolean[][] blocked;

    public BasicPathfindingAgent(boolean[][] blocked) {
        this.blocked = blocked;
    }

    private boolean isBlocked(int x, int y) {
        if (x < 0 || y < 0 || x >= blocked.length || y >= blocked[0].length) {
            return true;
        }
        return blocked[x][y];
    }

    /**
     * Performs a simple Bresenham line-of-sight check from the start to the
     * target position using the current {@code blocked} grid.
     */
    private boolean hasLineOfSight(int sx, int sy, int tx, int ty) {
        int dx = Math.abs(tx - sx);
        int dy = Math.abs(ty - sy);
        int x = sx;
        int y = sy;
        int n = 1 + dx + dy;
        int xInc = (tx > sx) ? 1 : -1;
        int yInc = (ty > sy) ? 1 : -1;
        int error = dx - dy;
        dx *= 2;
        dy *= 2;
        for (; n > 0; --n) {
            if (!(x == sx && y == sy) && isBlocked(x, y)) {
                return false;
            }
            if (error > 0) {
                x += xInc;
                error -= dy;
            } else if (error < 0) {
                y += yInc;
                error += dx;
            } else {
                x += xInc;
                y += yInc;
                error -= dy;
                error += dx;
            }
        }
        return true;
    }

    private static class Node implements Comparable<Node> {
        int x, y; double g, f; Node parent;
        Node(int x,int y,double g,double f,Node p){this.x=x;this.y=y;this.g=g;this.f=f;this.parent=p;}
        public int compareTo(Node o){return Double.compare(f,o.f);} }

    private List<int[]> computePath(int sx,int sy,int tx,int ty){
        PriorityQueue<Node> open=new PriorityQueue<>();
        Map<String,Node> visited=new HashMap<>();
        open.add(new Node(sx,sy,0,0,null));
        int[] dirs={1,0,-1,0,0,1,0,-1};
        while(!open.isEmpty()){
            Node n=open.poll();
            String k=n.x+","+n.y; if(visited.containsKey(k)) continue; visited.put(k,n);
            if(n.x==tx && n.y==ty){
                List<int[]> path=new ArrayList<>();
                while(n.parent!=null){path.add(0,new int[]{n.x,n.y}); n=n.parent;}
                return path; }
            for(int i=0;i<dirs.length;i+=2){
                int nx=n.x+dirs[i]; int ny=n.y+dirs[i+1];
                if(isBlocked(nx,ny)) continue;
                double ng=n.g+1; double h=Math.abs(nx-tx)+Math.abs(ny-ty);
                open.add(new Node(nx,ny,ng,ng+h,n));
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int[] getNextMove(int startX, int startY, int targetX, int targetY) {
        if (hasLineOfSight(startX, startY, targetX, targetY)) {
            int dx = targetX - startX;
            int dy = targetY - startY;
            if (dx != 0) dx /= Math.abs(dx);
            if (dy != 0) dy /= Math.abs(dy);
            return new int[] {dx, dy};
        }

        List<int[]> path = computePath(startX, startY, targetX, targetY);
        if (path.isEmpty()) return new int[]{0,0};
        int[] next = path.get(0);
        int dx = next[0] - startX;
        int dy = next[1] - startY;
        if (dx != 0) dx = dx/Math.abs(dx);
        if (dy != 0) dy = dy/Math.abs(dy);
        return new int[]{dx, dy};
    }
}

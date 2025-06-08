package com.tavuc.ai;

import com.tavuc.managers.WorldManager;
import com.tavuc.models.planets.Tile;
import com.tavuc.utils.Vector2D;

import java.util.*;

/**
 * Simple A* pathfinding agent operating on the world's tile grid.
 */
public class BasicPathfindingAgent implements PathfindingAgent {

    /**
     * Reference to the world manager used for querying tiles. The
     * manager instance itself is final, but its internal state can
     * change to reflect dynamic obstacles.
     */
    private final WorldManager world;

    public BasicPathfindingAgent(WorldManager world) {
        this.world = world;
    }

    private static class Node implements Comparable<Node> {
        int x, y; double g, f; Node parent;
        Node(int x, int y, double g, double f, Node parent) {
            this.x=x; this.y=y; this.g=g; this.f=f; this.parent=parent;
        }
        @Override
        public int compareTo(Node o) { return Double.compare(this.f, o.f); }
    }

    private boolean isBlocked(int x, int y) {
        Tile tile = world.getTileAt(x, y);
        return tile != null && tile.isSolid();
    }

    /**
     * Checks if there is a direct, unobstructed line between the given
     * coordinates using a Bresenham walk over the world's tiles.
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

    private List<Vector2D> computePath(int startX, int startY, int targetX, int targetY) {
        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<String, Node> visited = new HashMap<>();
        Node start = new Node(startX, startY,0,0,null);
        open.add(start);
        int[] dirs = {1,0,-1,0,0,1,0,-1};
        while(!open.isEmpty()) {
            Node n = open.poll();
            String key = n.x+","+n.y;
            if(visited.containsKey(key)) continue;
            visited.put(key,n);
            if(n.x==targetX && n.y==targetY) {
                List<Vector2D> path = new ArrayList<>();
                while(n.parent!=null){ path.add(0,new Vector2D(n.x,n.y)); n=n.parent; }
                return path;
            }
            for(int i=0;i<dirs.length;i+=2) {
                int nx=n.x+dirs[i]; int ny=n.y+dirs[i+1];
                if(isBlocked(nx,ny)) continue;
                double ng=n.g+1;
                double h=Math.abs(nx-targetX)+Math.abs(ny-targetY);
                Node nn = new Node(nx,ny,ng,ng+h,n);
                open.add(nn);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Vector2D getNextMove(Vector2D start, Vector2D target) {
        int sx = (int) start.getX();
        int sy = (int) start.getY();
        int tx = (int) target.getX();
        int ty = (int) target.getY();

        if (hasLineOfSight(sx, sy, tx, ty)) {
            Vector2D dir = new Vector2D(tx - sx, ty - sy);
            dir.normalize();
            return dir;
        }

        List<Vector2D> path = computePath(sx, sy, tx, ty);
        if (path.isEmpty()) return new Vector2D();
        Vector2D next = path.get(0);
        Vector2D dir = new Vector2D(next.getX() - start.getX(), next.getY() - start.getY());
        dir.normalize();
        return dir;
    }
}

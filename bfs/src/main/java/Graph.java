import java.util.*;

class Graph
{
    int[][] adj;
    int N;
    int S;
    int[][][] cube;
    boolean IS_CUBE;

    public Graph(int[][] adj) {
        this.adj = adj.clone();
        N = adj[0].length;
        IS_CUBE = false;
    }

    public Graph(int S) {
        this.S = S;
        N = (int) Math.round(Math.pow(S, 3));
        IS_CUBE = true;
        cube = new int[S][S][S];
        fillCube();
    }

    void fillCube() {
        for (int i = 0; i < N; ++i) {
            cube[i % S][(i / S) % S][(i / S) / S] = i;
        }
    }

    ArrayList<Integer> getNeighbours(int v) {
        ArrayList<Integer> neighbours = new ArrayList<>();

        if (IS_CUBE) {
            int x = v % S, y = (v / S) % S, z = (v / S) / S;
            if (x + 1 < S)
                neighbours.add(cube[x + 1][y][z]);
            if (y + 1 < S)
                neighbours.add(cube[x][y + 1][z]);
            if (z + 1 < S)
                neighbours.add(cube[x][y][z + 1]);
            return neighbours;
        }

        for (int u : adj[v]) {
            if (u != 0) {
                neighbours.add(u);
            }
        }
        return neighbours;
    }
}
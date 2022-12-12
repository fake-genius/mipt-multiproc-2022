import java.io.*;
import java.util.*;

// This class represents a directed graph using adjacency list
// representation
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
            for (int i = 0; i <= 1; ++i) {
                for (int j = 0; j <= 1; ++j) {
                    for (int k = 0; k <= 1; ++k) {
                        if (i + j + k > 1)
                            continue;
                        int x1 = x + i;
                        int y1 = y + j;
                        int z1 = z + k;

                        if (x1 >= S || y1 >= S || z1 >= S)
                            continue;

                        neighbours.add(cube[x1][y1][z1]);
                    }
                }
            }
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
import java.util.ArrayList;
import java.util.LinkedList;

public class BFSSequential implements BFS {
    boolean[] visited;
    LinkedList<Integer> queue;
    Graph graph;
    int[] distances;

    public BFSSequential(Graph graph) {
        this.graph = graph;
        visited = new boolean[graph.N];
        queue = new LinkedList<>();
        distances = new int[graph.N];
    }

    @Override
    public void bfs(int s)
    {
        visited[s] = true;
        queue.add(s);
        distances[s] = 0;
        int v;
        while (queue.size() != 0)
        {
            v = queue.poll();
            ArrayList<Integer> neighbours = graph.getNeighbours(v);
            for (int n : neighbours)
            {
                if (!visited[n])
                {
                    distances[n] = distances[v] + 1;
                    visited[n] = true;
                    queue.add(n);
                }
            }
        }
    }

    public int[] getDistances() {
        return distances;
    }
}

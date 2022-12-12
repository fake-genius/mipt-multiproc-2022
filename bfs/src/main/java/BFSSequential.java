import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BFSSequential implements BFS {
    Graph graph;
    LinkedList<Integer> queue;
    AtomicBoolean[] visited;
    ArrayList<Integer> distances;

    public BFSSequential(Graph graph) {
        this.graph = graph;
        visited = new AtomicBoolean[graph.N];
        for (int i = 0; i < graph.N; ++i)
            visited[i] = new AtomicBoolean(false);
        queue = new LinkedList<>();
        distances = new ArrayList<>();
        IntStream.range(0, graph.N).forEach(i -> distances.add(Integer.MAX_VALUE));
    }

    int degree(int v) {
        return graph.getNeighbours(v).size();
    }

    @Override
    public void bfs(int s) {
        List<Integer> frontier = new ArrayList<>();
        frontier.add(s);
        distances.set(s, 0);
        while (frontier.size() != 0) {
            int[] d = new int[frontier.size() + 1];
            List<Integer> frontier2 = frontier;
            d[0] = 0;
            IntStream.range(1, frontier.size() + 1).forEach(i ->
                    d[i] = degree(frontier2.get(i - 1)));
            Arrays.parallelPrefix(d, Integer::sum);
            List<Integer> frontierPrime = Arrays.asList(new Integer[d[d.length - 1]]);

            List<List<Integer>> allNeighbours = new ArrayList<>(frontier.size());
            IntStream.range(0, frontier.size()).forEach(i -> allNeighbours.add(new ArrayList<>()));
            List<Integer> frontier3 = frontier;
            IntStream.range(0, frontier.size()).forEach(i -> {
                        //System.out.println(Thread.currentThread().getName());
                        ArrayList<Integer> N = graph.getNeighbours(frontier3.get(i));
                        allNeighbours.set(i, N);
                        IntStream.range(0, degree(frontier3.get(i))).forEach(j -> {
                                    boolean tmp = visited[allNeighbours.get(i).get(j)].compareAndExchange(false, true);
                                    if (!tmp) {
                                        int tmp1 = d[i] + j;
                                        int tmp2 = allNeighbours.get(i).get(j);
                                        frontierPrime.set(tmp1, tmp2);
                                        distances.set(tmp2, distances.get(frontier3.get(i)) + 1);
                                    }
                                }
                        );
                    }
            );
            frontier = frontierPrime.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
    }

    public int[] getDistances() {
        return distances.stream().mapToInt(i -> i).toArray();
    }
}

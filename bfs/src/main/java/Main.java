public class Main {
    public static void validate(int[] distances, int S) {
        for (int i = 0; i < distances.length; ++i) {
            int x = i % S, y = (i / S) % S, z = (i / S) / S;
            int dExpected = x + y + z;
            if (distances[i] != dExpected) {
                System.out.println("Not correct! Distance of vertex number " + i + " is " + distances[i] + " " +
                        "but should be " + dExpected);
                return;
            }
        }
        System.out.println("Correct!");
    }

    public static void main(String[] args) {
        int S = 500;
        Graph graph = new Graph(S);
        BFSParallel bfsWarmUp = new BFSParallel(new Graph(200));
        bfsWarmUp.bfs(0);

        mainSeq(graph, S);
        mainPar(graph, S);
    }

    public static void mainSeq(Graph graph, int S) {
        BFSSequential bfsSeq = new BFSSequential(graph);
        long startSeq = System.nanoTime();
        bfsSeq.bfs(0);
        long endSeq = System.nanoTime();
        validate(bfsSeq.getDistances(), S);
        System.out.println("Sequential worked for " + (endSeq - startSeq) / 1000000 + "ms");
    }

    public static void mainPar(Graph graph, int S) {
        BFSParallel bfsPar = new BFSParallel(graph);
        long startPar = System.nanoTime();
        bfsPar.bfs(0);
        long endPar = System.nanoTime();
        validate(bfsPar.getDistances(), S);
        System.out.println("Parallel worked for " + (endPar - startPar) / 1000000 + "ms");
    }
}

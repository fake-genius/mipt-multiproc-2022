import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    static final AtomicInteger ops = new AtomicInteger();
    static final ReentrantLock mutex = new ReentrantLock();
    static final int N = 4;
    static final ExecutorService executor = Executors.newFixedThreadPool(N);
    static final Random random = new Random(23);
    static final boolean check = false;

    static void checkTrees(BST bst, BSTNaive bstNaive) {
        List<Integer> keysPar = bst.getTraversal();
        //System.out.println("Par tree " + keysPar);
        List<Integer> keysNaive = bstNaive.getTraversal();
        //System.out.println("Seq tree " +keysNaive);
        Collections.sort(keysNaive);
        if (keysPar.equals(keysNaive))
            System.out.println("Trees equal!");
        else
            System.out.println("------Trees not equal!------");
        bst.checkTree();
        bstNaive.checkTree();
        System.out.println();
    }

    private static void mainPar(int x, BST bst, BSTNaive bstNaive, List<Integer> keys, long deadline) throws InterruptedException {
        while (System.nanoTime() <= deadline) {
            int key = keys.get(random.nextInt(keys.size()));
            int p = random.nextInt(101);
            if (p < x) {
                if (check) {
                    mutex.lock();
                    bstNaive.insert(key);
                    mutex.unlock();
                }
                bst.insert(key);
                //if (bst.insert(key))
                    //System.out.println("insert " + key);
            } else if (p < 2 * x) {
                if (check) {
                    mutex.lock();
                    bstNaive.delete(key);
                    mutex.unlock();
                }
                bst.delete(key);
            } else if (p >= 2 * x) {
                if (check) {
                    mutex.lock();
                    bstNaive.contains(key);
                    mutex.unlock();
                }
                bst.contains(key);
                //if (bst.contains(key))
                    //System.out.println("contains " + key);
            }
            ops.getAndIncrement();
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        for (int i = 1; i <= N; i++) {
            System.out.println("Starting " + i + " threads");
            for (Integer x : List.of(0, 10, 50)) {
                BSTNaive bstNaive = new BSTNaive();
                BST bst = new BST(Integer.MAX_VALUE);

                List<Integer> keys = IntStream.range(1, 100001).boxed()
                        .collect(Collectors.toList());
                Collections.shuffle(keys);

                keys.forEach(k -> {
                    if (random.nextInt(100001) % 2 == 0) {
                        bstNaive.insert(k);
                        bst.insert(k);
                    }
                });

                List<CompletableFuture<Void>> futures = new ArrayList<>();

                long startTime = System.nanoTime();
                long endTime = System.nanoTime() + 5000000000L;


                for (int j = 0; j < i; j++) {
                    CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(
                            () -> {
                                try {
                                    mainPar(x, bst, bstNaive, keys, endTime);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            executor
                    );
                    futures.add(completableFuture);
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .exceptionally(e -> {
                            System.out.println("Error during completable future " + e);
                            return null;
                        }).get();

                double ops = Main.ops.get() * 1.0 /  ((double) (System.nanoTime() - startTime) / 1000000000);
                Main.ops.set(0);
                System.out.println("x: " + x + ", ops: " + String.format("%.3f", ops) + "/s");

                if (check) {
                    checkTrees(bst, bstNaive);
                }
            }
        }
        executor.shutdown();
    }
}

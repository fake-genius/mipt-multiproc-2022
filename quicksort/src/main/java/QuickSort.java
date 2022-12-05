import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class QuickSort extends RecursiveTask<Integer> {

    int l;
    int r;
    int[] arr;

    public QuickSort(int l, int r, int[] arr) {
        this.l = l;
        this.r = r;
        this.arr = arr;
    }

    int[] split(int l, int r, int[] arr) {
        int c = l + (r - l) / 2;
        int pivot = arr[c];

        int i = l, j = r;
        while (i <= j) {
            while (arr[i] < pivot)
                i++;
            while (arr[j] > pivot)
                j--;

            if (i <= j) {
                int tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
                i++;
                j--;
            }
        }
        return new int[] {i, j};
    }
    void seqImpl(int l, int r, int[] arr) {
        if (arr.length == 0 || l >= r)
            return;

        int[] ij = split(l, r, arr);
        seqImpl(l, ij[1], arr);
        seqImpl(ij[0], r, arr);
    }

    Integer parImpl() {
        if (l >= r)
            return null;
        int[] ij = split(l, r, arr);
        QuickSort left = new QuickSort(l, ij[1], arr);
        QuickSort right = new QuickSort(ij[0], r, arr);
        left.fork();
        right.compute();
        left.join();
        return null;
    }

    @Override
    protected Integer compute() {
        return parImpl();
    }
}

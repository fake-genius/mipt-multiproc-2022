import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void output(int[] arr, String msg) {
        System.out.print(msg);
        for (int i : arr) {
            System.out.print(i + " ");
        }
        System.out.println();
    }

    public static void measureSort(ForkJoinPool pool, boolean seq, int[] arr, String type) {
        QuickSort qs = new QuickSort(0, arr.length - 1, arr);
        long start_t = System.currentTimeMillis();
        if (seq)
            qs.seqImpl(0, arr.length - 1, arr);
        else
            pool.invoke(qs);
        long end_t = System.currentTimeMillis();
        double work_t = (end_t - start_t) / 1000.0;
        System.out.println(type + " time for array of size " + arr.length + ": " + work_t + "s. ");
    }

    public static void main(String[] args) {
        int N = 300000000;
        int[] arr_tmp0 = {19, 4, 6, 34, 100, 7, 1, 3, 9, 2, 5, 3, 4, 6};
        int[] arr_tmp1 = arr_tmp0.clone();

        Random random = new Random();
        int[] arr0 = random.ints(N / 10, 10,N / 1000).toArray();
        int[] arr1 = random.ints(N, 10,N / 1000).toArray();
        int[] arr2 = arr1.clone();

        ForkJoinPool poolPar = new ForkJoinPool(4);

        QuickSort qs_tmp0 = new QuickSort(0, arr_tmp0.length - 1, arr_tmp0);
        QuickSort qs_tmp1  = new QuickSort(0, arr_tmp1.length - 1, arr_tmp1);
        poolPar.invoke(qs_tmp0);
        qs_tmp1.seqImpl(0, arr_tmp1.length - 1, arr_tmp1);
        output(arr_tmp0, "Sequential: ");
        output(arr_tmp1, "Parallel: ");


        QuickSort qs0 = new QuickSort(0, arr0.length - 1, arr0);
        poolPar.invoke(qs0); //warm up

        measureSort(poolPar, true, arr1, "Sequential");
        measureSort(poolPar, false, arr2, "Parallel");
    }
}

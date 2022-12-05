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
    public static void main(String[] args) {
        int N = 100000000;
        int[] arr_tmp0 = {19, 4, 6, 34, 100, 7, 1, 3, 9, 2, 5, 3, 4, 6};
        int[] arr_tmp1 = arr_tmp0.clone();

        Random random = new Random();
        int[] arr0 = random.ints(N / 10, 10,N / 2).toArray();
        int[] arr1 = random.ints(N, 10,N / 2).toArray();
        int[] arr2 = arr1.clone();

        ForkJoinPool poolPar = new ForkJoinPool(4);

        QuickSort qs_tmp0 = new QuickSort(0, arr_tmp0.length - 1, arr_tmp0);
        QuickSort qs_tmp1  = new QuickSort(0, arr_tmp1.length - 1, arr_tmp1);
        poolPar.invoke(qs_tmp0);
        qs_tmp1.seqImpl(0, arr_tmp1.length - 1, arr_tmp1);
        output(arr_tmp0, "Sequential: ");
        output(arr_tmp1, "Parallel: ");


        QuickSort qs0 = new QuickSort(0, arr0.length - 1, arr0);
        QuickSort qs1 = new QuickSort(0, arr1.length - 1, arr1);
        QuickSort qs2 = new QuickSort(0, arr2.length - 1, arr2);

        //poolPar.invoke(qs0);

        long start_par = System.currentTimeMillis();
        poolPar.invoke(qs2);
        long end_par = System.currentTimeMillis();
        double work_time_par = (end_par - start_par) / 1000.0;
        System.out.println("Parallel time for array of size " + N + ": " + work_time_par + "s. ");

        long start_seq = System.currentTimeMillis();
        qs1.seqImpl(0, arr1.length - 1, arr1);
        long end_seq = System.currentTimeMillis();
        double work_time_seq = (end_seq - start_seq) / 1000.0;
        System.out.println("Sequential time for array of size " + N + ": " + work_time_seq + "s. ");
    }
}

package utils;

public class ArrayAlgorithms {
    public static void squareSort(int[] array) {
        for (int i = 0; i < array.length; ++i) {
            for (int j = 0; j < array.length; ++j) {
                if (array[i] > array[j]) {
                    int temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;
                }
            }
        }
    }
}

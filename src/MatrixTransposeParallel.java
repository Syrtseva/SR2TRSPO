import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;


public class MatrixTransposeParallel {
    public static void main(String[] args) {
        int n = 1000; // Розмір матриці n x n
        int numThreads = 4; // Кількість процесів (потоків)

        // Генеруємо випадкову матрицю M
        int[][] M = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                M[i][j] = (int) (Math.random() * 100);
            }
        }

        // Виводимо початкову матрицю M
        System.out.println("Початкова матриця M:");
        printMatrix(M);

        // Розбиваємо матрицю M на фрагменти для кожного процесу
        int fragmentSize = n / numThreads;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        Future<int[][]>[] results = new Future[numThreads];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++) {
            int startRow = i * fragmentSize;
            int endRow = (i == numThreads - 1) ? n : startRow + fragmentSize;

            results[i] = executor.submit(new TransposeTask(M, startRow, endRow));
        }

        // Збираємо результати від кожного процесу
        int[][] transposedMatrix = new int[n][n];
        for (int i = 0; i < numThreads; i++) {
            try {
                int[][] fragment = results[i].get();
                for (int row = 0; row < fragment.length; row++) {
                    for (int col = 0; col < fragment[0].length; col++) {
                        transposedMatrix[row + i * fragmentSize][col] = fragment[row][col];
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();

        // Виводимо транспоновану матрицю T
        System.out.println("Транспонована матриця T:");
        printMatrix(transposedMatrix);

        System.out.println("Час виконання: " + (endTime - startTime) + " мс");

        // Розрахунок теоретичного коефіцієнта прискорення
        double sequentialTime = sequentialTranspose(M);
        double speedup = (double) sequentialTime / (endTime - startTime);
        System.out.println("Теоретичний коефіцієнт прискорення: " + speedup);

        executor.shutdown();
    }

    // Метод для виводу матриці
    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + "\t");
            }
            System.out.println();
        }
    }

    // Метод для послідовного обчислення транспонованої матриці
    public static double sequentialTranspose(int[][] M) {
        int n = M.length;
        int[][] T = new int[n][n];
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                T[i][j] = M[j][i];
            }
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
}

class TransposeTask implements Callable<int[][]> {
    private int[][] matrix;
    private int startRow;
    private int endRow;

    public TransposeTask(int[][] matrix, int startRow, int endRow) {
        this.matrix = matrix;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    public int[][] call() {
        int numRows = endRow - startRow;
        int numCols = matrix[0].length;
        int[][] result = new int[numRows][numCols];

        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < numCols; j++) {
                result[i - startRow][j] = matrix[j][i];
            }
        }

        return result;
    }
}

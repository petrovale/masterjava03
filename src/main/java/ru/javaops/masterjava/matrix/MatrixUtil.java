package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    static class Task implements Callable<Void> {
        final int rowId;
        final int[][] matrixA, matrixB, matrixC;
        final int matrixSize;

        public Task(int[][] matrixA, int[][] matrixB, int rowId, int matrixSize, int[][] matrixC) {
            this.rowId = rowId;
            this.matrixA = matrixA;
            this.matrixB = matrixB;
            this.matrixSize = matrixSize;
            this.matrixC =matrixC;
        }

        @Override
        public Void call() {
            for (int j = 0; j < matrixSize; ++j) {
                int sum = 0;
                for (int k = 0; k < matrixSize; ++k)
                    sum = sum + matrixA[rowId][k] * matrixB[j][k];
                matrixC[rowId][j] = sum;
            }
            return null;
        }
    }

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        final int matrixBT[][] = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrixBT[j][i] = matrixB[i][j];
            }
        }

        List<Task> listFrag = new ArrayList<>();
        for (int i = 0; i < matrixSize; ++i) {
            listFrag.add(new Task(matrixA, matrixBT, i, matrixSize, matrixC));
        }
        executor.invokeAll(listFrag);
        return matrixC;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        
        int thatColumn[] = new int[matrixSize];

        for (int j = 0; j < matrixSize; j++) {
            for (int k = 0; k < matrixSize; k++) {
                thatColumn[k] = matrixB[k][j];                
            }

            for (int i = 0; i < matrixSize; i++) {
                int thisRow[] = matrixA[i];
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += thisRow[k] * thatColumn[k];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}

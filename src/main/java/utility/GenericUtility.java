package utility;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class GenericUtility {
    private GenericUtility(){ }

    /**
     * Generic print matrix
     * @param matrix
     */
    public static <E> void printMatrix(E[][] matrix){
        System.out.println("---------------");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("---------------");
    }


    public static void printMatrix(int[][] matrix){
        System.out.println("---------------");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("---------------");
    }

    public static void printMatrix(double[][] matrix){
        System.out.println("---------------");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("---------------");
    }

    public static void printMatrix(Number[][] matrix){
        System.out.println("---------------");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j].toString() + " ");
            }
            System.out.println();
        }
        System.out.println("---------------");
    }



    /**
     * Converts a number in decimal representation to a binary representation with a number of digitNumber digit
     *
     * @param value
     * @param digitNumber
     * @return
     */
    public static String digitToStringBinaryDigits(int value, int digitNumber) {
        StringBuilder postfix = new StringBuilder();
        StringBuilder prefix = new StringBuilder();

        postfix.append(Integer.toBinaryString(value));
        while ((prefix.length() + postfix.length()) < digitNumber) {
            prefix.append('0');
        }

        prefix.append(postfix);
        return prefix.toString();
    }


    /**
     * Checks if the List is not null
     * @param other
     * @return
     */
    public static <T> List<T> safeClient(List<T> other ) {
        return other == null ? Collections.emptyList() : other;
    }

}

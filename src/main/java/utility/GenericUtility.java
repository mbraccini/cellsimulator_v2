package utility;

import java.math.BigDecimal;
import java.util.Arrays;
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
    public static <T> List<T> safeClient(List<T> other) {
        return other == null ? Collections.emptyList() : other;
    }


    /**
     * From String to the an object of the specified class
     * @param clazz
     * @param value
     * @return
     */
    public static Object toObject( Class clazz, String value ) {
        if( Boolean.class == clazz ) return Boolean.parseBoolean( value );
        if( Byte.class == clazz ) return Byte.parseByte( value );
        if( Short.class == clazz ) return Short.parseShort( value );
        if( Integer.class == clazz ) return Integer.parseInt( value );
        if( Long.class == clazz ) return Long.parseLong( value );
        if( Float.class == clazz ) return Float.parseFloat( value );
        if( Double.class == clazz ) return Double.parseDouble( value );
        return value;
    }

    /**
     * From console arguments to an array of objects of the appropriate type
     * @param args
     * @param types
     * @return
     */
    public static Object[] fromArgsStringToObjects(String[] args, List<Class> types){
        Object[] a = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            a[i] = toObject(types.get(i), args[i]);
        }
        return a;
    }


}

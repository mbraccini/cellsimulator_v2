package utility;

import interfaces.network.Table;
import interfaces.tes.DifferentiationTree;
import network.NodeDeterministicImpl;
import org.apache.commons.math3.random.RandomGenerator;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GenericUtility {
    private GenericUtility(){ }

    /**
     * Log of "x" in base "base"
     * @param x
     * @param base
     * @return
     */
    public static int log(int x, int base)
    {
        return (int) (Math.log(x) / Math.log(base));
    }

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

    public static String matrixToString(Number[][] matrix){
        return Arrays.stream(matrix).map(x -> Arrays.toString(x)).collect(Collectors.joining("\n"));
    }

    public static String matrixToString(double[][] matrix){
        return Arrays.stream(matrix).map(x -> Arrays.toString(x)).collect(Collectors.joining("\n"));
    }

    public static String matrixToString(int[][] matrix){
        return Arrays.stream(matrix).map(x -> Arrays.toString(x)).collect(Collectors.joining("\n"));
    }

    public static <E> String matrixToString(E[][] matrix){
        return Arrays.stream(matrix).map(x -> Arrays.toString(x)).collect(Collectors.joining("\n"));
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

    /**
     * A measure of how the differentiation potential of a tree is.
     * @param tree
     * @return
     */
    public static double differentiationMeasure(DifferentiationTree<?> tree) {
        Integer levels = tree.getLevelsNumber();
        return IntStream.range(0, levels-1) //partono da 0, e giusto contarne uno in meno perché l'ultimo livello non ha figli
                .mapToDouble(lvl -> tree.getLevel(lvl).get().stream().mapToDouble(y -> y.branchingFactor() * (1d/(lvl + 1))).sum()).sum();

    }

    /**
     * Computes the difference without modifying the sets
     * @param setOne
     * @param setTwo
     * @param <T>
     * @return
     */
    public static <T> Set<T> setDifference(final Set<T> setOne, final Set<T> setTwo) {
        Set<T> result = new HashSet<T>(setOne);
        result.removeIf(setTwo::contains);
        return result;
    }

    public static UniqueNameGenerator newNameGenerator(final RandomGenerator rnd){
        return new UniqueNameGenerator(rnd);
    }

    public static class UniqueNameGenerator {

        static final String Az_09 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        static final String Az = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        private final RandomGenerator rnd;
        private final Set<String> alreadyUsed = new HashSet<>();

        public UniqueNameGenerator(RandomGenerator rnd) {
            this.rnd = rnd;
        }

        public String randomAlphanumericString( int len ){
            StringBuilder sb = new StringBuilder( len );
            for( int i = 0; i < len; i++ )
                sb.append( Az_09.charAt( rnd.nextInt(Az_09.length()) ) );
            return sb.toString();
        }

        public String randomAlphabeticString( int len ){
            StringBuilder sb = new StringBuilder( len );
            for( int i = 0; i < len; i++ )
                sb.append( Az.charAt( rnd.nextInt(Az.length()) ) );
            return sb.toString();
        }

        public String generateRandomAlphanumericNotAlreadyUsed(int len ) {
            return notDuplicate(() -> randomAlphanumericString(len));
        }

        public String generateRandomAlphabeticNotAlreadyUsed(int len ) {
            return notDuplicate(() -> randomAlphabeticString(len));
        }

        private String name;
        private String notDuplicate(Supplier<String> supp) {
            do {
                name = supp.get();
            } while (alreadyUsed.stream().anyMatch(x -> x.equals(name)));
            alreadyUsed.add(name);
            return name;
        }

    }


    public static void main(String args[]){
        SBMLDocument doc = null;
        try {
            doc = SBMLReader.read(new File("prova.sbml"));
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(doc.toString());
    }

}

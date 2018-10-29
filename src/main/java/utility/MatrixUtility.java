package utility;

import interfaces.tes.Atm;
import io.vavr.Tuple2;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MatrixUtility {

    private MatrixUtility() { }


    /**
     * Reorder an ATM (its matrix and header) with the main diagonal as the key
     * @param atm
     * @return
     */
    public static Tuple2<Number[][], String[]> reorderByDiagonalValuesATM(Atm<?> atm) {
        Double[][] m = atm.getMatrixCopy();
        List<Integer> indices = indicesSortedByDiagonalValues(m);
        Number[][] newMatrix = new Number[m.length][m.length];

        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                newMatrix[i][j] = m[indices.get(i)][indices.get(j)];
            }
        }

        String[] header = atm.header();
        String[] newHeader = new String[header.length];
        for (int i = 0; i < header.length; i++) {
           newHeader[i] = header[indices.get(i)];
        }
        return new Tuple2<>(newMatrix, newHeader);
    }


    /**
     * Reorder a matrix with the main diagonal as the key
     * @param m
     * @return
     */
    public static Number[][] reorderByDiagonalValues(Number[][] m) {
        List<Integer> indices = indicesSortedByDiagonalValues(m);
        Number[][] newMatrix = new Number[m.length][m.length];

        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                newMatrix[i][j] = m[indices.get(i)][indices.get(j)];
            }
        }
        return newMatrix;
    }

    /**
     * Support function for the reorder method
     * @param m
     * @return
     */
    private static List<Integer> indicesSortedByDiagonalValues(Number[][] m) {
        Number[][] temp = new Number[m.length][2];

        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                if (i == j) {
                    temp[i][0] = m[i][j];           //elemento su cui fare il sort
                    temp[i][1] = i; // indice di riga
                }
            }
        }
        Arrays.sort(temp, Comparator.comparingDouble(arr -> arr[0].doubleValue()));

        Stream<Number[]> stream = Arrays.stream(temp);
        List<Integer> indices =  stream.map(x -> x[1].intValue()).collect(Collectors.toList());
        return indices;
    }


    public static double[][] fromNumberToDoubleMatrix(Number[][] m){
        double[][] newM = new double[m.length][];
        for (int i = 0; i < m.length; i++) {
            newM[i] = new double[m[i].length];
            for (int j = 0; j < m[i].length; j++) {
                newM[i][j] = m[i][j].doubleValue();
            }
        }
        return newM;
    }

    /**
     * S_A index as defined in the work "Determining Relative Dynamic Stability of Cell States Using Boolean Network Model"
     * Jae Il Joo, Joseph X. Zhou, Sui Huang & Kwang-Hyun Cho
     */
    public static double[] stabilityIndexRelativeStabilityJoo(Atm<?> atm){
        Double[][] m = atm.getMatrixCopy();
        double[] sum = new double[m.length];

        // Sommo per colonna
        for (int j = 0; j < m.length; j++ ) {
            for (int i = 0; i < m.length; i++) {
                sum[j] += m[i][j];
            }
        }
        // Sottraggo per riga
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m.length; j++ ) {
                if (j != i) {
                    sum[i] -= m[i][j];
                }
            }
        }

        double[] newSum = Arrays.stream(sum).map(a ->  Math.round(a * 100.0) / 100.0).toArray();
        return newSum;
    }
}

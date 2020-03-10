package utility;

import interfaces.attractor.Attractors;
import interfaces.state.State;
import interfaces.tes.Atm;
import io.vavr.Tuple2;
import tes.AtmImpl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
            temp[i][0] = m[i][i];           //elemento su cui fare il sort
            temp[i][1] = i; // indice di riga
        }
        Arrays.sort(temp, Comparator.comparingDouble(arr -> arr[0].doubleValue()));

        Stream<Number[]> stream = Arrays.stream(temp);
        List<Integer> indices =  stream.map(x -> x[1].intValue()).collect(Collectors.toList());
        return indices;
    }

    /**
     * Return the min and max of the matrix's diagonal
     * @param m matrix
     * @return
     */
    public static Tuple2<Double,Double> retrieveMinMaxDiagonal(Double[][] m){
        double min = 10;
        double max = -1;

        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                if (i==j){
                    if (m[i][j] < min) {
                        min = m[i][j];
                    }
                    if (m[i][j] > max) {
                        max = m[i][j];
                    }
                }
            }
        }

        return new Tuple2<>(min,max);
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


    public static double f1_robustness_min(double[][] m) {
        /*double sum = 0.0;
        if(m.length > 0) {
            sum = m[0][0];
            for (int j = 1; j < m[0].length; j++) {
                sum -= m[0][j];
            }
        }*/
        return Math.round(m[0][0] * 100.0) / 100.0;
    }

    public static double f4_robustness_max(double[][] m) {
        return Math.round(m[m.length - 1][m.length - 1] * 100.0) / 100.0;
    }


    public static double f2_equallyDistributed(double[][] m) {
        //int attractorsNumber = m.length;
        double sum = 0;
        Double previous = null;
        for (int i = m.length - 1 ; i >= 0; i--) {
            for (int j = m[i].length - 1; j >= 0; j--) {
                if (i == j) {
                    if (Objects.nonNull(previous)) {
                        sum += previous - m[i][j];
                    }
                    previous = m[i][j];
                }
            }
        }
        //return Math.round((sum * attractorsNumber) * 100.0) / 100.0;
        return Math.round(sum * 100.0) / 100.0;

    }

    public static double f3_triangleDifference(double[][] m) {
        double[] trianglesSums = summingLowerAndUpperTriangle(m);
        double lower = trianglesSums[0];
        double upper = trianglesSums[1];
        return Math.round((upper - lower) * 100.0) / 100.0;
    }

    static double[] summingLowerAndUpperTriangle(double[][] m) {
        double[] sum = new double[2]; //lower, upper
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                if (i > j) {
                    sum[0] += m[i][j];
                } else if (j > i) {
                    sum[1] += m[i][j];
                }
            }
        }
        return sum;
    }

    /***
     * Retreive main diagonal
     * @param atm
     * @return
     */
    public static double[] mainDiagonal(Double[][] atm){
        double[] diagonal = new double[atm.length];
        for (int i = 0; i < atm.length; i++) {
            diagonal[i] = atm[i][i];
        }
        return diagonal;
    }
}

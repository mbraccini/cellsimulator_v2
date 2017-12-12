package utility;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MatrixUtility {

    private MatrixUtility() { }

    /**
     * Reorder a matrix with the main diagonal as the key
     * @param m
     * @return
     */
    public static double[][] reorderByDiagonalValues(Double[][] m) {
        List<Integer> indices = indicesSortedByDiagonalValues(m);
        double[][] newMatrix = new double[m.length][m.length];

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
    private static List<Integer> indicesSortedByDiagonalValues(Double[][] m) {
        Double[][] temp = new Double[m.length][2];

        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                if (i == j) {
                    temp[i][0] = m[i][j];           //elemento su cui fare il sort
                    temp[i][1] = Double.valueOf(i); // indice di riga
                }
            }
        }
        Arrays.sort(temp, Comparator.comparingDouble(arr -> arr[0]));

        Stream<Double[]> stream = Arrays.stream(temp);
        List<Integer> indices =  stream.map(x -> x[1].intValue()).collect(Collectors.toList());
        return indices;
    }

}

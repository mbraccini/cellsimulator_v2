package tes;


import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import utility.Files;
import utility.GenericUtility;
import utility.MatrixUtility;
import visualization.SCCGraphViz;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Algorithm that compute the TESs starting from an adjacency matrix (and exploiting the computation of the Tarjans's strongly connected components).
 *
 * @author michelebraccini
 */
public class FindGraphFromSCC {

    private List<List<Integer>> connectedComponents;
    protected Number[][] adjacencyMatrix;
    protected int[][] output;


    public FindGraphFromSCC(Number[][] adjacencyMatrix, Set<List<Integer>> cc) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.connectedComponents = new ArrayList<>(cc);
        this.output = new int[connectedComponents.size()][connectedComponents.size()];
    }

    public Tuple2<List<List<Integer>>, int[][]> get() {
        boolean outboundArc = false;

        for (int icc = 0; icc < connectedComponents.size(); icc++) {
            List<Integer> analyzedComponent = connectedComponents.get(icc);
            outboundArc = false;
            //System.out.println("componente "+sccComponentAnalyzed);

            for (Integer index : analyzedComponent) {
                for (Integer i : IntStream.range(0, adjacencyMatrix.length).boxed().collect(Collectors.toList())) {

                    if (adjacencyMatrix[index][i].doubleValue() > 0.0 && !analyzedComponent.stream().anyMatch(x -> x.intValue() == i.intValue())) {
                        output[icc][retrieveIndexOfConnectedComponents(i)] = 1;
                    }
                }

            }
        }
        return new Tuple2<>(connectedComponents, output);
    }


    private int retrieveIndexOfConnectedComponents(int i) {
        for (int j = 0; j < connectedComponents.size(); j++) {
            if (connectedComponents.get(j).stream().anyMatch(y -> y.intValue() == i)){
                return j;
            }
        }
        return -1;
    }


    public static void main(String[] args) {
        Double[][] adjacencyMatrix = new Double[][]{
                //0	   1	 2
                {0.0, 1.0, 0.0},

                {0.0, 0.5, 0.5},

                {0.0, 0.5, 0.5}

        };


        Double[][] adjacencyMatrix2 = new Double[][] {
                //0	   1	2    3    4
                {0.0, 0.0, 0.3, 0.4, 0.0},

                {0.2, 0.0, 0.0, 0.0, 0.0},

                {0.0, 0.05, 0.0, 0.0, 0.0},

                {0.0, 0.0, 0.0, 0.0, 0.1},

                {0.0, 0.0, 0.0, 0.0, 0.0}

        };

        Set<List<Integer>> scc = new SCCTarjanAlgorithm(adjacencyMatrix).getSCCComponents();
        System.out.println(scc);
        Tuple2<List<List<Integer>>, int[][]> t = new FindGraphFromSCC(adjacencyMatrix, scc).get();
        System.out.println(t.v1());
        GenericUtility.printMatrix(t.v2());

        new SCCGraphViz(t).saveOnDisk("TUPLE");

        GenericUtility.printMatrix(Files.readCsvMatrix( "/Users/michelebraccini/IdeaProjects/cellsimulator_v2/GeneticAlg/originalATM.csv",';', false));

    }
}


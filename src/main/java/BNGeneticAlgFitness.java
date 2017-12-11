import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import interfaces.attractor.Generator;
import interfaces.attractor.ImmutableAttractor;
import interfaces.attractor.ImmutableList;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.networkdescription.ExplicitFunExpr;
import interfaces.networkdescription.NameExpr;
import interfaces.networkdescription.NetworkAST;
import interfaces.networkdescription.TopologyExpr;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import io.jenetics.*;
import network.BooleanNetworkFactory;
import network.NaiveBNParser;
import noise.CompletePerturbations;
import simulator.AttractorsFinderService;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static utility.GenericUtility.printMatrix;


public class BNGeneticAlgFitness {
    private BNGeneticAlgFitness() {

    }

    public static int BINARY_DIGIT_NUMBER = (int) Math.round(Math.pow(2, MainJenetics.K));

    public static double eval(Genotype<IntegerGene> gt) {

        BooleanNetwork<BitSet, Boolean> bn = fromGenotypeToBN(gt);
        Double[][] atm = simulateBN(bn);
        double[][] sorted = reorder(atm);
        return f1_robustness(sorted) + f2_equallyDistributed(sorted) + f3_triangleDifference(sorted);
    }

    private static double f1_robustness(double[][] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                if (i == j) {
                    sum += m[i][j];
                } else {
                    sum -= m[i][j];
                }
            }
        }
        return sum;
    }
    private static double f2_equallyDistributed(double[][] m) {
        int attractorsNumber = m.length;
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
        return sum * attractorsNumber;
    }

    private static double f3_triangleDifference(double[][] m) {
        double[] trianglesSums = summingLowerAndUpperTriangle(m);
        double lower = trianglesSums[0];
        double upper = trianglesSums[1];
        return upper - lower;
    }

    private static BooleanNetwork<BitSet, Boolean> fromGenotypeToBN(Genotype<IntegerGene> gt) {
        /**
         * INPUT_NODES
         */
        Chromosome<IntegerGene> inputNodes = gt.getChromosome(0);

        List<List<IntegerGene>> topology = new ArrayList<>();
        List<IntegerGene> node = new ArrayList<>();

        for (int i = 0, nodeNumber = 1; i < inputNodes.length(); i++) {
            node.add(inputNodes.getGene(i));
            if (i == (nodeNumber * MainJenetics.K) - 1) {
                //Node found
                topology.add(new ArrayList<>(node));
                node.clear();
                nodeNumber++;
            }
        }
        List<TopologyExpr> topologyExprList = new ArrayList<>();
        for (int nodeIndex = 0; nodeIndex < topology.size(); nodeIndex++) {
            topologyExprList.add(new NaiveBNParser.TopologyExprImpl("" + nodeIndex, topology.get(nodeIndex).stream().map(x -> "" + x.getAllele()).collect(Collectors.toList())));
        }

        /**
         * BOOLEAN_FUNCTIONS
         */
        Chromosome<IntegerGene> booleanFunctions = gt.getChromosome(1);
        List<ExplicitFunExpr> explicitFunExprList = new ArrayList<>();
        List<NameExpr> nameExprList = new ArrayList<>();

        // List<String> booleanFunctionsOutput = booleanFunctions.stream().map(x -> ).collect(Collectors.toList());
        for (int bfIndex = 0; bfIndex < booleanFunctions.length(); bfIndex++) {
            explicitFunExprList.add(new NaiveBNParser.ExplicitFunExprImpl("" + bfIndex, convertNumToBitString(booleanFunctions.getGene(bfIndex).getAllele(), BINARY_DIGIT_NUMBER)));
            nameExprList.add(new NaiveBNParser.NameExprImpl("" + bfIndex, "gene_" + bfIndex));
        }

        NetworkAST ast = new NaiveBNParser.AST(topologyExprList, explicitFunExprList, new ArrayList<>() , nameExprList);

        return BooleanNetworkFactory.newNetworkFromAST(ast);
    }

    private static Double[][] simulateBN(BooleanNetwork<BitSet, Boolean> bn) {
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
        ImmutableList<ImmutableAttractor<BinaryState>> attractors = new AttractorsFinderService<BinaryState>(generator, dynamics).call();
        Callable<Atm<BinaryState>> cp = new CompletePerturbations(attractors, dynamics, 50000);
        Atm<BinaryState> atm = null;
        try {
            atm = cp.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return atm.getMatrixCopy();
    }

    /**
     * Converts a number in decimal representation to a binary representation with a number of digitNumber digit
     *
     * @param value
     * @param digitNumber
     * @return
     */
    public static String convertNumToBitString(int value, int digitNumber) {
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
     * Reorder a matrix with the main diagonal as the key
     * @param m
     * @return
     */
    private static double[][] reorder(Double[][] m) {
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




    static Double[][] matrix = new Double[][] {
            // 0	1	  2
            {0.3, 0.7, 0.2},

            {0.8, 0.1, 0.6},

            {0.4, 0.9, 0.1}

    };

    public static void main (String [] args) {


        double [][] n = reorder(matrix);
        printMatrix(n);
        double [] m =  summingLowerAndUpperTriangle(n);
        //System.out.println(f1_robustness(n));
        System.out.println(f2_equallyDistributed(n));
    }


    }

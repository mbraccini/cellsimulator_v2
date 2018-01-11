package experiments.selfLoop;

import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
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
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import network.BooleanNetworkFactory;
import network.NaiveBNParser;
import noise.CompletePerturbations;
import simulator.AttractorsFinderService;
import utility.Constant;
import utility.Files;
import utility.GenericUtility;
import utility.MatrixUtility;
import visualization.AtmGraphViz;
import visualization.BNGraphViz;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;



public class BNGeneticAlgFitness {
    private BNGeneticAlgFitness() {

    }


    public static double eval(Genotype<IntegerGene> gt) {
        BooleanNetwork<BitSet, Boolean> bn = fromGenotypeToBN(gt, MainJenetics.K);
        BigDecimal[][] atm = simulateBN(bn).getMatrixCopy();
        Number[][] sorted = MatrixUtility.reorderByDiagonalValues(atm);
        double[][] doubleSorted = fromNumberToDoubleMatrix(sorted);
        return f1_robustness(doubleSorted) + f2_equallyDistributed(doubleSorted) + f3_triangleDifference(doubleSorted);
    }

    private static double[][] fromNumberToDoubleMatrix(Number[][] m){
        double[][] newM = new double[m.length][m.length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m.length; j++) {
                newM[i][j] = m[i][j].doubleValue();
            }
        }
        return newM;
    }

    private static double f1_robustness(double[][] m) {
        double minSum = 1.0;
        for (int i = 0; i < m.length; i++) {
            double currentSum = 0;
            for (int j = 0; j < m[i].length; j++) {
                if (i == j) {
                    currentSum += m[i][j];
                } else {
                    currentSum -= m[i][j];
                }
            }
            if (currentSum < minSum) {
                minSum = currentSum;
            }
        }
        return Math.round((minSum) * 100.0) / 100.0;
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
        return Math.round((sum * attractorsNumber) * 100.0) / 100.0;

    }

    private static double f3_triangleDifference(double[][] m) {
        double[] trianglesSums = summingLowerAndUpperTriangle(m);
        double lower = trianglesSums[0];
        double upper = trianglesSums[1];
        return Math.round((upper - lower) * 100.0) / 100.0;
    }

    public static BooleanNetwork<BitSet, Boolean> fromGenotypeToBN(Genotype<IntegerGene> gt, int k) {
        final int BINARY_DIGIT_NUMBER = (int) Math.round(Math.pow(2, k));
        /**
         * INPUT_NODES
         */
        Chromosome<IntegerGene> inputNodes = gt.getChromosome(0);

        List<List<IntegerGene>> topology = new ArrayList<>();
        List<IntegerGene> node = new ArrayList<>();

        for (int i = 0, nodeNumber = 1; i < inputNodes.length(); i++) {
            node.add(inputNodes.getGene(i));
            if (i == (nodeNumber * k) - 1) {
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

        for (int bfIndex = 0; bfIndex < booleanFunctions.length(); bfIndex++) {
            explicitFunExprList.add(new NaiveBNParser.ExplicitFunExprImpl("" + bfIndex, GenericUtility.digitToStringBinaryDigits(booleanFunctions.getGene(bfIndex).getAllele(), BINARY_DIGIT_NUMBER)));
            nameExprList.add(new NaiveBNParser.NameExprImpl("" + bfIndex, "gene_" + bfIndex));
        }

        NetworkAST ast = new NaiveBNParser.AST(topologyExprList, explicitFunExprList, new ArrayList<>() , nameExprList);

        return BooleanNetworkFactory.newNetworkFromAST(ast);
    }

    public static Atm<BinaryState> simulateBN(BooleanNetwork<BitSet, Boolean> bn) {
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
        ImmutableList<ImmutableAttractor<BinaryState>> attractors = new AttractorsFinderService<BinaryState>(generator, dynamics).call();
        Callable<Atm<BinaryState>> cp = new CompletePerturbations(attractors, dynamics, Constant.PERTURBATIONS_CUTOFF);
        Atm<BinaryState> atm = null;
        try {
            atm = cp.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return atm;
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

    static double[][] matrix2 = new double[][] {
            // 0	1	  2
            {0.3, 0.7, 0.2},

            {0.8, 0.1, 0.6},

            {0.4, 0.9, 0.1}

    };

    public static void main (String [] args) {


        String genotype = "/Users/michelebraccini/IdeaProjects/cellsimulator_v2/build/libs/GeneticAlg/BestGenotype.ser";
        System.out.println(genotype);
        BooleanNetwork<BitSet, Boolean> bn = fromGenotypeToBN((Genotype<IntegerGene>) Files.deserializeObject(genotype), 2);
        System.out.println(bn);
        Atm<BinaryState> atm = simulateBN(bn);

        String path = "/Users/michelebraccini/IdeaProjects/cellsimulator_v2/build/libs/GeneticAlg";
        Files.createDirectories(path);

        Files.writeMatrixToCsv(atm.getMatrixCopy(), path + "originalATM");
        Files.writeMatrixToCsv(MatrixUtility.reorderByDiagonalValues(atm.getMatrixCopy()), path + "sortedATM");

        new AtmGraphViz(atm, path + "atm").generateDotFile().generateImg("jpg");
        new BNGraphViz<BitSet, Boolean>(bn, path + "bn").generateDotFile().generateImg("jpg");


    }


    }

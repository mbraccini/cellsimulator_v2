package experiments.selfLoop;

import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import interfaces.attractor.Attractors;
import interfaces.sequences.Generator;
import interfaces.attractor.ImmutableAttractor;
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
import io.jenetics.ext.moea.Vec;
import network.BooleanNetworkFactory;
import network.NaiveBNParser;
import noise.CompletePerturbations;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import simulator.AttractorsFinderService;
import tes.FindGraphFromSCC;
import tes.SCCTarjanAlgorithm;
import utility.Constant;
import utility.Files;
import utility.GenericUtility;
import utility.MatrixUtility;
import visualization.AtmGraphViz;
import visualization.BNGraphViz;
import visualization.SCCGraphViz;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;



public class GeneticAlgFitness {
    private GeneticAlgFitness() {

    }


    public static class Tuple3Extended extends Number implements Comparable<Tuple3Extended> {

        private Tuple3<Double, Double, Double> tuple;
        double sum;

        public Tuple3Extended(double a, double b, double c) {
            this.tuple = new Tuple3<>(a,b,c);
            this.sum = a+b+c;
        }

        @Override
        public int intValue() {
            return (int)sum;
        }

        @Override
        public long longValue() {
            return (long)sum;
        }

        @Override
        public float floatValue() {
            return (float)sum;
        }

        @Override
        public double doubleValue(){
            return sum;
        }

        @Override
        public int compareTo(Tuple3Extended o){
            if (tuple.equals(o.tuple)) {
                return 0;
            } else {
                if (sum - o.sum < 0){
                    return -1;
                } else {
                    return +1;
                }
            }
        }

        @Override
        public String toString() {
            return tuple.toString();
        }

        public static void main(String args[]){
            Tuple3Extended a = new Tuple3Extended(8.0,4.0,5.0);
            Tuple3Extended b = new Tuple3Extended(8.01,4.0,5.0);
            System.out.println(a.compareTo(b));
            System.out.println(a.intValue());
            System.out.println(a);

        }
    }


    public static Tuple3Extended eval(Genotype<IntegerGene> gt) {
        BooleanNetwork<BitSet, Boolean> bn = fromGenotypeToBN(gt, MainJenetics.K);
        return eval(bn);
    }

    public static Tuple3Extended eval(BooleanNetwork<BitSet, Boolean> bn) {
        Double[][] atm = simulateBN(bn).getMatrixCopy();
        Number[][] sorted = MatrixUtility.reorderByDiagonalValues(atm);
        double[][] doubleSorted = MatrixUtility.fromNumberToDoubleMatrix(sorted);
        //return f1_robustness(doubleSorted) + f2_equallyDistributed(doubleSorted) + f3_triangleDifference(doubleSorted);
        return new Tuple3Extended(  f1_robustness_min(doubleSorted),
                f2_equallyDistributed(doubleSorted),
                f3_triangleDifference(doubleSorted));
    }

    public static Vec<double[]> evalMultiObjective(Genotype<IntegerGene> gt) {
        BooleanNetwork<BitSet, Boolean> bn = fromGenotypeToBN(gt, MainJenetics.K);
        return evalMultiObjective(bn);
    }
    public static Vec<double[]> evalMultiObjective(BooleanNetwork<BitSet, Boolean> bn) {
        Double[][] atm = simulateBN(bn).getMatrixCopy();
        Number[][] sorted = MatrixUtility.reorderByDiagonalValues(atm);
        double[][] doubleSorted = MatrixUtility.fromNumberToDoubleMatrix(sorted);
        //return f1_robustness(doubleSorted) + f2_equallyDistributed(doubleSorted) + f3_triangleDifference(doubleSorted);
        return Vec.of(f1_robustness_min(doubleSorted),
                f2_equallyDistributed(doubleSorted),
                f3_triangleDifference(doubleSorted));
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
        Attractors<BinaryState> attractors = new AttractorsFinderService<BinaryState>().apply(generator, dynamics);
        return new CompletePerturbations().apply(attractors, dynamics, Constant.PERTURBATIONS_CUTOFF);
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

    @SuppressWarnings("unchecked")
    public static void main (String [] args) {

        String path = "/Users/michelebraccini/Documents/workspaces/R_workspace/self_loop_genetici/run_3/";

        for (int i = 1; i < 2; i++) {
            String genotype = path +   Files.FILE_SEPARATOR + "GeneticAlg/BestGenotype.ser";
            System.out.println(genotype);
            BooleanNetwork<BitSet, Boolean> bn = fromGenotypeToBN((Genotype<IntegerGene>) Files.deserializeObject(genotype), 2);
            System.out.println(bn);
            Atm<BinaryState> atm = simulateBN(bn);

            String path_res = path  +  Files.FILE_SEPARATOR + "GeneticAlg" + Files.FILE_SEPARATOR ;
            Files.createDirectories(path);

            Files.writeMatrixToCsv(atm.getMatrixCopy(), path_res + "originalATM");
            Files.writeMatrixToCsv(MatrixUtility.reorderByDiagonalValues(atm.getMatrixCopy()), path_res + "sortedATM");

            //new AtmGraphViz(atm, path_res + "atm").generateDotFile().generateImg("jpg");
            new BNGraphViz<BitSet, Boolean>(bn).saveOnDisk( path_res + "bn");
        }


    }


    /*public static void main (String [] args) {

        String path = "/Users/michelebraccini/Documents/workspaces/R_workspace/self_loop_genetici/run_2/";
        Double[][] sorted;
        for (int i = 1; i < 18; i++) {

            sorted = Files.readCsvMatrix(path + "_" + i + "/GeneticAlg/sortedATM.csv" ,';', false);
            Set<List<Integer>> scc = new SCCTarjanAlgorithm(sorted).getSCCComponents();
            Tuple2<List<List<Integer>>, int[][]> t = new FindGraphFromSCC(sorted, scc).get();

            new SCCGraphViz(t).saveOnDisk(path + "conn_" + i);

        }
    }*/




    }

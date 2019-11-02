import attractor.AttractorsUtility;
import dynamic.SynchronousDynamicsImpl;
import experiments.methylation.MainFrozenCheckModelRSerra;
import generator.CompleteGenerator;
import interfaces.attractor.Attractors;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import io.vavr.Tuple2;
import network.BooleanNetworkFactory;
import network.OrTable;
import network.UtilitiesBooleanNetwork;
import noise.CompletePerturbations;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.apache.commons.text.similarity.HammingDistance;
import simulator.AttractorsFinderService;
import states.ImmutableBinaryState;
import utility.*;
import visualization.AtmGraphViz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class Main3 {

    public static void main(String[] args){
        HammingDistance hd = new HammingDistance();

        System.out.println(ImmutableBinaryState.valueOf("0000").setNodesValue(1));
        BinaryState b = ImmutableBinaryState.valueOf("11010");
        BinaryState b1 = ImmutableBinaryState.valueOf("00101");
        System.out.println("HAMMING DISTANCE: " + hd.apply(b.getStringRepresentation(),b1.getStringRepresentation()));
        System.out.println(b.setNodesValues(1,2,3) == b);


        System.out.println(UtilitiesBooleanNetwork.extendTable(new OrTable(2),2,()->false));

        List<? extends Number> l = List.of(1,2,3);
        System.out.println(l.get(0).intValue() + "" + Integer.valueOf(2).compareTo(3));

        List<? extends AA> ll = new NImpl().get();
        List<BB> a = new NNImpl().get();

        System.out.println(new NNImpl().get().get(0));
        System.out.println(new NImpl().get().get(0));



        BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> bn =
                //BooleanNetworkFactory.newNetworkFromFile("/Users/michelebraccini/IdeaProjects/cellsimulator_v2/src/test/resources/testing/diff_trees/bn");
                BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, 7,3, 0.5, RandomnessFactory.getPureRandomGenerator());
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());

        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
        Attractors<BinaryState> attractors = AttractorsFinderService.apply(generator, dynamics, true, true, AttractorsFinderService.TRUE_TERMINATION);
        System.out.println("CHECK");
        System.out.println(attractors);
        System.out.println(attractors.traceabilityInfo().statistics().get("initialStatesCutOff"));
        System.out.println(attractors.getNumberOfFixedPoints());
        System.out.println(attractors.numberOfAttractors());
        attractors.forEach(System.out::println);
        attractors.forEach(x -> System.out.println(AttractorsUtility.fixed(x)));
        System.out.println("FIXED: " + AttractorsUtility.fixedAttractors(attractors));
        System.out.println("BLINKING: " + AttractorsUtility.blinkingAttractors(attractors));

        System.exit(1);


        Atm<BinaryState> atm = new CompletePerturbations().apply(attractors, dynamics, Constant.PERTURBATIONS_CUTOFF);


        //Number[][] sorted = MatrixUtility.reorderByDiagonalValues(atm.getMatrix());
        //double[][] doubleSorted = MatrixUtility.fromNumberToDoubleMatrix(sorted);
        Tuple2<Number[][], String[]> aa =  MatrixUtility.reorderByDiagonalValuesATM(atm);
        double[][] doubleSorted = MatrixUtility.fromNumberToDoubleMatrix(aa._1());

        Arrays.stream(atm.header()).forEach(System.out::println);
        GenericUtility.printMatrix(atm.getMatrix());
        Arrays.stream(aa._2()).forEach(System.out::println);
        GenericUtility.printMatrix(doubleSorted);

        // Files.writeMatrixToCsv(doubleSorted, "ATM.csv");
        new AtmGraphViz(atm).saveOnDisk("ATM");


        /*List<String> l = List.of("bn_test");

        for (String a: l) {
            BNClassic<BitSet,Boolean> bn = BooleanNetworkFactory.newNetworkFromFile(a);
            System.out.println(bn);
            Atm<BinaryState> atm = GeneticAlgFitness.simulateBN(bn);
            GenericUtility.printMatrix(atm.getMatrix());
            new BNGraphViz<>(bn).saveOnDisk(a + "_repr");

        }*/


        /*A a = new AImpl();
        System.out.println(a.get());
        A b = new BImpl();
        System.out.println(((B)b).get().doubleValue());
        B c = new BImpl();
        System.out.println(c.get().doubleValue());*/

        C c = new CImpl<>(new Number() {
            @Override
            public int intValue() {
                return -100;
            }

            @Override
            public long longValue() {
                return 0;
            }

            @Override
            public float floatValue() {
                return 0;
            }

            @Override
            public double doubleValue() {
                return -100;
            }
        });
        System.out.println(c.get().getClass());

        C d = new DImpl<>(0.0);
        System.out.println(d.get().getClass());
        D e = new DImpl<>(0.0);
        System.out.println(d.get().getClass());

    }

    public static interface A {
        Object get();
    }
    public static interface B extends A {
        Integer get();
    }
    public static class AImpl implements A{
        public Object get(){
            return "AImpl";
        }
    }
    public static class BImpl extends AImpl implements B{
        public Integer get(){
            return 5;
        }
    }


    public static interface C<N extends Number> {
        N get();
    }
    public static interface D<N extends Double> extends C<N> {

    }
    public static class CImpl<N extends Number> implements C<N> {
        N n;
        public CImpl(N n){
            this.n = n;
        }
        public N get(){
            return n;
        }
    }
    public static class DImpl<N extends Double> extends CImpl<N> implements D<N> {

        public DImpl(N n) {
            super(n);
        }
    }





    public static interface AA {
        Object get();
    }
    public static interface BB extends AA {
        Integer get();
    }

    public static interface N {
        List<? extends AA> get();
    }
    public static interface NN extends N {
    }

    public static class AAImpl implements AA {
        public Object get(){
            return "AAImpl";
        }
    }
    public static class BBImpl extends AAImpl implements BB {
        public Integer get(){
            return 5;
        }
    }

    public static class NImpl implements N {
        @Override
        public List<? extends AA> get() {
            return List.of(new AAImpl());
        }
    }
    public static class NNImpl extends NImpl implements NN {
        @Override
        public List<BB> get() {
            return List.of(new BBImpl());
        }
    }
}

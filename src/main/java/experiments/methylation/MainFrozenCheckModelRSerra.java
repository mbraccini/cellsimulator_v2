package experiments.methylation;

import dynamic.KnockOutDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.BagOfStatesGenerator;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import io.vavr.Tuple2;
import network.BNKBiasImpl;
import network.BooleanNetworkFactory;
import network.TableSupplierNonCanalizingK2;
import org.apache.commons.math3.random.RandomGenerator;
import simulator.AttractorsFinderService;
import tes.StaticAnalysisTES;
import utility.Files;
import utility.RandomnessFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MainFrozenCheckModelRSerra {

    static BigInteger INITIAL_SAMPLES_STATES_NUMBER = BigInteger.valueOf(1000);
    static Integer BN_SAMPLES = 20;

    public static void main(String args[]) {

        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        int numNodes = 500;
        int k = 2;
        double bias = 0.5;
        char sep = ',';

        System.out.println("MainFrozenCheckModelRSerra, " +
                "BN_SAMPLES "+ BN_SAMPLES + " INITIAL_SAMPLES_STATES_NUMBER " + INITIAL_SAMPLES_STATES_NUMBER
                + " N:"+ numNodes);

        //M = phi * N
        Integer[] M = new Integer[]{5,50,200};
        //Header of csv STRING
        List<String> attsString = IntStream.range(0, M.length).mapToObj(x -> "Attrs").collect(Collectors.toList());
        List<String> pfString = IntStream.range(0, M.length).mapToObj(x -> "FixPoints").collect(Collectors.toList());
        io.vavr.collection.Stream<Tuple2<Integer,String>> temp1 = io.vavr.collection.Stream.of(M).zip(io.vavr.collection.Stream.ofAll(attsString));
        io.vavr.collection.Stream<Tuple2<Integer,String>> temp2 = io.vavr.collection.Stream.of(M).zip(io.vavr.collection.Stream.ofAll(pfString));
        List<Tuple2<Tuple2<Integer,String>,Tuple2<Integer,String>>> headerList = temp1.zip(temp2).toJavaList();
        String header = headerList.stream().map(x -> x._1()._1() + "" + x._1()._2() + sep + x._2()._1() + "" + x._2()._2()).collect(Collectors.joining("" + sep));
        //

        List<Tuple2<String, Supplier<BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>>>>> bnSuppliers =
                   List.of(//new Tuple2<>("NON_CANALZING", () -> new BNKBiasImpl(numNodes, r, Boolean.FALSE, new TableSupplierNonCanalizingK2(numNodes, r))));
                           //new Tuple2<>("ONLY_CANALZING", () -> new BNKBiasImpl(numNodes, r, Boolean.FALSE, new TableSupplierCanalizingK2(numNodes, r))),
                           new Tuple2<>("ALL", () -> BooleanNetworkFactory.newRBN(BNKBias.BiasType.EXACT, BooleanNetworkFactory.SelfLoop.WITHOUT, numNodes,k,  bias, r)));

        for (Tuple2<String, Supplier<BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>>>> supp : bnSuppliers) {
            String rootPath = supp._1() + Files.FILE_SEPARATOR;
            Files.createDirectories(rootPath);
            Supplier<BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>>> bnSupplier = supp._2();

            BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
            try (BufferedWriter csv = new BufferedWriter(new FileWriter(supp._1() + "_stats.csv", true),10)) {
                //csv.write("M1atts, M1fp, M2atts, M2fp, M3atts, M3fp, M5atts, M5fp, M10atts, M10fp, M15atts, M15fp, M20atts, M20fp");
                csv.write(header);
                csv.newLine();
                for (int i = 0; i < BN_SAMPLES; i++) {
                    bn = bnSupplier.get();
                    String path_bn = rootPath + "bn_" + i + Files.FILE_SEPARATOR;
                    Files.createDirectories(path_bn);
                    Files.writeBooleanNetworkToFile(bn, path_bn + "bn_" + i);
                    for (int j = 0; j < M.length; j++) {
                        int m = M[j];
                        String config_path = path_bn + "M_" + m + Files.FILE_SEPARATOR;
                        Files.createDirectories(config_path);
                        Tuple2<Integer, Integer> NoAttrsNoFixedPoints = forEachConfiguration(bn, m, numNodes, k, bias, r, config_path);
                        csv.append(NoAttrsNoFixedPoints._1().toString());
                        csv.append(sep);
                        csv.append(NoAttrsNoFixedPoints._2().toString());
                        if (j == M.length - 1) {
                            csv.newLine();
                        } else {
                            csv.append(sep);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    static private Tuple2<Integer, Integer> forEachConfiguration(BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                               int numOfFrozenNodes,
                                               int numNodes,
                                               int k,
                                               double bias,
                                               RandomGenerator r,
                                               String path
    ) {
        //indices of nodes to KO
        List<Integer> indicesToKnockOut = new ArrayList<>();
        for (int i = 0; i < numOfFrozenNodes; i++) {
            indicesToKnockOut.add(i);
        }
        //KNOCK OUT DYNAMICS
        Dynamics<BinaryState> dynamicsKO = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToKnockOut));
        //GENERATOR
        Generator<BinaryState> genUnif =
                new UniformlyDistributedGenerator(INITIAL_SAMPLES_STATES_NUMBER, numNodes,r);

        Generator<BinaryState> genKO = new BagOfStatesGenerator<>(Stream.generate(genUnif::nextSample)
                                                                        .limit(INITIAL_SAMPLES_STATES_NUMBER.intValue())
                                                                        .map(sample -> sample.setNodesValues(Boolean.FALSE, indicesToKnockOut.toArray(new Integer[0])))
                                                                        .collect(Collectors.toList()));

        //ATTRACTORS
        Attractors<BinaryState> atts = AttractorsFinderService.apply(genKO,
                                                                    dynamicsKO,
                                                                false,
                                                                        false,
                                                                         AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(numNodes));
        //Attractors<BinaryState> atts = StaticAnalysisTES.attractors(genKO, dynamicsKO);
        //ON DISK
        Files.writeAttractorsToReadableFile(atts, path + "atts");
        //
        return new Tuple2<>(atts.numberOfAttractors(), atts.getNumberOfFixedPoints());
    }


    public static Set<Integer> fixedAttractors(Attractors<BinaryState> attrs){
        List<ImmutableAttractor<BinaryState>> noFixedPoints = attrs.getAttractors().stream().filter(x -> x .getLength() > 1).collect(Collectors.toList());
        Set<Integer> intersect = null;
        for (int attIdx = 0; attIdx < noFixedPoints.size(); attIdx++) {
            if (intersect == null) {
                intersect = new HashSet<>(fixed(noFixedPoints.get(attIdx)));
            } else {
                intersect.retainAll(fixed(noFixedPoints.get(attIdx)));
            }
        }
        return intersect;
    }

    public static Set<Integer> fixed(ImmutableAttractor<BinaryState> a) {
        Set<Integer> indices = new HashSet<>();
        Integer numNodes = a.getFirstState().getLength();
        BinaryState prev, succ;
        boolean first = true;
        for (int state = 0; state < a.getLength() - 1; state++) {
            prev = a.getStates().get(state);
            succ = a.getStates().get(state + 1);

            for (int i = 0; i < numNodes; i++) {
                if (prev.getNodeValue(i) == succ.getNodeValue(i)){
                    if (first) {
                        indices.add(i);
                    }
                } else if (indices.contains(i)) {
                    indices.remove(i);
                }
            }
            first = false;
        }
        return indices;
    }


}

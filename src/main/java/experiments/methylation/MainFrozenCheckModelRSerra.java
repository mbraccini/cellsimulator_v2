package experiments.methylation;

import attractor.AttractorsUtility;
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
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import network.BNKBiasImpl;
import network.BooleanNetworkFactory;
import network.TableSupplierCanalizingK2;
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
    static Integer BN_SAMPLES = 1000;

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
        Integer[] M = new Integer[]{0,25,125,250};
        //Header of csv STRING
        List<String> attsString = IntStream.range(0, M.length).mapToObj(x -> "Attrs").collect(Collectors.toList());
        List<String> pfString = IntStream.range(0, M.length).mapToObj(x -> "FixPoints").collect(Collectors.toList());
        List<String> fixedFraction = IntStream.range(0, M.length).mapToObj(x -> "FixNodes").collect(Collectors.toList());
        List<String> blinkingFraction = IntStream.range(0, M.length).mapToObj(x -> "blinkNodes").collect(Collectors.toList());
        List<String> cutOff = IntStream.range(0, M.length).mapToObj(x -> "cutOff").collect(Collectors.toList());

        io.vavr.collection.Stream<Tuple2<Integer,String>> temp1 = io.vavr.collection.Stream.of(M).zip(io.vavr.collection.Stream.ofAll(attsString));
        io.vavr.collection.Stream<Tuple2<Integer,String>> temp2 = io.vavr.collection.Stream.of(M).zip(io.vavr.collection.Stream.ofAll(pfString));
        io.vavr.collection.Stream<Tuple2<Integer,String>> temp3 = io.vavr.collection.Stream.of(M).zip(io.vavr.collection.Stream.ofAll(fixedFraction));
        io.vavr.collection.Stream<Tuple2<Integer,String>> temp4 = io.vavr.collection.Stream.of(M).zip(io.vavr.collection.Stream.ofAll(blinkingFraction));
        io.vavr.collection.Stream<Tuple2<Integer,String>> temp5 = io.vavr.collection.Stream.of(M).zip(io.vavr.collection.Stream.ofAll(cutOff));

        List<Tuple2<Tuple2<Integer,String>,Tuple2<Integer,String>>> headerList = temp1.zip(temp2).toJavaList();
        List<Tuple2<Tuple2<Integer,String>,Tuple2<Integer,String>>> headerList2 = temp3.zip(temp4).toJavaList();

        List<String> header1 = headerList.stream().map(x -> x._1()._1() + "" + x._1()._2() + sep + x._2()._1() + "" + x._2()._2()).collect(Collectors.toList());
        List<String> header2 = headerList2.stream().map(x -> x._1()._1() + "" + x._1()._2() + sep + x._2()._1() + "" + x._2()._2()).collect(Collectors.toList());
        List<String> header3 = temp5.toStream().map(x -> x._1()+ "" + x._2()).collect(Collectors.toList());
        int elementsPerList = 1;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < header1.size();) {
            for (int j = 0; j < elementsPerList && (i + j) <header1.size() ; j++) {
                sb.append(header1.get(i + j));
                sb.append(sep);
                sb.append(header2.get(i + j));
                sb.append(sep);
                sb.append(header3.get(i + j));
            }
            if (i != header1.size() - 1){
                sb.append(sep);
            }
            i += elementsPerList;
        }
        //
        String header = sb.toString();
        System.out.println(header);
        List<Tuple2<String, Supplier<BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>>>>> bnSuppliers =
                   List.of(//new Tuple2<>("NON_CANALIZING", () -> new BNKBiasImpl(numNodes, r, Boolean.FALSE, new TableSupplierNonCanalizingK2(numNodes, r))),
                           //new Tuple2<>("ONLY_CANALIZING", () -> new BNKBiasImpl(numNodes, r, Boolean.FALSE, new TableSupplierCanalizingK2(numNodes, r))),
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
                        Tuple5<Integer, Integer, Double, Double, Integer> NoAttrsNoFixedPoints = forEachConfiguration(bn, m, numNodes, k, bias, r, config_path);
                        csv.append(NoAttrsNoFixedPoints._1().toString());
                        csv.append(sep);
                        csv.append(NoAttrsNoFixedPoints._2().toString());
                        csv.append(sep);
                        csv.append((NoAttrsNoFixedPoints._3() == null ? "NA": NoAttrsNoFixedPoints._3().toString()));
                        csv.append(sep);
                        csv.append((NoAttrsNoFixedPoints._4() == null ? "NA": NoAttrsNoFixedPoints._4().toString()));
                        csv.append(sep);
                        csv.append(NoAttrsNoFixedPoints._5().toString());

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

    static private Tuple5<Integer, Integer, Double, Double, Integer> forEachConfiguration(BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
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
        //Files.writeAttractorsToReadableFile(atts, path + "atts");
        //
        Set<Integer> blink = AttractorsUtility.blinkingAttractors(atts);
        Set<Integer> fixed = AttractorsUtility.fixedAttractors(atts);
        Double blinkFraction, fixedFraction;
        if (blink == null) {
            blinkFraction = null;
        } else {
            blinkFraction = ((double)blink.size() / numNodes);
        }
        if (fixed == null) {
            fixedFraction = null;
        } else {
            fixedFraction = ((double)fixed.size() / numNodes);
        }
        return new Tuple5<>(atts.numberOfAttractors(), atts.getNumberOfFixedPoints(),fixedFraction, blinkFraction, atts.traceabilityInfo().statistics().get("initialStatesCutOff").intValue());
    }


}

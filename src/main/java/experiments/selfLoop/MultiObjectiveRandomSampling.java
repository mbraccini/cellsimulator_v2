package experiments.selfLoop;

import generator.RandomnessFactory;
import interfaces.network.BooleanNetwork;
import interfaces.network.Table;
import network.BiasedTable;
import network.BooleanNetworkFactory;
import network.ConfigurableTable;
import network.RBNSelfLoop;
import org.jooq.lambda.tuple.Tuple3;
import utility.Files;
import utility.MatrixUtility;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MultiObjectiveRandomSampling {

    public static Tuple3<Double, Double, Double> evaluate(BooleanNetwork<BitSet, Boolean> bn) {
        Double[][] atm = GeneticAlgFitness.simulateBN(bn).getMatrixCopy();
        Number[][] sorted = MatrixUtility.reorderByDiagonalValues(atm);
        double[][] doubleSorted = GeneticAlgFitness.fromNumberToDoubleMatrix(sorted);
        return new Tuple3<>(GeneticAlgFitness.f1_robustness(doubleSorted),
                GeneticAlgFitness.f2_equallyDistributed(doubleSorted),
                GeneticAlgFitness.f3_triangleDifference(doubleSorted));
    }

    public static void checkIfDominated(Set<Tuple3<Integer, BooleanNetwork<BitSet, Boolean>, Tuple3<Double, Double, Double>>> paretoFront,
                                        Tuple3<Integer, BooleanNetwork<BitSet, Boolean>, Tuple3<Double, Double, Double>> tuple) {
        if (paretoFront.size() == 0) {
            //aggiungi
            paretoFront.add(tuple);
        } else {
            /*System.out.println("pareto: ");
            paretoFront.forEach(x -> System.out.println(x.v1() + ", " + x.v3()));
            System.out.println("--------");*/

            //controlliamo se lui viene dominato perché se così non fosse lo dovremmo aggiungere al paretoSet
            if (!paretoFront.stream().anyMatch(x -> {
                return x.v3().v1() > tuple.v3().v1() && x.v3().v2() >= tuple.v3().v2() && x.v3().v3() >= tuple.v3().v3()
                        || x.v3().v1() >= tuple.v3().v1() && x.v3().v2() > tuple.v3().v2() && x.v3().v3() >= tuple.v3().v3()
                        || x.v3().v1() >= tuple.v3().v1() && x.v3().v2() >= tuple.v3().v2() && x.v3().v3() > tuple.v3().v3();
            })) {

                paretoFront.removeIf(x -> {
                    return tuple.v3().v1() > x.v3().v1() && tuple.v3().v2() >= x.v3().v2() && tuple.v3().v3() >= x.v3().v3()
                            || tuple.v3().v1() >= x.v3().v1() && tuple.v3().v2() > x.v3().v2() && tuple.v3().v3() >= x.v3().v3()
                            || tuple.v3().v1() >= x.v3().v1() && tuple.v3().v2() >= x.v3().v2() && tuple.v3().v3() > x.v3().v3();
                });

                paretoFront.add(tuple);
                /*System.out.println("tuple inserted: " + tuple.v3());
                paretoFront.forEach(x -> System.out.println(x.v3()));*/
            }
        }
    }


    public static void main(String[] args) {
        int k = 2;
        int nodesNumber = 20;
        double bias = 0.5;
        int samples = 1000;
        long seed = 153; //used: 100 con selfloop e 153 senza

        Random r = RandomnessFactory.newPseudoRandomGenerator(seed);

        /*Supplier<Table<BitSet, Boolean>> supplier = () -> new ConfigurableTable(k, r.doubles((long) Math.pow(2, k), 0.0, 1.0)
                .mapToObj(x -> ((x > 0.5) ? Boolean.TRUE : Boolean.FALSE))
                .collect(Collectors.toList()));*/

        Supplier<Table<BitSet, Boolean>> supplier = () -> new BiasedTable(k, bias, r);
        Set<Tuple3<Integer, BooleanNetwork<BitSet, Boolean>, Tuple3<Double, Double, Double>>> paretoFront = new HashSet<>();
        BooleanNetwork<BitSet, Boolean> current_bn;
        Tuple3<Integer, BooleanNetwork<BitSet, Boolean>, Tuple3<Double, Double, Double>> current_tuple;
        int counter = 0;

        System.out.println("senza self loop");

        while (counter < samples) {
            //current_bn = RBNSelfLoop.newInstance(nodesNumber, k, r, supplier);
            current_bn = BooleanNetworkFactory.newRBN(BooleanNetworkFactory.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
            current_tuple = new Tuple3<>(counter, current_bn, evaluate(current_bn));
            checkIfDominated(paretoFront, current_tuple);
            System.out.println("" + counter);

            counter++;
        }


        String directory = "pareto/";
        Files.createDirectories(directory);

        paretoFront.forEach(x -> Files.writeBooleanNetworkToFile(x.v2(), directory + "bn_" + x.v1()));
        //List.of("bn_index", "f1", "f2", "f3");
        Files.writeListsToCsv(paretoFront.stream().map(x -> List.of(x.v1(), x.v3().v1(), x.v3().v2(), x.v3().v3())).collect(Collectors.toList()), directory + "pareto.csv");

    }

}

//package network;
//
//import exceptions.InputConnectionsException;
//import interfaces.network.BNClassic;
//import interfaces.network.Table;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.function.Supplier;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//public class RBNSelfLoop<K, V> extends AbstractBooleanNetwork<K, V> {
//
//    protected finalrandomGeneratorrandom;
//    protected final int k;
//    protected Supplier<Table<K, V>> rndTableSupplier;
//
//    private RBNSelfLoop(int nodesNumber, int k,randomGeneratorrandom, Supplier<Table<K, V>> rndTableSupplier) {
//        super(nodesNumber);
//        this.k = k;
//        this.random = random;
//        this.rndTableSupplier = rndTableSupplier;
//        checkNodesNumberInvariant();
//        configure();
//        properties.setProperty("CellPopulation", "random");
//    }
//
//    private final void checkNodesNumberInvariant() {
//        if (this.k > this.nodesNumber) {
//            throw new InputConnectionsException("K must be less than #nodes!");
//        }
//    }
//
//    public static <K, V> BNClassic<K, V> newInstance(int nodesNumber, int k,randomGeneratorrandom, Supplier<Table<K, V>> rndTableSupplier) {
//        return new RBNSelfLoop<K, V>(nodesNumber, k, random, rndTableSupplier);
//    }
//
//    private void configure() {
//        initNodes();
//        initTopology();
//    }
//
//    protected void initNodes() {
//        nodes = IntStream.range(0, nodesNumber)
//                .mapToObj(x -> {
//                    return new AbstractNode<>("gene_" + x, x, rndTableSupplier.get());
//                })
//                .collect(Collectors.toList());
//
//    }
//
//    protected final void initTopology() {
//        List<Integer> list;
//        for (int nodeId = 0; nodeId < this.nodesNumber; nodeId++) {
//            list = new ArrayList<>(this.k);
//            int nodesAdded = 0;
//            do {
//                int candidate = random.nextInt(this.nodesNumber);
//
//                if (!list.stream().anyMatch(x -> x == candidate)) {
//                    list.add(candidate);
//                    nodesAdded++;
//                }
//            } while (nodesAdded < this.k);
//
//            /* ora con le informazioni calcolate posso riempire la map */
//            this.nodesMap.put(this.nodes.get(nodeId),
//                    list.stream().map(x -> nodes.get(x)).collect(Collectors.toList()));
//        }
//
//    }
//
//}
//
//

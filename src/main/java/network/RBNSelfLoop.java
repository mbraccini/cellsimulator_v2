package network;

import exceptions.InputConnectionsException;
import interfaces.network.BooleanNetwork;
import interfaces.network.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RBNSelfLoop<K, V> extends AbstractBooleanNetwork<K, V> {

    protected final Random random;
    protected final int k;
    protected Supplier<Table<K, V>> tableSupplier;

    private RBNSelfLoop(int nodesNumber, int k, Random random, Supplier<Table<K, V>> tableSupplier) {
        super(nodesNumber);
        this.k = k;
        this.random = random;
        this.tableSupplier = tableSupplier;
        checkNodesNumberInvariant();
        configure();
        properties.setProperty("topology", "random");
    }

    private final void checkNodesNumberInvariant() {
        if (this.k > this.nodesNumber) {
            throw new InputConnectionsException("K must be less than #nodes!");
        }
    }

    public static <K, V> BooleanNetwork<K, V> newInstance(int nodesNumber, int k, Random random, Supplier<Table<K, V>> tableSupplier) {
        return new RBNSelfLoop<K, V>(nodesNumber, k, random, tableSupplier);
    }

    private void configure() {
        initNodes();
        initTopology();
    }

    protected void initNodes() {
        nodesList = IntStream.range(0, nodesNumber)
                .mapToObj(x -> {
                    return new NodeImpl<>("gene_" + x, x, tableSupplier.get());
                })
                .collect(Collectors.toList());

    }

    protected final void initTopology() {
        List<Integer> list;
        for (int nodeId = 0; nodeId < this.nodesNumber; nodeId++) {
            list = new ArrayList<>(this.k);
            int nodesAdded = 0;
            do {
                int candidate = random.nextInt(this.nodesNumber);

                if (!list.stream().anyMatch(x -> x == candidate)) {
                    list.add(candidate);
                    nodesAdded++;
                }
            } while (nodesAdded < this.k);

            /* ora con le informazioni calcolate posso riempire la map */
            this.nodesMap.put(this.nodesList.get(nodeId),
                    list.stream().map(x -> nodesList.get(x)).collect(Collectors.toList()));
        }

    }

}



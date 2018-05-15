package network;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import exceptions.InputConnectionsException;
import interfaces.core.Factory;
import interfaces.network.BNClassic;
import interfaces.network.NodeDeterministic;
import interfaces.network.Table;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphBuilder;

public class RBNFactory<K, V> implements Factory<BNClassicImpl<K,V,NodeDeterministic<K,V>>> {

    protected final Random random;
    protected final int k;
    private final int nodesNumber;
    private Supplier<Table<K, V>> tableSupplier;
    private List<NodeDeterministic<K,V>> nodesList = new ArrayList<>();

    private GraphBuilder<NodeDeterministic<K,V>, DefaultEdge, DefaultDirectedGraph<NodeDeterministic<K,V>,DefaultEdge>> builder;

    public RBNFactory(int nodesNumber, int k, Random random, Supplier<Table<K, V>> tableSupplier) {
        this.nodesNumber = nodesNumber;
        this.k = k;
        this.random = random;
        this.tableSupplier = tableSupplier;
        checkNodesNumberInvariant();

        this.builder = new GraphBuilder<>(new DefaultDirectedGraph<>(DefaultEdge.class));
        configure();
    }

    private final void checkNodesNumberInvariant() {
        if (this.k > (this.nodesNumber - 1)) {
            throw new InputConnectionsException("K must be less than (#nodes - 1)!");
        }
    }

    private void configure() {
        initNodes();
        initTopology();
    }

    protected void initNodes() {
        nodesList = IntStream.range(0, nodesNumber)
                .mapToObj(x -> {
                    return new NodeDeterministicImpl<>("gene_" + x, x, tableSupplier.get());
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

                if (candidate != nodeId && !list.stream().anyMatch(x -> x == candidate)) {
                    list.add(candidate);
                    nodesAdded++;
                }
            } while (nodesAdded < this.k);

			/* ora con le informazioni calcolate posso riempire la map */
            addIncomingNodes(nodeId, list);
        }

    }


    private void addIncomingNodes(Integer nodeId, List<Integer> incoming){
        for (Integer i: incoming) {
            builder.addEdge(nodesList.get(i), nodesList.get(nodeId));
        }
    }

    @Override
    public BNClassicImpl<K, V, NodeDeterministic<K,V>> newInstance() {
        return new BNClassicImpl<>(builder.build());
    }
}

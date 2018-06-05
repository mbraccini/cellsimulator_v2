package network;

import interfaces.network.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class miRNABNClassicImpl<K, V> extends BNClassicImpl<K,V,NodeDeterministic<K,V>>
        implements miRNABNClassic<K,
        V,
        NodeDeterministic<K,V>,
        BNClassic<K,V,NodeDeterministic<K,V>>,
        NodeDeterministic<K,V>> {

    private final BNClassic<K,V,NodeDeterministic<K,V>> wrappedBN;
    private final int miRNA_Number;
    private final RandomGenerator random;
    private final int[] miRNA_FanOut;
    private final Supplier<Table<K, V>> miRNATableSupplier;
    private List<NodeDeterministic<K,V>> miRNAnodesList;
    private int[] miRNA_K;
    private BiFunction<Integer, Table<K, V>, Table<K, V>> supplierDownstreamNode;
    private List<NodeDeterministic<K,V>> miRNADownstreamNodesList;


    protected miRNABNClassicImpl(int nodesNumber, int miRNA_Number, int[] miRNA_FanOut, BNClassic<K,V,NodeDeterministic<K,V>> wrappedBN,RandomGenerator random, Supplier<Table<K, V>> miRNATableSupplier, BiFunction<Integer, Table<K, V>, Table<K, V>> supplierDownstreamNode) {
        super();
        this.wrappedBN = wrappedBN;
        this.miRNA_Number = miRNA_Number;
        this.miRNA_FanOut = miRNA_FanOut;
        this.random = random;
        this.miRNATableSupplier = miRNATableSupplier;
        this.supplierDownstreamNode = supplierDownstreamNode;
        this.miRNADownstreamNodesList = new ArrayList<>();
        this.miRNA_K = new int[miRNA_Number];

        configure();

    }


    public static <K, V> miRNABNClassic<K, V, NodeDeterministic<K,V>, BNClassic<K, V,NodeDeterministic<K,V>>, NodeDeterministic<K,V>>
                                                                        newInstance(int miRNA_Number,
                                                                                   int[] miRNA_FanOut,
                                                                                   BNClassic<K,V,NodeDeterministic<K,V>> wrappedBN,
                                                                                    RandomGenerator random,
                                                                                   Supplier<Table<K, V>> miRNATableSupplier,
                                                                                   BiFunction<Integer, Table<K, V>, Table<K, V>> supplierDownstreamNode) {
        /**
         * Remark:
         *  - nodesNumber = miRNANodesNumber + getNodesNumber()
         */
        return new miRNABNClassicImpl<>(miRNA_Number + wrappedBN.getNodesNumber(), miRNA_Number, miRNA_FanOut, wrappedBN, random, miRNATableSupplier, supplierDownstreamNode);
    }


    public void configure() {
        initNodes();
    }

    public void initNodes() {
        List<NodeDeterministic<K,V>> wrappedNodesList = wrappedBN.getNodes();
        /**
         * miRNA nodes
         */
        miRNAnodesList = IntStream.range(0, miRNA_Number)
                .mapToObj(x -> {
                    Table<K, V> table = miRNATableSupplier.get();
                    miRNA_K[x] = table.getVariablesNumber();
                    return new NodeDeterministicImpl<>("miRNA_" + (wrappedBN.getNodesNumber() + x), wrappedBN.getNodesNumber() + x, table);
                })
                .collect(Collectors.toList());


        /**
         * indices of the nodes influenced by miRNAs
         */
        List<List<Integer>> miRNADownstreamNodes = chooseRandomNodes(miRNA_Number, miRNA_FanOut, 0, wrappedBN.getNodesNumber());
        Map<Integer, Integer> countsIncomingNodesToAdd = miRNADownstreamNodes.stream().flatMap(x -> x.stream()).collect(Collectors.toList()).stream().collect(Collectors.groupingBy(e -> e, Collectors.summingInt(s -> 1)));


        for (int i = 0; i < wrappedBN.getNodesNumber(); i++) {
            if (Objects.isNull(countsIncomingNodesToAdd.get(i))) {
                // We copy the wrappedBN's node
                NodeDeterministic<K,V> wrappedNode = wrappedNodesList.get(i);
                nodes.add(wrappedNode);                                             /* wrapped nodes added to nodes */
                incomingNodesMap.put(wrappedNode.getId(), wrappedBN.getIncomingNodes(wrappedNode).stream().mapToInt(Node::getId).boxed().collect(Collectors.toList()));     /* wrapped nodes added to nodesMap */
            } else {
                // We create a MODIFIED COPY of the wrappedBN's node
                int miRNAIncomingNodesnumber = countsIncomingNodesToAdd.get(i);
                NodeDeterministic<K,V> wrappedNode = wrappedNodesList.get(i);
                Table<K, V> newTable = supplierDownstreamNode.apply(miRNAIncomingNodesnumber, wrappedNodesList.get(i).getFunction());
                NodeDeterministic<K,V> newNode = new NodeDeterministicImpl<>(wrappedNode.getName(), wrappedNode.getId(), newTable);
                nodes.add(newNode);                                                 /* MODIFIED wrapped nodes added to nodes */

                List<Integer> miRNAIncomingIndices = new ArrayList<>();
                for (int miRNAIdx = 0; miRNAIdx < miRNADownstreamNodes.size(); miRNAIdx++) {
                    if (miRNADownstreamNodes.get(miRNAIdx).contains(i)) {
                        miRNAIncomingIndices.add(miRNAIdx);
                        if (miRNAIncomingIndices.size() == miRNAIncomingNodesnumber) {
                            break;
                        }
                    }
                }
                List<NodeDeterministic<K,V>> incomingNodes = new ArrayList<>(wrappedBN.getIncomingNodes(wrappedNode));
                incomingNodes.addAll(miRNAIncomingIndices.stream().map(x -> miRNAnodesList.get(x)).collect(Collectors.toList()));
                incomingNodesMap.put(newNode.getId(), incomingNodes.stream().mapToInt(Node::getId).boxed().collect(Collectors.toList()));            /* MODIFIED wrapped nodes added to nodesMap */
                this.miRNADownstreamNodesList.add(newNode);
            }
        }

        nodes.addAll(miRNAnodesList);                                               /* miRNA nodes added to nodes */


        /**
         * indices of miRNAs' incoming nodes
         */
        List<List<Integer>> miRNAIncomingNodes = chooseRandomNodes(miRNA_Number, miRNA_K, 0, wrappedBN.getNodesNumber());


        /**
         * we add the miRNA to the nodesMap
         */
        IntStream.range(0, miRNA_Number)
                .forEach(idx -> incomingNodesMap.put(miRNAnodesList.get(idx).getId(),
                        miRNAIncomingNodes.get(idx).stream().mapToInt(y -> getNodeById(y).getId()).boxed().collect(Collectors.toList())));


    }

    private List<List<Integer>> chooseRandomNodes(int groupsNumber, int[] elementsNumberInEachGroup, int fromIndex, int toIndex) {
        int range = toIndex - fromIndex;
        List<List<Integer>> set = new ArrayList<>();
        List<Integer> list;
        for (int group = 0; group < groupsNumber; group++) {
            list = new ArrayList<>(elementsNumberInEachGroup[group]);
            int nodesAdded = 0;
            do {
                int candidate = random.nextInt(range) + fromIndex;
                if (list.stream().noneMatch(x -> x == candidate)) {
                    list.add(candidate);
                    nodesAdded++;
                }
            } while (nodesAdded < elementsNumberInEachGroup[group]);
            set.add(list);
        }
        return set;
    }


    @Override
    public BNClassic<K, V, NodeDeterministic<K,V>> getWrappedBooleanNetwork() {
        return wrappedBN;
    }

    @Override
    public Integer miRNANumber() {
        return miRNA_Number;
    }

    @Override
    public List<NodeDeterministic<K,V>> miRNANodes() {
        return miRNAnodesList;
    }

    @Override
    public List<NodeDeterministic<K,V>> miRNADownstreamNodes() {
        return miRNADownstreamNodesList;
    }

}

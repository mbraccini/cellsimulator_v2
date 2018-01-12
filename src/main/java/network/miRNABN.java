package network;

import exceptions.RowNotFoundException;
import generator.RandomnessFactory;
import interfaces.network.*;
import states.States;
import utility.GenericUtility;
import utility.Randomness;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class miRNABN<K, V> extends AbstractBooleanNetwork<K, V> implements miRNABooleanNetwork<K, V> {

    private final BooleanNetwork<K, V> wrappedBN;
    private final int miRNA_Number;
    private final Random random;
    private final int[] miRNA_FanOut;
    private final Supplier<Table<K, V>> miRNATableSupplier;
    private List<Node<K, V>> miRNAnodesList;
    private int[] miRNA_K;
    private BiFunction<Integer, Table<K, V>, Table<K, V>> supplierDownstreamNode;
    private List<Node<K, V>> miRNADownstreamNodesList;


    protected miRNABN(int nodesNumber, int miRNA_Number, int[] miRNA_FanOut, BooleanNetwork<K, V> wrappedBN, Random random, Supplier<Table<K, V>> miRNATableSupplier, BiFunction<Integer, Table<K, V>, Table<K, V>> supplierDownstreamNode) {
        super(nodesNumber);
        this.wrappedBN = wrappedBN;
        this.miRNA_Number = miRNA_Number;
        this.miRNA_FanOut = miRNA_FanOut;
        this.random = random;
        this.miRNATableSupplier = miRNATableSupplier;
        this.supplierDownstreamNode = supplierDownstreamNode;
        this.miRNADownstreamNodesList = new ArrayList<>();
        this.miRNA_K = new int[miRNA_Number];

        configure();

        wrappedBN.getNetworkProperties().forEach((k, v) -> properties.put(k, v));
        properties.setProperty("type", "miRNABooleanNetwork");
    }


    public static <K, V> miRNABooleanNetwork<K, V> newInstance(int miRNA_Number,
                                                               int[] miRNA_FanOut,
                                                               BooleanNetwork<K, V> wrappedBN,
                                                               Random random,
                                                               Supplier<Table<K, V>> miRNATableSupplier,
                                                               BiFunction<Integer, Table<K, V>, Table<K, V>> supplierDownstreamNode) {
        /**
         * Remark:
         *  - nodesNumber = miRNANodesNumber + getNodesNumber()
         */
        return new miRNABN<K, V>(miRNA_Number + wrappedBN.getNodesNumber(), miRNA_Number, miRNA_FanOut, wrappedBN, random, miRNATableSupplier, supplierDownstreamNode);
    }


    public void configure() {
        initNodes();
    }

    public void initNodes() {
        List<Node<K, V>> wrappedNodesList = wrappedBN.getNodes();
        /**
         * miRNA nodes
         */
        miRNAnodesList = IntStream.range(0, miRNA_Number)
                .mapToObj(x -> {
                    Table<K, V> table = miRNATableSupplier.get();
                    miRNA_K[x] = table.getVariablesNumber();
                    return new NodeImpl<>("miRNA_" + (wrappedBN.getNodesNumber() + x), wrappedBN.getNodesNumber() + x, table);
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
                Node<K, V> wrappedNode = wrappedNodesList.get(i);
                nodesList.add(wrappedNode);                                             /* wrapped nodes added to nodesList */
                nodesMap.put(wrappedNode, wrappedBN.getIncomingNodes(wrappedNode));     /* wrapped nodes added to nodesMap */
            } else {
                // We create a MODIFIED COPY of the wrappedBN's node
                int miRNAIncomingNodesnumber = countsIncomingNodesToAdd.get(i);
                Node<K, V> wrappedNode = wrappedNodesList.get(i);
                Table<K, V> newTable = supplierDownstreamNode.apply(miRNAIncomingNodesnumber, wrappedNodesList.get(i).getFunction());
                Node<K, V> newNode = new NodeImpl<>(wrappedNode.getName(), wrappedNode.getId(), newTable);
                nodesList.add(newNode);                                                 /* MODIFIED wrapped nodes added to nodesList */

                List<Integer> miRNAIncomingIndices = new ArrayList<>();
                for (int miRNAIdx = 0; miRNAIdx < miRNADownstreamNodes.size(); miRNAIdx++) {
                    if (miRNADownstreamNodes.get(miRNAIdx).contains(i)) {
                        miRNAIncomingIndices.add(miRNAIdx);
                        if (miRNAIncomingIndices.size() == miRNAIncomingNodesnumber) {
                            break;
                        }
                    }
                }
                List<Node<K, V>> incomingNodes = new ArrayList<>(wrappedBN.getIncomingNodes(wrappedNode));
                incomingNodes.addAll(miRNAIncomingIndices.stream().map(x -> miRNAnodesList.get(x)).collect(Collectors.toList()));
                nodesMap.put(newNode, incomingNodes);                                   /* MODIFIED wrapped nodes added to nodesMap */
                this.miRNADownstreamNodesList.add(newNode);
            }
        }

        nodesList.addAll(miRNAnodesList);                                               /* miRNA nodes added to nodesList */


        /**
         * indices of miRNAs' incoming nodes
         */
        List<List<Integer>> miRNAIncomingNodes = chooseRandomNodes(miRNA_Number, miRNA_K, 0, wrappedBN.getNodesNumber());


        /**
         * we add the miRNA to the nodesMap
         */
        IntStream.range(0, miRNA_Number)
                .forEach(idx -> this.nodesMap.put(miRNAnodesList.get(idx),
                        miRNAIncomingNodes.get(idx).stream().map(y -> nodesList.get(y)).collect(Collectors.toList())));


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
    public BooleanNetwork<K, V> getWrappedBooleanNetwork() {
        return wrappedBN;
    }

    @Override
    public Integer miRNANumber() {
        return miRNA_Number;
    }

    @Override
    public List<Node<K, V>> miRNANodes() {
        return miRNAnodesList;
    }

    @Override
    public List<Node<K, V>> miRNADownstreamNodes() {
        return miRNADownstreamNodesList;
    }
}

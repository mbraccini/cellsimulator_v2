package network;

import generator.RandomnessFactory;
import interfaces.network.BooleanNetwork;
import interfaces.network.Node;
import interfaces.network.Table;
import interfaces.network.miRNABooleanNetwork;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class miRNABN<K, V> extends AbstractBooleanNetwork<K, V> implements miRNABooleanNetwork<K, V> {

    private final BooleanNetwork<K, V> wrappedBN;
    private final int miRNA_Number;
    private final Random random;
    private final int miRNA_FanOut;
    private final Supplier<Table<K, V>> miRNATableSupplier;
    private List<Node<K,V>> miRNAnodesList;
    private int[] miRNA_K;


    protected miRNABN(int nodesNumber, int miRNA_Number, int miRNA_FanOut, BooleanNetwork<K, V> wrappedBN, Random random, Supplier<Table<K,V>> miRNATableSupplier) {
        super(nodesNumber);
        this.wrappedBN = wrappedBN;
        this.miRNA_Number = miRNA_Number;
        this.miRNA_FanOut = miRNA_FanOut;
        this.random = random;
        this.miRNATableSupplier = miRNATableSupplier;
        this.miRNA_K = new int[miRNA_Number];

        configure();

        wrappedBN.getNetworkProperties().forEach((k, v) -> properties.put(k, v));
        properties.setProperty("type", "miRNABooleanNetwork");
    }


    public static <K, V> miRNABooleanNetwork<K, V> newInstance(int miRNA_Number, int miRNA_FanOut, BooleanNetwork<K, V> wrappedBN, Random random, Supplier<Table<K,V>> miRNATableSupplier) {
        /**
         * Remark:
         *  - nodesNumber = miRNANodesNumber + getNodesNumber()
         */
        return new miRNABN(miRNA_Number + wrappedBN.getNodesNumber(), miRNA_Number, miRNA_FanOut, wrappedBN, random, miRNATableSupplier);
    }


    public void configure() {

        initNodes();
        //initTopology();
    }

    public void initNodes() {
        /**
         * wrapped nodes
         */
        nodesList.addAll(wrappedBN.getNodes());


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
        nodesList.addAll(miRNAnodesList);
        System.out.println(Arrays.toString(miRNA_K));


        /**
         * indices of miRNAs' incoming nodes
         */
        List<List<Integer>> miRNAIncomingNodes = chooseRandomNodes(miRNA_Number, miRNA_K[0], 0, wrappedBN.getNodesNumber());

        /**
         * we add the miRNA to the nodesMap
         */
        IntStream.range(0, miRNA_Number)
                .forEach(idx -> this.nodesMap.put(miRNAnodesList.get(idx),
                                                    miRNAIncomingNodes.get(idx).stream().map(y -> nodesList.get(y)).collect(Collectors.toList())));

        /**
         * we add the wrappedBN's nodes to the nodesMap
         */
        wrappedBN.getNodes().stream().forEach(x -> this.nodesMap.put(x, wrappedBN.getIncomingNodes(x)));


        /**
         * indices of the nodes influenced by miRNAs
         */
        List<List<Integer>> miRNADownstreamNodes = chooseRandomNodes(miRNA_Number, miRNA_FanOut, 0, wrappedBN.getNodesNumber());

        System.out.println("miRNADownstreamNodes: " + miRNADownstreamNodes);

        for (int miRNAIdx = 0; miRNAIdx < miRNADownstreamNodes.size(); miRNAIdx++) {
            List<Integer> influencedList = miRNADownstreamNodes.get(miRNAIdx); //influenced by miRNA with index i
            for (Integer influenced : influencedList) {
                nodesMap.get(getNodeById(influenced).get()).add(miRNAnodesList.get(miRNAIdx));
            }
        }

    }

    private List<List<Integer>> chooseRandomNodes(int groupsNumber, int elementsNumberInEachGroup, int fromIndex, int toIndex) {
        int range = toIndex - fromIndex;
        List<List<Integer>> set = new ArrayList<>();
        List<Integer> list;
        for (int group = 0; group < groupsNumber; group++) {
            list = new ArrayList<>(elementsNumberInEachGroup);
            int nodesAdded = 0;
            do {
                int candidate = random.nextInt(range) + fromIndex;
                if (list.stream().noneMatch(x -> x == candidate)) {
                    list.add(candidate);
                    nodesAdded++;
                }
            } while (nodesAdded < elementsNumberInEachGroup);
            set.add(list);
        }
        return set;
    }


    /*protected final void initTopology() {
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
           /* this.nodesMap.put(this.nodesList.get(nodeId),
                    list.stream().map(x -> nodesList.get(x)).collect(Collectors.toList()));
        }

    }*/


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
        return null;
    }

    public static void main (String [] arg) {
        Random r = RandomnessFactory.getPureRandomGenerator();
        Supplier<Table<BitSet, Boolean>> suppliermiRNA = () -> new BiasedTable(2, 0.5, r);
        Function<Boolean, Boolean> supplierBoolfunct = (x) -> !x;

       /* Stream<BiasedTable>  stream =  Stream.generate(() -> new BiasedTable(2, 0.5, r))
                                        .limit(10);
        */
        //miRNABN.initNodes(supplier);

        BooleanNetwork<BitSet, Boolean> bn =  miRNABN.newInstance(3, 2, BooleanNetworkFactory.newNetworkFromFile("bn"), r, suppliermiRNA);
        //System.out.println(bn.getNodes());
        //System.out.println(bn.getNodesNumber());

        //System.out.println(((miRNABN)bn).getWrappedBooleanNetwork());

        System.out.println(bn);


    }
}

package network;

import exceptions.InputConnectionsException;
import interfaces.network.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public final class BNKBiasImpl
            extends BNClassicImpl<BitSet,Boolean, NodeDeterministic<BitSet, Boolean>>
            implements BNKBias<BitSet, Boolean,NodeDeterministic<BitSet, Boolean>>{

    private Random random;
    private  int k;
    private  double bias;
    private  int nodesNumber;
    private BiasType biasType;
    private boolean hasSelfLoop;
    private  Supplier<Table<BitSet, Boolean>> tableSupplier;

    public BNKBiasImpl(int nodesNumber, int k, double bias, Random random, BiasType biasType, boolean hasSelfLoop) {
        super();
        this.nodesNumber = nodesNumber;
        this.k = k;
        this.random = random;
        this.biasType = biasType;
        this.bias = bias;
        this.hasSelfLoop = hasSelfLoop;
        this.tableSupplier = UtilitiesBooleanNetwork.rndTableSupplier(biasType, nodesNumber, k, bias, random);

        configure();
    }


    private final void configure() {
        checkNodesNumberInvariant(hasSelfLoop);
        initNodes();
        initTopology();
        checkVariablesNumberIncomingNodes();
        check();
    }

    /**
     * Invariant
     * @param hasSelfLoop
     */
    private final void checkNodesNumberInvariant(boolean hasSelfLoop) {
        if (hasSelfLoop){ //SELFLOOP
            if (this.k > this.nodesNumber) {
                throw new InputConnectionsException("K must be less than #nodes!");
            }
        } else {
            if (this.k > (this.nodesNumber - 1)) {
                throw new InputConnectionsException("K must be less than (#nodes - 1)!");
            }
        }

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

                if (hasSelfLoop){ //SELFLOOP
                    if (!list.stream().anyMatch(x -> x == candidate)) {
                        list.add(candidate);
                        nodesAdded++;
                    }
                } else {
                    if (candidate != nodeId && !list.stream().anyMatch(x -> x == candidate)) {
                        list.add(candidate);
                        nodesAdded++;
                    }
                }

            } while (nodesAdded < this.k);

            /* ora con le informazioni calcolate posso riempire la map */
            incomingNodesMap.put(nodeId, list);
        }
    }




    @Override
    public String toString() {
        return super.toString()
                    + "\n::K-BIAS-info::"
                    + "\nk: "+ k
                    + "\nbias " + bias
                    + "\n::End K-BIAS-info::";
    }

    @Override
    public double nominalBias() {
        return bias;
    }

    @Override
    public BiasType getBiasType() {
        return biasType;
    }









    /**
     * Modifier
     */
//    public static class BNKBiasModifier
//            extends AbstractModifier<NodeDeterministic<BitSet, Boolean>,
//                                    BNKBias<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>>,BNKBiasModifier>
//    {
//
//        protected BNKBiasModifier(BNKBias<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn) {
//            super(bn);
//
//        }
//
//        @Override
//        protected BNKBiasModifier self() {
//            return this;
//        }
//
//
//        @Override
//        public BNKBias<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> build() {
//            boolean selfLoop = false;
//            for (Map.Entry<Integer, List<Integer>> map : newTopology.entrySet()) {
//                if (map.getValue().contains(map.getKey())){
//                    selfLoop = true;
//                    break;
//                }
//            }
//            return new BNKBiasImpl(nodes, newTopology, bn.K(), bn.nominalBias(), bn.getBiasType(), selfLoop);
//        }
//
//
//
//
//    }
//
//

}
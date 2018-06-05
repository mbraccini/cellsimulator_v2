import dynamic.SynchronousDynamicsImpl;
import org.apache.commons.math3.random.RandomGenerator;
import utility.RandomnessFactory;
import interfaces.dynamic.Dynamics;
import interfaces.network.*;
import interfaces.network.BNKBias.BiasType;
import interfaces.state.BinaryState;
import network.BNClassicBuilder;
import network.BooleanNetworkFactory;
import network.NodeDeterministicImpl;
import network.UtilitiesBooleanNetwork;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import states.ImmutableBinaryState;

import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class TestBooleanNetworks {

    static RandomGenerator pureRandomGenerator;
    static int iterations;

    @BeforeClass
    public static void initializationRunOnce() {
        pureRandomGenerator = RandomnessFactory.getPureRandomGenerator();
    }

    @Before
    public void before() {
        iterations = 10;
    }

    @Test
    public void TestRBN() {
        int nodesNumber = 30;
        int k = 2;
        double bias = 0.5;
        while (iterations > 0) {
            BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn = BooleanNetworkFactory.newRBN(BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, pureRandomGenerator);
            System.out.println("AVG bias: " + bn.computeActualAverageBias());
            System.out.println("AVG k: " + bn.computeActualAverageIncomingNodes());
            System.out.println("AVG selfloop per nodo: " + bn.computeAverageNumberSelfLoopsPerNode());

            /** Tests **/
            assertTrue("k medio diverso da " + k, bn.computeActualAverageIncomingNodes() == k);
            assertTrue("numero selfloop medio per nodo diverso da 0", bn.computeAverageNumberSelfLoopsPerNode() == 0);
            Assert.assertFalse("Almeno un nodo presenta un self-loop!", bn.getNodes().stream().anyMatch(x -> bn.getIncomingNodes(x).contains(x)));

            iterations--;
        }
    }

    @Test
    public void TestRBNWithExactBias() {
        int nodesNumber = 30;
        int k = 3;
        double bias = 0.5;
        while (iterations > 0) {
            BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn = BooleanNetworkFactory.newRBN(BiasType.EXACT, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, pureRandomGenerator);
            System.out.println("AVG bias: " + bn.computeActualAverageBias());
            System.out.println("AVG k: " + bn.computeActualAverageIncomingNodes());
            System.out.println("AVG selfloop per nodo: " + bn.computeAverageNumberSelfLoopsPerNode());

            /** Tests **/
            assertTrue("bias medio diverso da " + bias, bn.computeActualAverageBias() == bias);
            assertTrue("numero selfloop medio per nodo diverso da 0", bn.computeAverageNumberSelfLoopsPerNode() == 0);
            Assert.assertFalse("Almeno un nodo presenta un self-loop!", bn.getNodes().stream().anyMatch(x -> bn.getIncomingNodes(x).contains(x)));

            iterations--;
        }
    }
        @Test
    public void TestRBNSelfLoop() {
        int nodesNumber = 30;
        int k = 2;
        double bias = 0.5;
        while (iterations > 0) {
            BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn = BooleanNetworkFactory.newRBN(BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITH, nodesNumber, k, bias, pureRandomGenerator);
            System.out.println(bn);
            System.out.println("AVG bias: " + bn.computeActualAverageBias());
            System.out.println("AVG k: " + bn.computeActualAverageIncomingNodes());
            System.out.println("AVG selfloop per nodo: " + bn.computeAverageNumberSelfLoopsPerNode());

            /** Tests **/
            assertTrue("k medio diverso da " + k, bn.computeActualAverageIncomingNodes() == k);
            iterations--;
        }
    }


    @Test
    public void TestBNmiRNA() {
        /* wrapped BN */
        BNClassic<BitSet, Boolean,NodeDeterministic<BitSet,Boolean>> bn;
        int nodesNumber = 30;
        int k = 3;
        double bias = 0.5;

        /* miRNA */
        miRNABNClassic<BitSet, Boolean,NodeDeterministic<BitSet,Boolean>,BNClassic<BitSet, Boolean,NodeDeterministic<BitSet,Boolean>>,NodeDeterministic<BitSet,Boolean>> miRNAbn;
        int miRNA_nodes = 5;
        int miRNA_K = 2;
        double miRNA_bias = 0.5;
        int miRNA_FanOut = 3;

        while (iterations > 0) {
            if (iterations % 2 == 0) {
                bn = BooleanNetworkFactory.newRBN(BiasType.EXACT, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, pureRandomGenerator);
            } else {
                bn = BooleanNetworkFactory.newRBN(BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, pureRandomGenerator);
            }

            miRNAbn = BooleanNetworkFactory.miRNANetworkInstance(bn, miRNA_nodes, miRNA_K, miRNA_bias, miRNA_FanOut, pureRandomGenerator);

            // controllare che la rete wrapped sia la stessa di quella di partenza

            assertTrue("Wrapped BN different from original BN", bn.equals(miRNAbn.getWrappedBooleanNetwork()));

            // controllare il numero di nodi totale sia bn.getNodes + nodi miRNA

            assertTrue("Total nodes number is wrong", miRNAbn.getNodesNumber() == bn.getNodesNumber() + miRNA_nodes);

            // controllare che le funzioni dei nodi a valle dei miRNA abbiano false quando il miRNA è attivo
            // possibilmente controllandolo tramite agggiornamento della rete e non dalle tabelle che dovrebbe già essere vero!

            final int max = miRNAbn.miRNANodes().size();
            final int miRNAIndexInExam = miRNAbn.miRNANodes().get(pureRandomGenerator.nextInt(max)).getId(); // indice di un miRNA
            // creo stato random
            int[] indices = new int[10];
            for (int i = 0; i < 10; i++) {
                indices[i] = pureRandomGenerator.nextInt(miRNAbn.getNodesNumber());
            }
            BinaryState state = new ImmutableBinaryState(miRNAbn.getNodesNumber(), indices);

            if (!state.getNodeValue(miRNAIndexInExam)) {
                state = state.flipNodesValues(miRNAIndexInExam); //settiamo a 1 il miRNA
            }

            for (NodeDeterministic<BitSet, Boolean> n : miRNAbn.miRNADownstreamNodes()) {
                if (miRNAbn.getIncomingNodes(n).stream().map(Node::getId).anyMatch(x -> x == miRNAIndexInExam)) {

                    if (!state.getNodeValue(n.getId())) {
                        state = state.flipNodesValues(n.getId()); //settiamo a 1 tutti i nodi che il miRNA influenza
                    }
                }
            }

            Dynamics<BinaryState> dyn = new SynchronousDynamicsImpl(miRNAbn);
            BinaryState nextState = dyn.nextState(state); // aggiorno lo stato e ora devo verificare che tutti i nodi che il miRNA scelto influenza siano a 0!

            for (NodeDeterministic<BitSet, Boolean> n : miRNAbn.miRNADownstreamNodes()) {

                List<Integer> previousIndices = bn.getIncomingNodes(bn.getNodeById(n.getId())).stream().map(Node::getId).collect(Collectors.toList());
                int previousLength = previousIndices.size();
                List<Integer> newIndices = miRNAbn.getIncomingNodes(n).stream().map(Node::getId).collect(Collectors.toList());

                assertTrue("miRNA are not appended to the list of previous incoming nodes", previousIndices.equals(newIndices.subList(0, previousLength)));

                if (miRNAbn.getIncomingNodes(n).stream().map(Node::getId).anyMatch(x -> x == miRNAIndexInExam)) {
                    assertTrue("The node " + n + " must be false since the miRNA that affects it was previously active", nextState.getNodeValue(n.getId()) == false);
                }

            }

            iterations--;
        }
    }


    @Test
    public void TestBuilder() {
        int nodesNumber = 30;
        int k = 2;
        double bias = 0.5;

        while (iterations > 0) {
            BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> current_bn = BooleanNetworkFactory.newRBN(BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, pureRandomGenerator);
            //
            int selfLoopNodeId = pureRandomGenerator.nextInt(nodesNumber);
            NodeDeterministic<BitSet,Boolean> node = current_bn.getNodeById(selfLoopNodeId);

            current_bn = new BNClassicBuilder<>(current_bn)
                    .addIncomingNode(node.getId(),node.getId()) //selfloop
                    .replaceNode(node,
                            new NodeDeterministicImpl<>("r_" + node.getName(),
                                    node.getId(),
                                    UtilitiesBooleanNetwork.extendTable(node.getFunction(),1, () -> true)))
                    .build();
            //

            int[] indices = new int[5];
            for (int i = 0; i < 5; i++) {
                indices[i] = pureRandomGenerator.nextInt(nodesNumber);
            }
            BinaryState s = ImmutableBinaryState.valueOf(nodesNumber, indices)
                            .setNodesValue(selfLoopNodeId);

            Dynamics<BinaryState> d = new SynchronousDynamicsImpl(current_bn);
            BinaryState sNext = d.nextState(s);


            /** Tests **/
            assertTrue("Must be true the node with selfloop and OR function", sNext.getNodeValue(selfLoopNodeId));
            assertTrue("Must have a selfloop", current_bn.hasSelfLoop());
            assertTrue("Must have ONE selfloop", current_bn.numberOfNodeWithSelfloops() == 1);


            iterations--;
        }
    }


    @Test
    public void TestManualTopologyReconfiguration() {
        /*int nodesNumber = 10;
        int k = 2;
        double bias = 0.5;
        //while (iterations > 0) {
            BNClassic<BitSet, Boolean> bn = BooleanNetworkFactory.newRBN(BooleanNetworkFactory.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, pureRandomGenerator);
            System.out.println(bn);
            bn.reconfigureIncomingEdge(9, 9, 0);
        bn.reconfigureIncomingEdge(6, 2, 1);
        bn.reconfigureIncomingEdge(6, 6, 0);


        System.out.println(bn);
        System.out.println(bn.numberOfNodeWithSelfloops());

*/
        //}

    }
}

import dynamic.SynchronousDynamicsImpl;
import generator.RandomnessFactory;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.network.Node;
import interfaces.network.miRNABooleanNetwork;
import interfaces.state.BinaryState;
import network.BooleanNetworkFactory;
import network.RBN;
import network.RBNExactBias;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import states.ImmutableBinaryState;

import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

public class TestBooleanNetworks {

    static Random pureRandomGenerator;
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
        Random pseudoRandom;
        int nodesNumber = 30;
        int k = 2;
        double bias = 0.5;
        while (iterations > 0) {
            pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(pureRandomGenerator.nextLong());
            BooleanNetwork<BitSet, Boolean> bn = new RBN(nodesNumber, k, bias, pseudoRandom);
            System.out.println("AVG bias: " + BooleanNetwork.computeActualAverageBias(bn));
            System.out.println("AVG k: " + BooleanNetwork.computeActualAverageIncomingNodes(bn));
            System.out.println("AVG selfloop per nodo: " + BooleanNetwork.computeAverageNumberSelfLoopsPerNode(bn));

            /** Tests **/
            assertTrue("k medio diverso da " + k, BooleanNetwork.computeActualAverageIncomingNodes(bn) == k);
            assertTrue("numero selfloop medio per nodo diverso da 0", BooleanNetwork.computeAverageNumberSelfLoopsPerNode(bn) == 0);
            Assert.assertFalse("Almeno un nodo presenta un self-loop!", bn.getNodes().stream().anyMatch(x -> bn.getIncomingNodes(x).contains(x)));

            iterations--;
        }
    }

    @Test
    public void TestRBNWithExactBias() {
        Random pseudoRandom;
        int nodesNumber = 30;
        int k = 3;
        double bias = 0.5;
        while (iterations > 0) {
            pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(pureRandomGenerator.nextLong());
            BooleanNetwork<BitSet, Boolean> bn = new RBNExactBias(nodesNumber, k, bias, pseudoRandom);
            System.out.println("AVG bias: " + BooleanNetwork.computeActualAverageBias(bn));
            System.out.println("AVG k: " + BooleanNetwork.computeActualAverageIncomingNodes(bn));
            System.out.println("AVG selfloop per nodo: " + BooleanNetwork.computeAverageNumberSelfLoopsPerNode(bn));

            /** Tests **/
            assertTrue("bias medio diverso da " + bias, BooleanNetwork.computeActualAverageBias(bn) == bias);
            assertTrue("numero selfloop medio per nodo diverso da 0", BooleanNetwork.computeAverageNumberSelfLoopsPerNode(bn) == 0);
            Assert.assertFalse("Almeno un nodo presenta un self-loop!", bn.getNodes().stream().anyMatch(x -> bn.getIncomingNodes(x).contains(x)));

            iterations--;
        }
    }


    @Test
    public void TestBNmiRNA() {
        Random pseudoRandom;
        /* wrapped BN */
        BooleanNetwork<BitSet, Boolean> bn;
        int nodesNumber = 30;
        int k = 3;
        double bias = 0.5;

        /* miRNA */
        miRNABooleanNetwork<BitSet, Boolean> miRNAbn;
        int miRNA_nodes = 5;
        int miRNA_K = 2;
        double miRNA_bias = 0.5;
        int miRNA_FanOut = 3;

        while (iterations > 0) {
            pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(pureRandomGenerator.nextLong());
            if (iterations % 2 == 0) {
                bn = new RBNExactBias(nodesNumber, k, bias, pseudoRandom);
            } else {
                bn = new RBN(nodesNumber, k, bias, pseudoRandom);
            }

            miRNAbn = BooleanNetworkFactory.miRNANetworkInstance(bn, miRNA_nodes, miRNA_K, miRNA_bias, miRNA_FanOut, pseudoRandom);

            // controllare che la rete wrapped sia la stessa di quella di partenza

            assertTrue("Wrapped BN different from original BN", bn.equals(miRNAbn.getWrappedBooleanNetwork()));

            // controllare il numero di nodi totale sia bn.getNodes + nodi miRNA

            assertTrue("Total nodes number is wrong", miRNAbn.getNodesNumber() == bn.getNodesNumber() + miRNA_nodes);

            // controllare che le funzioni dei nodi a valle dei miRNA abbiano false quando il miRNA è attivo
            // possibilmente controllandolo tramite agggiornamento della rete e non dalle tabelle che dovrebbe già essere vero!

            final int max = miRNAbn.miRNANodes().size();
            final int miRNAIndexInExam = miRNAbn.miRNANodes().get(pseudoRandom.nextInt(max)).getId(); // indice di un miRNA
            // creo stato random
            int[] indices = pseudoRandom.ints(0, miRNAbn.getNodesNumber()).limit(10).toArray();
            BinaryState state = new ImmutableBinaryState(miRNAbn.getNodesNumber(), indices);

            if (!state.getNodeValue(miRNAIndexInExam)) {
                state = state.flipNodesValues(miRNAIndexInExam); //settiamo a 1 il miRNA
            }

            for (Node<BitSet, Boolean> n : miRNAbn.miRNADownstreamNodes()) {
                if (miRNAbn.getIncomingNodes(n).stream().map(Node::getId).anyMatch(x -> x == miRNAIndexInExam)) {

                    if (!state.getNodeValue(n.getId())) {
                        state = state.flipNodesValues(n.getId()); //settiamo a 1 tutti i nodi che il miRNA influenza
                    }
                }
            }

            Dynamics<BinaryState> dyn = new SynchronousDynamicsImpl(miRNAbn);
            BinaryState nextState = dyn.nextState(state); // aggiorno lo stato e ora devo verificare che tutti i nodi che il miRNA scelto influenza siano a 0!

            for (Node<BitSet, Boolean> n : miRNAbn.miRNADownstreamNodes()) {

                List<Integer> previousIndices = bn.getIncomingNodes(bn.getNodeById(n.getId()).get()).stream().map(Node::getId).collect(Collectors.toList());
                int previousLength = previousIndices.size();
                List<Integer> newIndices = miRNAbn.getIncomingNodes(n).stream().map(Node::getId).collect(Collectors.toList());

                assertTrue("miRNA are not appended to the list of previous incoming nodes", previousIndices.equals(newIndices.subList(0,previousLength)));

                if (miRNAbn.getIncomingNodes(n).stream().map(Node::getId).anyMatch(x -> x == miRNAIndexInExam)) {
                    assertTrue("The node " + n + " must be false since the miRNA that affects it was previously active", nextState.getNodeValue(n.getId()) == false);
                }

            }

            iterations--;
        }
    }

}

import generator.RandomnessFactory;
import interfaces.network.BooleanNetwork;
import network.RBN;
import network.RBNExactBias;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.BitSet;
import java.util.Random;

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


            assertTrue("k medio diverso da " + k, BooleanNetwork.computeActualAverageIncomingNodes(bn) == k);
            assertTrue("numero selfloop medio per nodo diverso da 0", BooleanNetwork.computeAverageNumberSelfLoopsPerNode(bn) == 0);

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


            assertTrue("bias medio diverso da " + bias, BooleanNetwork.computeActualAverageBias(bn) == bias);
            assertTrue("numero selfloop medio per nodo diverso da 0", BooleanNetwork.computeAverageNumberSelfLoopsPerNode(bn) == 0);

            iterations--;
        }
    }

}

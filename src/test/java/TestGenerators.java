import generator.BagOfStatesGenerator;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.BeforeClass;
import org.junit.Test;
import states.ImmutableBinaryState;
import states.States;
import utility.GenericUtility;
import utility.RandomnessFactory;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class TestGenerators {

    static RandomGenerator randomInstance;

    @BeforeClass
    public static void initializationRunOnce() {
        randomInstance = RandomnessFactory.getPureRandomGenerator();
    }

    @Test
    public void test_bagOfStatesGenerator() {
        int MAX_VALUE = 1024;
        int i = 100;
        int num = 100;
        Set<BinaryState> set = new HashSet<>();
        while(i > 0) {
            int decimalState = randomInstance.nextInt(MAX_VALUE);
            int numNodes = GenericUtility.log(MAX_VALUE, 2);

            BitSet b = States.convert(decimalState, numNodes);
            BinaryState bs = new ImmutableBinaryState(numNodes,b );
            if (!set.contains(bs)) {
                set.add(bs);
                i--;
            }
        }

        Generator<BinaryState> gen = new BagOfStatesGenerator<>(set);
        assertEquals("Num. of samples is different from the expected value!",gen.totalNumberOfSamplesToBeGenerated().intValue(), num);

        Set<BinaryState> checkSet = new HashSet<>();
        BinaryState bs = gen.nextSample();
        while(bs != null){
            checkSet.add(bs);
            bs = gen.nextSample();
        }

        assertEquals("Samples generated different from the original ones!",checkSet, set);

    }




}

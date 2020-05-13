import dynamic.FrozenNodesDynamicsDecorator;
import dynamic.KnockOutKnockInDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import io.vavr.Tuple2;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.BeforeClass;
import org.junit.Test;
import utility.RandomnessFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class TestDynamics {

    static RandomGenerator randomInstance;
    static int iterations;
    static int nodesNumber = 20;
    static BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn;

    @BeforeClass
    public static void initializationRunOnce() {
        randomInstance = RandomnessFactory.getPureRandomGenerator();
        bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL,BooleanNetworkFactory.SelfLoop.WITHOUT,nodesNumber, 3, 0.5, randomInstance);
    }

    /**
     * Test for the frozen dynamics decorator
     */
    @Test
    public void test_FrozenDynamicsDecorator(){

        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());

        List<Integer> indicesToFreeze = List.of(randomInstance.nextInt(nodesNumber),
                                                                randomInstance.nextInt(nodesNumber),
                                                                randomInstance.nextInt(nodesNumber));

        System.out.println("indicesToFreeze");
        System.out.println(indicesToFreeze);
        Dynamics<BinaryState> onlySynchronousUpdate = new SynchronousDynamicsImpl(bn);

        Dynamics<BinaryState> dynamics = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new FrozenNodesDynamicsDecorator(dyn, indicesToFreeze));

        List<Tuple2<Integer,Boolean>> nodesToFreezeCheck;
        List<Tuple2<Integer,Boolean>> nodesNOTtoFreezeCheck;

        BinaryState stateTemp;
        //TEST FOR 50 SAMPLES (INTIAL STATES)
        for (int i = 0; i < 100; i++) {
            Boolean firstTimeWeUpdate = Boolean.TRUE;
            nodesToFreezeCheck = new ArrayList<>();
            BinaryState sample = generator.nextSample();
            //100 STEPS OF UPDATING FOR EACH INITIAL STATE
            /*System.out.println("SAMPLE");
            System.out.println(sample);*/
            for (int j = 0; j < 80; j++) {
                //read nodes that must be frozen
                if (firstTimeWeUpdate){
                    for (Integer idxC : indicesToFreeze) {
                        nodesToFreezeCheck.add(new Tuple2<>(idxC, sample.getNodeValue(idxC)));
                    }
                    firstTimeWeUpdate = Boolean.FALSE;
                }
                /*System.out.println("toFreeze");
                System.out.println(nodesToFreezeCheck);*/

                stateTemp = sample; //copy of the state
                /*System.out.println("stateTemp");
                System.out.println(stateTemp);*/
                //UPDATE
                sample = dynamics.nextState(sample);
                /*System.out.println("sample (AfterUpdateDECORATED)");
                System.out.println(sample);*/
                //read nodes that must not freeze
                nodesNOTtoFreezeCheck = new ArrayList<>();//in every step we must check the nodes that will not be frozen
                for (int x = 0; x < sample.getLength(); x++) {
                    if (!indicesToFreeze.contains(x)){
                        nodesNOTtoFreezeCheck.add(new Tuple2<>(x, sample.getNodeValue(x)));
                    }
                }
                /*System.out.println("NOTtoFreeze");
                System.out.println(nodesNOTtoFreezeCheck);*/
                //compare FROZEN nodes
                for (Tuple2<Integer,Boolean> frozen: nodesToFreezeCheck) {
                    assertEquals("Nodes specified by means of the list MUST BE FROZEN (SAME VALUES)",frozen._2(), sample.getNodeValue(frozen._1()));
                }

                stateTemp = onlySynchronousUpdate.nextState(stateTemp);
                /*System.out.println("stateTemp (AfterUpdateSync)");
                System.out.println(stateTemp);*/
                //Check if other nodes (not frozen) remain the same as the sync update
                for (Tuple2<Integer,Boolean> notFrozen : nodesNOTtoFreezeCheck) {
                    assertEquals("Nodes NOT FROZEN must be the same with the 2 different updating schemes",notFrozen._2(), stateTemp.getNodeValue(notFrozen._1()));
                }
            }
        }

    }


    /**
     * Test for the frozen dynamics decorator
     */
    @Test
    public void test_KnockOutDynamicsDecorator(){

        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());

        List<Integer> indices = IntStream.range(0, bn.getNodesNumber()).boxed().collect(Collectors.toList());
        Collections.shuffle(indices);
        List<Integer> indicesToKnockOut = indices.subList(0,3);
        List<Integer> indicesToKnockIn = indices.subList(3,6);

        Integer[] indicesToKnockOutArray = indicesToKnockOut.toArray(new Integer[0]);
        Integer[] indicesToKnockInArray = indicesToKnockIn.toArray(new Integer[0]);

        System.out.println("indicesToKnockOut");
        System.out.println(indicesToKnockOut);
        System.out.println("indicesToKnockIn");
        System.out.println(indicesToKnockIn);

        Dynamics<BinaryState> onlySynchronousUpdate = new SynchronousDynamicsImpl(bn);

        Dynamics<BinaryState> dynamics = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new KnockOutKnockInDynamicsDecorator(dyn, indicesToKnockOut,indicesToKnockIn));

        List<Tuple2<Integer,Boolean>> nodesNOTToKnockOutOrInCheck;

        BinaryState stateTemp;
        //TEST FOR 50 SAMPLES (INTIAL STATES)
        for (int i = 0; i < 100; i++) {
            BinaryState sample = generator.nextSample();
            //PUT TO ZERO THE indicesToKnockOut
            sample = sample.setNodesValues(Boolean.FALSE, indicesToKnockOutArray);
            sample = sample.setNodesValues(Boolean.TRUE, indicesToKnockInArray);

            //100 STEPS OF UPDATING FOR EACH INITIAL STATE
            /*System.out.println("SAMPLE");
            System.out.println(sample);*/
            for (int j = 0; j < 80; j++) {
                stateTemp = sample; //copy of the state
                /*System.out.println("stateTemp");
                System.out.println(stateTemp);*/
                //UPDATE
                sample = dynamics.nextState(sample);
                /*System.out.println("sample (AfterUpdateDECORATED)");
                System.out.println(sample);*/
                //read nodes that must not freeze
                nodesNOTToKnockOutOrInCheck = new ArrayList<>();//in every step we must check the nodes that will not be frozen
                for (int x = 0; x < sample.getLength(); x++) {
                    if (!indicesToKnockOut.contains(x) && !indicesToKnockIn.contains(x)){
                        nodesNOTToKnockOutOrInCheck.add(new Tuple2<>(x, sample.getNodeValue(x)));
                    }
                }
                /*System.out.println("nodesNOTToKnockOutCheck");
                System.out.println(nodesNOTToKnockOutCheck);*/
                //compare KO nodes
                for (Integer idx : indicesToKnockOut) {
                    assertEquals("Nodes specified by means of the list indicesToKnockOut MUST BE KO (FALSE)",Boolean.FALSE, sample.getNodeValue(idx));
                }
                for (Integer idx : indicesToKnockIn) {
                    assertEquals("Nodes specified by means of the list indicesToKnockIn MUST BE IN (TRUE)",Boolean.TRUE, sample.getNodeValue(idx));
                }

                stateTemp = onlySynchronousUpdate.nextState(stateTemp);
                /*System.out.println("stateTemp (AfterUpdateSync)");
                System.out.println(stateTemp);*/
                //Check if other nodes (not knocked-out) remain the same as the sync update
                for (Tuple2<Integer,Boolean> notKO : nodesNOTToKnockOutOrInCheck) {
                    assertEquals("Nodes NOT KO must be the same with the 2 different updating schemes",notKO._2(), stateTemp.getNodeValue(notKO._1()));
                }
            }
        }

    }

}

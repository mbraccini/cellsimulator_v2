package experiments.methylation;

import dynamic.KnockOutDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.BagOfStatesGenerator;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import network.BNKBiasImpl;
import network.TableSupplierCanalizingK2;
import org.apache.commons.math3.random.RandomGenerator;
import states.States;
import tes.StaticAnalysisTES;
import utility.Files;
import utility.RandomnessFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

public class MainKnockOutTrees {


    static BigInteger INITIAL_SAMPLES_STATES_NUMBER = BigInteger.valueOf(10000);
    static Integer NETWORK_NUMBER = 10;

    public static void main(String args[]) {

        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        int numNodes = 100;
        int k = 2;
        double bias = 0.5;

        int[] stepOfGenesKO = new int[]{2, 5, 10};
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;

        for (int i = 0; i < NETWORK_NUMBER; i++) {
            bn = new BNKBiasImpl(numNodes, r, Boolean.FALSE, new TableSupplierCanalizingK2(numNodes, r));
            //bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.EXACT, BooleanNetworkFactory.SelfLoop.WITHOUT,numNodes,k,  bias, r);
            int id = r.nextInt(10000);
            for (int config : stepOfGenesKO) {
                forEachConfiguration(bn, id, numNodes, k, bias, r, config);
            }
        }

    }

    static private void forEachConfiguration(BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                             int bn_id,
                                             int numNodes,
                                             int k,
                                             double bias,
                                             RandomGenerator r,
                                             int stepOfGenesKO
    ) {


        //BN (wild-type) ANALYSIS
        Dynamics<BinaryState> dynWildType = new SynchronousDynamicsImpl(bn);
        Generator<BinaryState> genWildType = new UniformlyDistributedGenerator(INITIAL_SAMPLES_STATES_NUMBER, bn.getNodesNumber(), r);
        Attractors<BinaryState> attrsWildType = StaticAnalysisTES.attractors(genWildType, dynWildType);


        int numOfGenesToKO = stepOfGenesKO;
        String parentDir = "bn_"+ bn_id +"_" + stepOfGenesKO + Files.FILE_SEPARATOR;
        Files.createDirectories(parentDir);
        Files.writeBooleanNetworkToFile(bn, parentDir + "bn");



        for (ImmutableAttractor<BinaryState> att : attrsWildType) {
            DFS_onDisk(parentDir, att, numOfGenesToKO, stepOfGenesKO, numNodes, bn);
        }



        //****** generator from attractors' states *******//*
        //Set<BinaryState> newInitialStates = attrsWildType.getAttractors().stream().flatMap(a -> a.getStates().stream()).collect(Collectors.toSet());
        //Generator<BinaryState> genFrozenType = new BagOfStatesGenerator<>(newInitialStates);
        //****** RND generator ***************************//*
        //Generator<BinaryState> genFrozenType = new UniformlyDistributedGenerator(INITIAL_SAMPLES_STATES_NUMBER, bn.getNodesNumber(), r);
        //****** generator RND + 1 step for each state ***//*
        //genFrozenType = new BagOfStatesGenerator<>(Stream.generate(genFrozenType::nextSample).limit(INITIAL_SAMPLES_STATES_NUMBER.intValue()).map(dynWildType::nextState).collect(Collectors.toList()));

        //Attractors<BinaryState> attrsFrozenType = StaticAnalysisTES.attractors(genFrozenType, dynFrozenType);

    }


    public static void DFS_onDisk(String path, ImmutableAttractor<BinaryState> father, int numOfGenesToKO, int stepOfGenesKO, int numNodes, BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn ) {
        String myPath = path + States.bitSetToHex(father.getFirstState().toBitSet()) + "_" + father.getLength() + Files.FILE_SEPARATOR;
        Files.createDirectories( myPath);
        if (numOfGenesToKO <= numNodes) {
            //indices of nodes to KO
            List<Integer> indicesToKnockOut = new ArrayList<>();
            for (int i = 0; i < numOfGenesToKO; i++) {
                indicesToKnockOut.add(i);
            }
            //KNOCK OUT DYNAMICS
            Dynamics<BinaryState> dynamicsKO = DecoratingDynamics
                    .from(new SynchronousDynamicsImpl(bn))
                    .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToKnockOut));
            //FOR EACH ATTRACTOR
            Generator<BinaryState> genFromAttStates = new BagOfStatesGenerator<>(father.getStates()
                                                                                .stream()
                                                                                .map(x -> x.setNodesValues(Boolean.FALSE, indicesToKnockOut.toArray(new Integer[0])))
                                                                                .collect(Collectors.toSet()));
            Attractors<BinaryState> myAttrs = StaticAnalysisTES.attractors(genFromAttStates, dynamicsKO);
            for (ImmutableAttractor<BinaryState> att : myAttrs) {
                DFS_onDisk(myPath, att, numOfGenesToKO + stepOfGenesKO,stepOfGenesKO, numNodes, bn);
            }
        }
    }



}
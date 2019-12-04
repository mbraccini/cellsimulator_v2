package experiments.methylation;

import attractor.AttractorsUtility;
import dynamic.KnockOutDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.BagOfStatesGenerator;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import simulator.AttractorsFinderService;
import states.ImmutableRealState;
import utility.Files;
import utility.RandomnessFactory;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class CheckRobertoModelFixedNodes {

    private static DecimalFormat df2 = new DecimalFormat("#.#");


    public static Optional<ImmutableAttractor<BinaryState>> retrieveSignificantAttractor(final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                                                         final RandomGenerator r){

        Generator<BinaryState> gen = new UniformlyDistributedGenerator(BigInteger.valueOf(1000), bn.getNodesNumber(),r);
        Dynamics<BinaryState> dyn = new SynchronousDynamicsImpl(bn);
        Attractors<BinaryState> atts = AttractorsFinderService.apply(gen,dyn,true,true,AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(bn.getNodesNumber()));
        return atts.getAttractors().stream().findFirst();
    }

    public static List<Integer> nodesWithMeanExpressionValueStrictlyGreaterThanZeroAndLowerThanOne(ImmutableRealState a){
        int l = a.getLength();
        //System.out.println(a);
        List<Integer> list_indices = IntStream.range(0,l).boxed().filter(x -> a.getNodeValue(x) > 0 && a.getNodeValue(x) < 1).collect(Collectors.toList());
        //System.out.println(list_indices);
        return list_indices;
    }

    public static List<Integer> fixedNodes(ImmutableRealState a) {
        int l = a.getLength();
        //System.out.println("reaced MEan: " + a);

        List<Integer> list_indices = IntStream.range(0,l)
                                            .boxed()
                                            .peek(x -> System.out.println(a.getNodeValue(x)))
                                            .filter(x -> a.getNodeValue(x).equals(0.0) || a.getNodeValue(x).equals(1.0))

                                            .collect(Collectors.toList());
        //System.out.println("fixed indices list: " + list_indices);

        return list_indices;
    }

    public static List<List<Integer>> experiment(final int nodesNumber,
                                          final int k,
                                          final double bias,
                                          final int nodesToFreeze,
                                          final int howManyNetworks,
                                          final RandomGenerator r){

        List<Integer> list_indices = IntStream.range(0,nodesNumber).boxed().collect(toList());

        //System.out.println("nodesToFreeze: \n" + nodesToFreeze);
        List<List<Integer>> results = new ArrayList<>();
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
        int net = 0;
        do {
            //System.out.println("net: \n" + net);
            bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
            Optional<ImmutableAttractor<BinaryState>> potentialAttractor = retrieveSignificantAttractor(bn, r);
            if( !potentialAttractor.isPresent() || potentialAttractor.get().isFixedPoint()){
                continue;
            }
            ImmutableAttractor<BinaryState>startingAttr = potentialAttractor.get();
            ImmutableRealState meanAttr = AttractorsUtility.attractorMeanRepresentativeState(startingAttr);
            List<Integer> notZeroAndNotOneIndices = nodesWithMeanExpressionValueStrictlyGreaterThanZeroAndLowerThanOne(meanAttr);
            List<Integer> fixed = fixedNodes(meanAttr);
            Set<Integer> setFixed = new HashSet<>();
            setFixed.addAll(fixed);

            if (notZeroAndNotOneIndices.size() < nodesToFreeze) {
                //System.out.println("CONTINUE");
                continue;
            }
            BinaryState sample = startingAttr.getStates().get(startingAttr.getStates().size() - 1); //PRENDIAMO L'ULTIMO IN ORDINE LESSICOGRAFICO COSì DOVREBBE AVERE PIU' NODI A 1
            List<Integer> actualNodesToFreeze = notZeroAndNotOneIndices.subList(0,nodesToFreeze);
            Tuple2<List<Integer>,Integer> storyResult = followStory(sample,bn,actualNodesToFreeze,setFixed);
            List<Integer> res = new ArrayList<>();
            res.add(fixed.size());
            res.addAll(storyResult._1());
            res.add(storyResult._2());
            results.add(res);
            net++;
        } while(net < howManyNetworks);

        return results;
    }

    public static Tuple2<List<Integer>,Integer> followStory(final BinaryState sampleNotFrozen,
                                     final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                     final List<Integer> indicesToKnockOut,
                                     final Set<Integer> fixedInStartingAttractor){

        //System.out.println("sampleNO \n" +sampleNotFrozen);

        BinaryState sample = sampleNotFrozen.setNodesValues(Boolean.FALSE, indicesToKnockOut.toArray(new Integer[0]));
        //System.out.println("sample \n" +sample);

        ImmutableAttractor<BinaryState> reachedAttractor = getFrozenAttractor(sample,bn,indicesToKnockOut);
        //System.out.println(att);


        ImmutableRealState meanReachedAttr = AttractorsUtility.attractorMeanRepresentativeState(reachedAttractor);
        List<Integer> fixedNodesInReachedAttractor = fixedNodes(meanReachedAttr);
        Set<Integer> setFixedNodesInReachedAttractor = new HashSet<>();
        setFixedNodesInReachedAttractor.addAll(fixedNodesInReachedAttractor);

        Set<Integer> setDifference = new HashSet<>(setFixedNodesInReachedAttractor);
        setDifference.removeAll(indicesToKnockOut);//rimuovo i congelati

        List<BinaryState> traj = returnTrajectory(sample,bn,indicesToKnockOut,reachedAttractor);
        //analizziamo la traiettoria a ritroso

        List<Integer> countFixedNodesPerStep = new ArrayList<>();
        BinaryState state = null;
        BinaryState referenceStateReachedAttractor = reachedAttractor.getFirstState(); //stato di riferimento tanto dobbiamo guardare i nodi fissi e per definizione lo sono in tuitti gli stati che compongono l'attrattore
        for (int i = 0; i < traj.size(); i++) {
            state = traj.get(traj.size() - 1 - i);
            int countOfFixed = 0;
            for (Iterator<Integer> iterator = setDifference.iterator(); iterator.hasNext();) {
                Integer aFixedNodeIndex = iterator.next();
                if (state.getNodeValue(aFixedNodeIndex).equals(referenceStateReachedAttractor.getNodeValue(aFixedNodeIndex))){
                    countOfFixed++;
                } else {
                    iterator.remove();//Se non è uguale al valore che assume nell'attrattore lo devo rimuovere perché vuol dire che da lì in poi(all'indietro) non è stato fisso
                }
            }
            countFixedNodesPerStep.add(countOfFixed);
        }
        Collections.reverse(countFixedNodesPerStep);


        return new Tuple2<>(countFixedNodesPerStep,fixedNodesInReachedAttractor.size() - indicesToKnockOut.size());
    }

    public static List<BinaryState> returnTrajectory(
            final BinaryState sample,
            final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
            final List<Integer> indicesToKnockOut,
            final ImmutableAttractor<BinaryState> reachedAttractor){

        Dynamics<BinaryState> dynKO = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToKnockOut));

        List<BinaryState> trajectory = new ArrayList<>();

        BinaryState previousState = sample;
        BinaryState nextState = previousState;
        //System.out.println("sample \n" +sample);
        while(!belongToAttractor(nextState,reachedAttractor)){
            nextState = dynKO.nextState(previousState);
            trajectory.add(nextState);

            previousState=nextState;
        }
        if (trajectory.size() > 0){
            trajectory.remove(trajectory.size() - 1); //rimuovo l'ultimo elemento perché fa parte dell'attrattore
        }

        return trajectory;
    }

    public static ImmutableAttractor<BinaryState> getFrozenAttractor(final BinaryState sample,
                                                                     final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                                     final List<Integer> indicesToKnockOut){

        Dynamics<BinaryState> dynamicsKO = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToKnockOut));

        Attractors<BinaryState> att = AttractorsFinderService.apply(new BagOfStatesGenerator<>(List.of(sample)),
                dynamicsKO,
                true,
                true,
                AttractorsFinderService.TRUE_TERMINATION);

        return att.getAttractors().get(0);
    }



    public static Boolean belongToAttractor(BinaryState state, ImmutableAttractor<BinaryState> att){
        return att.getStates().stream().anyMatch(x -> x.equals(state));
    }


    public static void main(String args[]){
        System.out.println("Checking Roberto's Model");
        RandomGenerator r = RandomnessFactory.newPseudoRandomGenerator(2);

        final int howManyNetworks = 100;
        final int nodesNumber = 100;
        final int k = 2;
        final double bias = 0.5;

        //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        //LocalDateTime now = LocalDateTime.now();
        System.out.println();

        String dir = "fixedPropagation"+ Files.FILE_SEPARATOR; //+ dtf.format(now) + Files.FILE_SEPARATOR;
        Files.createDirectories(dir);

        List.of(1,3,5,10).stream().forEach(  toFreeze -> {
            Files.writeListsToCsv(experiment(nodesNumber, k, bias, toFreeze, howManyNetworks, r), dir + "res_toFreeze" + toFreeze);
            });
    }
}



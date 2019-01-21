package experiments.methylation;

import com.google.common.base.Supplier;
import dynamic.KnockOutDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.BagOfStatesGenerator;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractor;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import interfaces.tes.DifferentiationNode;
import interfaces.tes.DifferentiationTree;
import io.vavr.Tuple2;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import states.ImmutableBinaryState;
import tes.DifferentiationNodeImpl;
import tes.DifferentiationTreeImpl;
import tes.StaticAnalysisTES;
import utility.Files;
import utility.GenericUtility;
import utility.RandomnessFactory;
import visualization.DifferentiationTreeGraphViz;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MainFrozenTree {

    //SE IL CONGELAMENTO PUò CREARE VARIETà/DIVERSITà

    static final String COMBINATIONS_FOR_BIGGER_ATTR = "10000";

    static class DifferentiationFrozenTreeGraphViz extends DifferentiationTreeGraphViz<FrozenNode>{

        public DifferentiationFrozenTreeGraphViz(DifferentiationTree<FrozenNode> tree) {
            super(tree);
        }

        @Override
        protected String nodeInfo(DifferentiationNode<FrozenNode> DifferentiationNode) {
            return 	"\""
                    + "p:"      + DifferentiationNode.getWrappedElement().getAttractor().getLength()
                    + "lvl:"    + DifferentiationNode.getLevel()
                    + "lbl:"    + (DifferentiationNode.getWrappedElement().getEquivalence())

                    //+ GraphViz.NEW_LINE_DOT_ESCAPE
                    //AttractorsUtility.attractorMeanRepresentativeState(DifferentiationNode.getWrappedElement().getAttractor())

            + "\"";

        }
    }

    static class FrozenNode{

        private final Attractor<BinaryState> a;
        private final List<Integer> frozenIndices;
        private String label = null;
        private final Set<Integer> historyOfFrozen;
        private boolean EQUALS;

        FrozenNode(final Attractor<BinaryState> a, List<Integer> frozenIndices, Set<Integer> historyOfFrozen, boolean EQUALS){
            this.a = a;
            this.frozenIndices = frozenIndices;
            this.historyOfFrozen = (historyOfFrozen == null ? Set.of() : historyOfFrozen);
            this.EQUALS = EQUALS;
        }

        public void setEQUALS(boolean EQUALS){
            this.EQUALS = EQUALS;
        }

        public Attractor<BinaryState> getAttractor() {
            return a;
        }

        public List<Integer> getFrozenIndices() {
            return frozenIndices;
        }

        public void setEquivalence(final String label) {
            this.label = label;
        }
        public String getEquivalence(){
            return this.label;
        }

        public Set<Integer> getHistoryOfFrozen(){
            return this.historyOfFrozen;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FrozenNode that = (FrozenNode) o;
            //EQUALS
            if (EQUALS) return false;
            if (a.getLength().intValue() != that.a.getLength().intValue()) return false;
            Set<Integer> indices = new HashSet<>(historyOfFrozen);

            indices.addAll(frozenIndices);
            indices.addAll(that.frozenIndices);
            indices.addAll(that.historyOfFrozen);
            Set<Integer> all = IntStream.range(0,a.getStates().get(0).getLength()).boxed().collect(Collectors.toSet());
            all.removeAll(indices);

            List<Integer> indicesToMaintainList = new ArrayList<>(all);
            Collections.sort(indicesToMaintainList);

            List<BinaryState> thisAttractor = constructNewAttractor(a, indicesToMaintainList);
            List<BinaryState> otherAttractor = constructNewAttractor(that.getAttractor(), indicesToMaintainList);
            //System.out.println("Idx: " + indicesToMaintainList + ", this:" + thisAttractor + ", other:"+ otherAttractor);


            return thisAttractor.equals(otherAttractor);
        }

        private List<BinaryState> constructNewAttractor(Attractor<BinaryState> oldAttractor,  List<Integer> indicesToMaintainList){
            List<BinaryState> thisAttractor = new ArrayList<>();

            for (BinaryState state: oldAttractor.getStates()) {
                BitSet newBit = new BitSet(indicesToMaintainList.size());
                int i = 0;
                for (Integer j : indicesToMaintainList) {
                    newBit.set(i,state.getNodeValue(j));
                    i++;
                }
                thisAttractor.add(new ImmutableBinaryState(indicesToMaintainList.size(), newBit));
            }

            Collections.sort(thisAttractor);
            return thisAttractor;
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, frozenIndices, historyOfFrozen);
        }

        @Override
        public String toString() {
            return "{" +
                    "att=" + a +
                    ", frzIndex=" + frozenIndices +
                    ", lbl=" + (label == null ? "" : label) +
                    '}';
        }
    }

    public static void main(String args[]){
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        int numNodes = Integer.valueOf(args[0]);
        boolean treeFile = Boolean.valueOf(args[1]);
        int numOfFrozenNodesPerStep = Integer.valueOf(args[2]);
        int k = Integer.valueOf(args[3]);
        double bias = Double.valueOf(args[4]);

        /*int numNodes = 40;
        boolean treeFile = false;
        int k = 2;
        double bias = 0.5;
        final int numOfFrozenNodesPerStep = 2;*/

        final int[] numChildrenPerNode = new int[]{2, 4};
        final int SAMPLES = 30;
        System.out.println("...MainFrozenTree...\n" +
                "numNodes: " + numNodes + "\n" +
                "treeFile: " + treeFile + "\n" +
                "numOfFrozenNodesPerStep: " + numOfFrozenNodesPerStep + "\n" +
                "k: " + k + "\n" +
                "bias: " + bias
        );

        String sep = ",";
        for (int b: numChildrenPerNode) {
            //FILES
            String pathFolder = "b_" + b  + Files.FILE_SEPARATOR;
            Files.createDirectories(pathFolder);
            StringBuilder sb = new StringBuilder();
            // header generic stats
            sb.append("distinctPathsALLtree");
            sb.append(sep);
            sb.append("levelMaxDistinct");
            sb.append(sep);
            sb.append("relativeMaxDistinct");
            sb.append("\n");


            try (BufferedWriter csv = new BufferedWriter(new FileWriter(pathFolder + b + "_stats.csv", true))) {
                //header
                csv.append("level");
                csv.append(sep);
                csv.append("r");
                //
                csv.append("\n");
                String ss = "";
                for (int i = 0; i < SAMPLES; i++) {
                    try{
                        ss = forEachBN(numNodes, k, bias, r,  b, numOfFrozenNodesPerStep, i, sep, pathFolder, treeFile, sb);
                        csv.append(ss);
                    } catch(IllegalStateException a) {
                        System.err.println("Recupero da situazione di errore...");
                        i = i - 1;
                    }
                }
            } catch (IOException e) {
               e.printStackTrace();
            }
            String FILEGenericStats = pathFolder + "genStats_b_" + b;

            try (PrintWriter out = new PrintWriter(FILEGenericStats + ".txt")) {
                out.println(sb.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    static private String forEachBN(final int numNodes, final int k, final double bias, final RandomGenerator r,final int times, final int numOfIndices, final int ID, String sep, String pathFolder, boolean treeFile, StringBuilder sb){
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
        bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.EXACT, BooleanNetworkFactory.SelfLoop.WITHOUT, numNodes,k,  bias, r);

        ImmutableAttractor<BinaryState> bigger  = searchAndTakeBiggerAttractor(bn, numNodes,r);


        DifferentiationNode<FrozenNode> root = new DifferentiationNodeImpl<>(
                new FrozenNode(bigger, null, null, Boolean.TRUE)
        );
        root.setLevel(0);
        DifferentiationTree<FrozenNode> tree = new DifferentiationTreeImpl<>(List.of(root));


        Set<Set<Integer>> sets = indicesSibling(numNodes, r,times, numOfIndices);

        //System.out.println(bigger);

        //System.out.println(AttractorsUtility.attractorMeanRepresentativeState(bigger));
        for (Set<Integer> set: sets ) {
            DFS(bigger.getFirstState(), bn, numNodes, new ArrayList<>(set), root, null, r, times, numOfIndices, 1);
        }

        GenericUtility.UniqueNameGenerator nameGen = GenericUtility.newNameGenerator(r);
        List<DifferentiationNode<FrozenNode>> nodes;
        final int codeLen = 4;

        //System.out.println(tree.getTreeRepresentation());


        //ANALYZING THE TREE
        //PUTS label into nodes' tree
        for (int i = 1; i < tree.getLevelsNumber() ; i++) {
            Optional<Set<DifferentiationNode<FrozenNode>>> lvl = tree.getLevel(i);
            if (!lvl.isPresent() || !(lvl.get().size() == Math.pow(times,i))) {
                throw new IllegalStateException();
            } else {
                lvl.get().stream().forEach(y -> y.getWrappedElement().setEQUALS(Boolean.FALSE)); //ABILITO EQUALS
                nodes = new ArrayList<>(lvl.get());
                for (int j = 0; j < nodes.size(); j++) {
                    DifferentiationNode<FrozenNode> n = nodes.get(j);
                    if (n.getWrappedElement().getEquivalence() == null) {
                        String eq = nameGen.generateRandomAlphabeticNotAlreadyUsed(codeLen);
                        n.getWrappedElement().setEquivalence(eq);

                        //SE è l'ultimo elemento
                        if( j != (nodes.size() - 1) ) {
                            for (DifferentiationNode<FrozenNode> other : nodes.subList(j + 1, nodes.size())) {
                                if (other.getWrappedElement().getEquivalence() == null
                                        && n.getWrappedElement().equals(other.getWrappedElement())) {
                                    other.getWrappedElement().setEquivalence(eq);
                                }
                            }
                        }
                    }
                }
            }
        }

        // PATHS COMPUTATION
        List<List<String>> allPaths = new ArrayList<>();
        DFSDifferentPaths(allPaths, new Stack<>(), root);

        sb.append(distinctPaths(allPaths));
        sb.append(sep);
        Tuple2<Integer,Integer> recursiveChechPath = recursiveMaxNumberOfDistinctPaths(allPaths);
        sb.append(recursiveChechPath._2());
        sb.append(sep);
        sb.append(recursiveChechPath._1());
        sb.append("\n");

        Files.writeBooleanNetworkToFile(bn, pathFolder + ID + "_bn");
        if (treeFile)
            new DifferentiationFrozenTreeGraphViz(tree).saveOnDisk( pathFolder + ID + "_diffTree");
        return treeStatsPerLevel(tree, pathFolder, ID, sep, times);

    }

    static private long distinctPaths(List<List<String>> allPaths){
        return allPaths.stream().distinct().count();
    }

    static private Tuple2<Integer,Integer> recursiveMaxNumberOfDistinctPaths(List<List<String>> allPaths){
        long maxDistinct = -1;
        int levelMaxDistinct = - 1;
        int actualLevel = allPaths.get(0).size() - 1;
        long d;
        while(allPaths.get(0).size() > 1) {
            d = distinctPaths(allPaths);
            if (d > maxDistinct) {
                maxDistinct = d;
                levelMaxDistinct = actualLevel;
            }
            //remove one element from each list
            allPaths.forEach(x -> x.remove(x.size() - 1));
            //System.out.println(allPaths);
            actualLevel--;
        }

        //System.out.println("levelMaxDistinct" + levelMaxDistinct);
        //System.out.println("maxDistinct" + maxDistinct );
        return new Tuple2<>((int) maxDistinct, levelMaxDistinct);
    }

    /**
     * Compute all paths
     * @param allPaths
     * @param previousLbls
     * @param node
     */
    static private void DFSDifferentPaths (List<List<String>> allPaths, Stack<String> previousLbls, DifferentiationNode<FrozenNode> node){
        previousLbls.push(node.getWrappedElement().getEquivalence());
        if (Objects.isNull(node.getChildren())) {
            //non ha più figli
            allPaths.add(new ArrayList<>(previousLbls));
            previousLbls.pop();
        } else {
            for (DifferentiationNode<FrozenNode> c : node.getChildren()) {
                DFSDifferentPaths(allPaths, previousLbls, c);
            }
            previousLbls.pop();
        }
    }



    static private String treeStatsPerLevel(DifferentiationTree<FrozenNode> tree, String pathFolder, final int ID, String sep, final int branching){
        //COMPUTE r
        String s;
        StringBuilder sb = new StringBuilder();
        // COUNT FRACTION OF DIFFERENT ATTRS PER LEVEL
        //try (BufferedWriter csv = new BufferedWriter(new FileWriter(pathFolder + ID + "_stats.csv", true),10)) {
            for (int i = 1; i < tree.getLevelsNumber(); i++) {
                Optional<Set<DifferentiationNode<FrozenNode>>> lvl = tree.getLevel(i);
                if (lvl.isPresent()) {
                    double d = (double) lvl.get().stream()
                            .map(y -> y.getWrappedElement().getEquivalence()).distinct().count();
                    double tot = d / Math.pow(branching, i);
                    sb.append(i + sep + tot);
                    sb.append("\n");
                }
            }
        //} catch (IOException e) {
         //   e.printStackTrace();
        //}

        return sb.toString();
    }

    static private Set<Set<Integer>> indicesSibling(final int numNodes, final RandomGenerator r, final int times, final int numOfIndices){
        Supplier<Integer> supp = () -> r.nextInt(numNodes);
        Set<Set<Integer>> sets = new HashSet<>();
        int temp = times;
        while(temp > 0){
            Set<Integer> s = IntStream.range(0,numOfIndices).mapToObj(x -> supp.get()).collect(Collectors.toSet());
            if(s.size() == numOfIndices && !sets.contains(s)) {
                sets.add(s);
                temp--;
            }
        }
        return sets;
    }


    static private Set<Set<Integer>> indicesChildren(final int numNodes, final RandomGenerator r, final int times, final int numOfIndices, List<List<Integer>> toAvoid){
        Set<Integer> avoidSet  = toAvoid.stream().flatMap(x -> x.stream()).collect(Collectors.toSet());
        Set<Set<Integer>> sets = new HashSet<>();
        Set<Integer> s;
        int temp = times;
        while(temp > 0){
            s = new HashSet<>();
            int tempNumOfIndices = numOfIndices;
            while(tempNumOfIndices > 0){
                int i = r.nextInt(numNodes);
                if (!avoidSet.contains(i)) {
                    s.add(i);
                    tempNumOfIndices--;
                }
            }
            if(s.size() == numOfIndices && !sets.contains(s)) {
                sets.add(s);
                temp--;
            }
        }
        return sets;
    }

    static private ImmutableAttractor<BinaryState> searchAndTakeBiggerAttractor(BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                                       int numNodes,
                                                                       RandomGenerator r
                                                                       ){

        Generator<BinaryState> gen = new UniformlyDistributedGenerator(new BigInteger(COMBINATIONS_FOR_BIGGER_ATTR),numNodes,r);
        Dynamics<BinaryState> dyn = new SynchronousDynamicsImpl(bn);
        Attractors<BinaryState> attrs = StaticAnalysisTES.attractors(gen, dyn);

        int bigger = 0;
        ImmutableAttractor<BinaryState> attBigger = null;
        for (ImmutableAttractor<BinaryState> a : attrs) {
            if(a.getBasinSize().isPresent()){
                int temp = a.getBasinSize().get();
                if (temp > bigger){
                    attBigger = a;
                    bigger = temp;
                }
            }
        }

        return attBigger;
    }

    /*static private Tuple5<Integer, Integer, Double, Double, Integer> forEachConfiguration(BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                                                          int numOfFrozenNodes,
                                                                                          int numNodes,
                                                                                          int k,
                                                                                          double bias,
                                                                                          RandomGenerator r,
                                                                                          String path
    ) {
        //indices of nodes to KO
        List<Integer> indicesToKnockOut = new ArrayList<>();
        for (int i = 0; i < numOfFrozenNodes; i++) {
            indicesToKnockOut.add(i);
        }
        //KNOCK OUT DYNAMICS
        Dynamics<BinaryState> dynamicsKO = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToKnockOut));
        //GENERATOR
        Generator<BinaryState> genUnif =
                new UniformlyDistributedGenerator(INITIAL_SAMPLES_STATES_NUMBER, numNodes,r);

        Generator<BinaryState> genKO = new BagOfStatesGenerator<>(Stream.generate(genUnif::nextSample)
                .limit(INITIAL_SAMPLES_STATES_NUMBER.intValue())
                .map(sample -> sample.setNodesValues(Boolean.FALSE, indicesToKnockOut.toArray(new Integer[0])))
                .collect(Collectors.toList()));

        //ATTRACTORS
        Attractors<BinaryState> atts = AttractorsFinderService.apply(genKO,
                dynamicsKO,
                false,
                false,
                AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(numNodes));
        //Attractors<BinaryState> atts = StaticAnalysisTES.attractors(genKO, dynamicsKO);
        //ON DISK
        //Files.writeAttractorsToReadableFile(atts, path + "atts");
        //
        Set<Integer> blink = AttractorsUtility.blinkingAttractors(atts);
        Set<Integer> fixed = AttractorsUtility.fixedAttractors(atts);
        Double blinkFraction, fixedFraction;
        if (blink == null) {
            blinkFraction = null;
        } else {
            blinkFraction = ((double)blink.size() / numNodes);
        }
        if (fixed == null) {
            fixedFraction = null;
        } else {
            fixedFraction = ((double)fixed.size() / numNodes);
        }
        return new Tuple5<>(atts.numberOfAttractors(), atts.getNumberOfFixedPoints(),fixedFraction, blinkFraction, atts.traceabilityInfo().statistics().get("initialStatesCutOff").intValue());
    }*/


    static void DFS(BinaryState startingState,
                    BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                    int numNodes,
                    List<Integer> indicesToFreeze,
                    DifferentiationNode<FrozenNode> fatherNode,
                    List<List<Integer>> historyOfIndices,
                    RandomGenerator r,
                    final int times,
                    final int numOfIndices,
                    final int treeLevel
                   ){



        //KNOCK OUT DYNAMICS
        Dynamics<BinaryState> dynamicsKO = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToFreeze));

        //GENERATOR
        Generator<BinaryState> genStartingState =
                new BagOfStatesGenerator<>(List.of(startingState));

        Generator<BinaryState> genKO = new BagOfStatesGenerator<>(Stream.generate(genStartingState::nextSample)
                .limit(1)
                .map(sample -> sample.setNodesValues(Boolean.FALSE, indicesToFreeze.toArray(new Integer[0])))
                .collect(Collectors.toList()));
        //ATTRACTORS
        Attractors<BinaryState> atts = StaticAnalysisTES.attractors(genKO, dynamicsKO);


        //ADD TO THE TREE
        if (atts.numberOfAttractors() == 1) {
            //ADD TO THE TREE
            ImmutableAttractor<BinaryState> a = atts.getAttractors().get(0);
            DifferentiationNode<FrozenNode> child = new DifferentiationNodeImpl<>( new FrozenNode(a, indicesToFreeze, (historyOfIndices != null ? (historyOfIndices.stream().flatMap(x -> x.stream()).collect(Collectors.toSet())): null), Boolean.TRUE));
            child.setLevel(treeLevel);
            fatherNode.addChild(child);
            child.addParent(fatherNode);

            if (historyOfIndices != null && ((historyOfIndices.size() * indicesToFreeze.size()) + indicesToFreeze.size()) >= (numNodes/2)) {
                return;
            } else {
                List<List<Integer>> updatedHistory;
                if (historyOfIndices == null) {
                    updatedHistory = new ArrayList<>();
                } else {
                    updatedHistory  = new ArrayList<>(historyOfIndices);
                }

                updatedHistory.add(indicesToFreeze);
                for (Set<Integer> set: indicesChildren(numNodes,r,times,numOfIndices, updatedHistory)) {
                    DFS(a.getFirstState(), bn, numNodes, new ArrayList<>(set), child, updatedHistory, r, times, numOfIndices, treeLevel + 1);

                }
            }
        }

    }

}

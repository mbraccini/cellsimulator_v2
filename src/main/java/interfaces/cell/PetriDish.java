package interfaces.cell;

import cell.CellImpl;
import dynamic.SynchronousDynamicsImpl;
import interfaces.network.BNClassic;
import interfaces.network.BooleanNetwork;
import interfaces.network.Node;
import interfaces.network.NodeDeterministic;
import interfaces.state.BinaryState;
import interfaces.state.State;
import io.vavr.Tuple2;
import network.BooleanNetworkFactory;

import java.util.*;

public interface PetriDish<K,V,T extends State, B extends BooleanNetwork<? extends Node>> {

    StateFunction<T> getMyNoise();

    CellPopulation<K,V,T,B> getCellPopulation();

    List<SimulationStep<T>> simulate(Set<Tuple2<Integer,T>> initialStates, Integer steps);

    interface SimulationStep<T extends State> {
        /**
         * from cell with id equal to 0 to N-1
         * @return
         */
        List<T> states();
    }

    class SimulationStepImpl<T extends State> implements SimulationStep<T> {
        private final List<T> states;
        public SimulationStepImpl(List<T> states) {
            this.states = states;
        }
        @Override
        public List<T> states() {
            return Collections.unmodifiableList(states);
        }

        @Override
        public String toString() {
            return "SimStep{" + states + '}';
        }
    }


    class PetriDishImpl<K,V,T extends BinaryState, B extends BooleanNetwork<? extends NodeDeterministic<K,V>>> implements PetriDish<K,V,T,B> {

        private final StateFunction<T> noise;
        private CellPopulation<K,V,T,B> cellPopulation;
        private Cell<T,B> aCell;

        public PetriDishImpl(StateFunction<T> noise, CellPopulation<K,V,T,B> cellPopulation){
            this.noise = noise;
            this.cellPopulation = cellPopulation;
        }

        public PetriDishImpl(StateFunction<T> noise, Cell<T,B> aCell){
            this.noise = noise;
            this.aCell = aCell;
        }

        @Override
        public StateFunction<T> getMyNoise() {
            return noise;
        }

        @Override
        public CellPopulation<K,V,T,B> getCellPopulation() {
            return cellPopulation;
        }

        @Override
        public List<SimulationStep<T>> simulate(final Set<Tuple2<Integer,T>> initialStates, final Integer steps) {
            List<SimulationStep<T>> simSteps = new ArrayList<>();
            Set<Tuple2<Integer,T>> temp = initialStates;
            int totSteps = steps;
            while (totSteps > 0) {
                System.out.println(temp);
                //environment (petri_dish noiseFunction)
//                temp = temp.stream().map(x -> noise.apply(x._2())).collect(Collectors.toSet());
//                System.out.println(temp);
//
//                //neighborhood (from the topology)
//                //...TO-DO
//
//                //self-update (noise + dynamics)
//                temp = cellPopulation..stream().map(x -> ).collect(Collectors.toList());
//
//                simSteps.add(new SimulationStepImpl<>(temp));
//                totSteps--;
            }
            return simSteps;
        }
    }

    public static void main(String []a){
        BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> bn =
                BooleanNetworkFactory.newNetworkFromFile("/Users/michelebraccini/IdeaProjects/cellsimulator_v2/src/test/resources/testing/diff_trees/bn");

        CellPopulation<BitSet,Boolean,BinaryState,BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>>> population =
                new CellPopulation.CellPopulationImpl<>(new CellImpl<>(bn, new SynchronousDynamicsImpl(bn),"p", 0));
                /*CellPopulation.CellPopulationConnectionsImpl.<BitSet,Boolean>builder()
                .addConnection(2,3, List.of(new Tuple2<>(3,3), new Tuple2<>(1,2)),new OrTable(2))
                .build();*/

//        new PetriDishImpl<>(
//                StateFunction.<BinaryState>withNoise(1,RandomnessFactory.getPureRandomGenerator()),
//                population).simulate(Set.of(ImmutableBinaryState.valueOf("000000000")),2);
    }
}

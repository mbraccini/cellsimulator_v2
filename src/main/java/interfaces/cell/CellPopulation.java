package interfaces.cell;

import exceptions.SimulatorExceptions;
import interfaces.network.BooleanNetwork;
import interfaces.network.Node;
import interfaces.network.Table;
import interfaces.state.State;
import io.vavr.Tuple2;
import network.OrTable;

import java.util.*;

public interface CellPopulation<K,V,T extends State, B extends BooleanNetwork<? extends Node>> {

    Set<Cell<T,B>> getCells();

    CellPopulationConnections<K,V> cellPopulationConnections();

    class CellPopulationImpl<K,V,T extends State, B extends BooleanNetwork<? extends Node>> implements CellPopulation<K,V,T,B> {
        private Set<Cell<T,B>> cells;
        private CellPopulationConnections<K,V> cellPopulationConnections;
        private Cell<T,B> cell;

        public CellPopulationImpl(Cell<T,B> cell) {
            this.cell = cell;
        }

        public CellPopulationImpl(Set<Cell<T,B>> cells, CellPopulationConnections<K,V> cellPopulationConnections) {
            this.cells = cells;
            this.cellPopulationConnections = cellPopulationConnections;
        }

        @Override
        public Set<Cell<T, B>> getCells() {
            if (Objects.isNull(cells)) {
                return Set.of(cell);
            }
            return cells;
        }

        @Override
        public CellPopulationConnections<K, V> cellPopulationConnections() {
            return cellPopulationConnections;
        }
    }


    interface CellPopulationConnections<K,V>{

        /**
         * [CellID,NodePosition]
         * @return
         */
        Set<Tuple2<Integer,Integer>> connectedNodes();

        /**
         *
         * @param cellId_NodePosition
         * @return
         */
        Table<K,V> getFunction(Tuple2<Integer,Integer> cellId_NodePosition);

        /**
         * List[IncomingCellID, IncomingNodePosition]
         * @return
         */
        List<Tuple2<Integer,Integer>> topology(Tuple2<Integer,Integer> cellId_NodePosition);
    }

    class CellPopulationConnectionsImpl<K,V> implements CellPopulationConnections<K,V> {

        Map<Tuple2<Integer, Integer>, Tuple2<List<Tuple2<Integer, Integer>>,Table<K,V>>> map;
        private CellPopulationConnectionsImpl(Map<Tuple2<Integer, Integer>, Tuple2<List<Tuple2<Integer, Integer>>,Table<K,V>>> map) {
            this.map = map;
            check();
        }

        private void check() {
            for (Tuple2<List<Tuple2<Integer, Integer>>, Table<K, V>> x : map.values() ) {
                if (x._1().size() != x._2().getVariablesNumber()) {
                    throw new SimulatorExceptions.NetworkNodeException.FunctionTopologyMismatch();
                }
            }
        }

        @Override
        public Set<Tuple2<Integer, Integer>> connectedNodes() {
            return map.keySet();
        }

        @Override
        public Table<K, V> getFunction(Tuple2<Integer, Integer> cellId_NodePosition) {
            return map.get(cellId_NodePosition)._2();
        }

        @Override
        public List<Tuple2<Integer, Integer>> topology(Tuple2<Integer, Integer> cellId_NodePosition) {
            return map.get(cellId_NodePosition)._1();
        }

        @Override
        public String toString() {
            return "CellPopulationConnectionsImpl{"
                    + map +
                    '}';
        }

        public static <K,V> CellPopulationConnectionsBuilder1<K,V> builder() {
            return new CellPopulationConnectionsBuilder1<>();
        }


        static class CellPopulationConnectionsBuilder1<K,V> {
            Map<Tuple2<Integer, Integer>, Tuple2<List<Tuple2<Integer, Integer>>,Table<K,V>>> map = new HashMap<>();

            public CellPopulationConnectionsBuilder1<K,V> addConnection(Integer cellID, Integer NodeId, List<Tuple2<Integer,Integer>> connections, Table<K,V> table){
                map.put(new Tuple2<>(cellID,NodeId),new Tuple2<>(connections,table));
                return this;
            }

            public CellPopulationConnectionsImpl<K,V> build(){
                return new CellPopulationConnectionsImpl<>(map);
            }
        }

    }


    public static void main(String []a){
        System.out.print(CellPopulationConnectionsImpl
                .<BitSet,Boolean> builder()
                .addConnection(2,3, List.of(new Tuple2<>(3,3),new Tuple2<>(1,2)),new OrTable(2))
                .build());
    }
}

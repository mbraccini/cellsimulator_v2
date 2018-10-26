//package cell;
//
//import interfaces.cell.Cell;
//import interfaces.cell.LiveCell;
//import interfaces.cell.LiveCellImpl;
//import interfaces.cell.StateFunction;
//import interfaces.dynamic.Dynamics;
//import interfaces.network.BooleanNetwork;
//import interfaces.sequences.UnboundedSequence;
//import interfaces.state.State;
//
//import java.util.function.Function;
//
//public class CellImpl<K,V,T extends State> implements Cell<K,V,T>{
//
//    private final BooleanNetwork<K, V> bn;
//    private final Dynamics<T> dynamics;
//    private final String name;
//
//    public CellImpl (BooleanNetwork<K, V> bn , Dynamics<T> dynamics, String name) {
//        this.bn = bn;
//        this.dynamics = dynamics;
//        this.name = name;
//    }
//
//    @Override
//    public BooleanNetwork<K, V> getBooleanNetwork() {
//        return bn;
//    }
//
//    @Override
//    public Dynamics<T> getDynamics() {
//        return dynamics;
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//
//    @Override
//    public String toString() {
//        return "CellImpl{" +
//                "bn=" + bn +
//                ", dynamics=" + dynamics +
//                ", name='" + name + '\'' +
//                '}';
//    }
//
//
//    @Override
//    public LiveCell<K, V, T> getDefault(T initialState) {
//        return new LiveCellImpl<>(getDynamics(), initialState);
//    }
//
//    @Override
//    public LiveCell<K, V, T> getCustom(StateFunction<T> fun, T initialState) {
//        return new LiveCellImpl<>(fun, initialState);
//    }
//
//
//}

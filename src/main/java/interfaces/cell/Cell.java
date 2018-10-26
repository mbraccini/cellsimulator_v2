//package interfaces.cell;
//
//import interfaces.dynamic.Dynamics;
//import interfaces.network.BooleanNetwork;
//import interfaces.state.State;
//
//import java.util.function.Function;
//
//public interface Cell<K,V, T extends State> extends StateFunction<T>{
//
//    BooleanNetwork<K,V> getBooleanNetwork();
//
//    Dynamics<T> getDynamics(); //le cellule di un tessuto avranno la stessa dinamica, settata dal tessuto!
//
//    String getName();
//
//    @Override
//    default T apply(T t){
//        return getDynamics().apply(t);
//    }
//
//    LiveCell<K, V, T> getDefault(T initialState);
//
//    LiveCell<K, V, T> getCustom(StateFunction<T> fun, T initialState);
//
//
//}

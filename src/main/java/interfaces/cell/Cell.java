package interfaces.cell;

import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BooleanNetwork;
import interfaces.network.Node;
import interfaces.state.State;

public interface Cell<T extends State, B extends BooleanNetwork<? extends Node>>{// extends StateFunction<T>{

    B getBooleanNetwork();

    Dynamics<T> getDynamics(); //le cellule di un tessuto avranno la stessa dinamica, settata dal tessuto!

    String getName();

    Integer id();

    /*@Override
    default T apply(T t){
        return getDynamics().apply(t);
    }

    LiveCell<K, V, T> getDefault(T initialState);

    LiveCell<K, V, T> getCustom(StateFunction<T> fun, T initialState);

*/
}

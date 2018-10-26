//package interfaces.cell;
//
//import interfaces.dynamic.Dynamics;
//import interfaces.state.State;
//
//import java.util.List;
//
//public interface Tissue<K,V,T extends State> {
//
//    List<Cell<K,V,T>> getCells();
//
//    Dynamics<T> getDynamics();  //una sola dinamica perché le cellule avranno tutte la stessa dinamica all'interno di un tessuto!
//
//    T nextState();
//
//    String getName();
//
//}
//

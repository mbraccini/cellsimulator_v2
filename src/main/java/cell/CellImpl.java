package cell;

import dynamic.SynchronousDynamicsImpl;
import utility.RandomnessFactory;
import interfaces.cell.Cell;
import interfaces.dynamic.Dynamics;
import interfaces.network.*;
import interfaces.state.BinaryState;
import interfaces.state.State;
import network.BooleanNetworkFactory;

import java.util.BitSet;

public class CellImpl<T extends State, B extends BooleanNetwork<? extends Node>> implements Cell<T,B>{

    private final B bn;
    private final Dynamics<T> dynamics;
    private final String name;
    private final Integer id;

    public CellImpl (B bn, Dynamics<T> dynamics, String name, Integer id) {
        this.bn = bn;
        this.dynamics = dynamics;
        this.name = name;
        this.id = id;
    }


    @Override
    public B getBooleanNetwork(){
        return bn;
    }

    @Override
    public Dynamics<T> getDynamics(){
        return dynamics;
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public Integer id(){
        return id;
    }



    @Override
    public String toString() {
        return "CellImpl{" +
                "bn=" + bn +
                ", dynamics=" + dynamics +
                ", id=" + id+
                ", name='" + name + '\'' +
                '}';
    }

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


    public static void main(String []a){
        BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL,BooleanNetworkFactory.SelfLoop.WITHOUT,3,2,0.5,RandomnessFactory.getPureRandomGenerator());
        Cell<BinaryState, BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>>> cell = new CellImpl<>(bn, new SynchronousDynamicsImpl(bn),"First",1);


    }

}

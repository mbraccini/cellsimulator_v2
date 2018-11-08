package dynamic;

import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.state.BinaryState;
import io.vavr.Tuple2;
import states.ImmutableBinaryState;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class FrozenNodesDynamicsDecorator extends AbstractDynamicsDecorator<BinaryState> implements DecoratingDynamics<BinaryState> {

    private final List<Integer> nodesIndicesToFreeze;
    private final List<Tuple2<Integer,Boolean>> nodesToFreeze = new ArrayList<>();


    public FrozenNodesDynamicsDecorator(Dynamics<BinaryState> dynamics, List<Integer> nodesIndicesToFreeze){
        super(dynamics);
        this.nodesIndicesToFreeze = nodesIndicesToFreeze;
    }

    private BinaryState frozen(BinaryState state){
        BitSet bitsetRep = state.toBitSet();
        int numNodes = state.getLength();

        for (Tuple2<Integer,Boolean> idxVal: nodesToFreeze) {
            bitsetRep.set(idxVal._1(),idxVal._2());
        }
        return new ImmutableBinaryState(numNodes,bitsetRep);
    }


    @Override
    public BinaryState nextState(BinaryState state) {
        for (Integer idx : nodesIndicesToFreeze){
            nodesToFreeze.add(new Tuple2<>(idx, state.getNodeValue(idx)));
        }
        return frozen(dynamics.apply(state));
    }
}

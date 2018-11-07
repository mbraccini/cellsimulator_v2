package dynamic;

import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.state.BinaryState;
import io.vavr.Tuple2;
import states.ImmutableBinaryState;

import java.util.BitSet;
import java.util.List;

public class FrozenNodesDynamicsDecorator extends AbstractDynamicsDecorator<BinaryState> implements DecoratingDynamics<BinaryState> {

    private final List<Tuple2<Integer,Boolean>> nodesToFreeze;

    public FrozenNodesDynamicsDecorator(Dynamics<BinaryState> dynamics, List<Tuple2<Integer,Boolean>> nodesToFreeze){
        super(dynamics);
        this.nodesToFreeze = nodesToFreeze;
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
        return frozen(dynamics.apply(state));
    }
}

package dynamic;

import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.state.BinaryState;
import io.vavr.Tuple2;
import states.ImmutableBinaryState;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * The states passed to this nextState method must be initially set to zero in the indices specified ion the constructor.
 */
public class KnockOutDynamicsDecorator extends AbstractDynamicsDecorator<BinaryState> implements DecoratingDynamics<BinaryState> {

    private final List<Integer> nodesIndicesToKnockOut;

    public KnockOutDynamicsDecorator(Dynamics<BinaryState> dynamics, List<Integer> nodesIndicesToKnockOut){
        super(dynamics);
        this.nodesIndicesToKnockOut = nodesIndicesToKnockOut;
    }

    private BinaryState knockOut(BinaryState state){
        BitSet bitsetRep = state.toBitSet();
        int numNodes = state.getLength();

        for (Integer idx: nodesIndicesToKnockOut) {
            bitsetRep.set(idx, Boolean.FALSE);
        }
        return new ImmutableBinaryState(numNodes, bitsetRep);
    }


    @Override
    public BinaryState nextState(BinaryState state) {
        for (Integer idx: nodesIndicesToKnockOut) {
            if (state.getNodeValue(idx) != Boolean.FALSE) {
                System.err.println("WARNING: the passed state is not FALSE in the specified indices!");
            }
        }
        return knockOut(dynamics.apply(state));
    }
}
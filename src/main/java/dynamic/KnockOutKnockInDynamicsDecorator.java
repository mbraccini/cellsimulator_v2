package dynamic;

import exceptions.DynamicsException;
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
public class KnockOutKnockInDynamicsDecorator extends AbstractDynamicsDecorator<BinaryState> implements DecoratingDynamics<BinaryState> {

    private final List<Integer> nodesIndicesToKnockOut;
    private final List<Integer> nodesIndicesToKnockIn;

    public KnockOutKnockInDynamicsDecorator(Dynamics<BinaryState> dynamics, List<Integer> nodesIndicesToKnockOut, List<Integer> nodesIndicesToKnockIn){
        super(dynamics);
        this.nodesIndicesToKnockOut = nodesIndicesToKnockOut;
        this.nodesIndicesToKnockIn = nodesIndicesToKnockIn;

    }

    private BinaryState knockOut(BinaryState state){
        BitSet bitsetRep = state.toBitSet();
        int numNodes = state.getLength();

        for (Integer idx: nodesIndicesToKnockOut) {
            bitsetRep.set(idx, Boolean.FALSE);
        }
        for (Integer idx: nodesIndicesToKnockIn) {
            bitsetRep.set(idx, Boolean.TRUE);
        }
        return new ImmutableBinaryState(numNodes, bitsetRep);
    }


    @Override
    public BinaryState nextState(BinaryState state) {
        for (Integer idx: nodesIndicesToKnockOut) {
            if (state.getNodeValue(idx) != Boolean.FALSE) {
                System.err.println("ERROR: the passed state is not FALSE in the specified indices!");
                throw new DynamicsException.KnockOutKnockInDynamics.InitialNodeValueNotSet();
            }
        }
        for (Integer idx: nodesIndicesToKnockIn) {
            if (state.getNodeValue(idx) != Boolean.TRUE) {
                System.err.println("ERROR: the passed state is not TRUE in the specified indices!");
                throw new DynamicsException.KnockOutKnockInDynamics.InitialNodeValueNotSet();
            }
        }
        return knockOut(dynamics.apply(state));
    }
}
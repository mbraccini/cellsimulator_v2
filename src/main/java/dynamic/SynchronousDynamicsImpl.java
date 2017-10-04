package dynamic;

import interfaces.dynamic.SynchronousDynamics;
import interfaces.network.BooleanNetwork;
import interfaces.state.BinaryState;
import states.ImmutableBinaryState;

import java.util.BitSet;

public class SynchronousDynamicsImpl extends AbstractDynamics<BitSet, Boolean> implements SynchronousDynamics<BinaryState> {

    public SynchronousDynamicsImpl(BooleanNetwork<BitSet, Boolean> bn) {
        super(bn);
    }

    @Override
    public BinaryState nextState(BinaryState initialState) {
        BitSet nextState = new BitSet(this.nodesNumber);
        for (int nodeInExam = 0; nodeInExam < nodesNumber; nodeInExam++) {
            int[] incomingNodes = incomingNodesMatrix[nodeInExam]; //incomingNodes of the node in exam
            BitSet nodePreviousState = new BitSet(incomingNodesMatrix[nodeInExam].length);
            for (int j = 0; j < incomingNodes.length; j++) {
                nodePreviousState.set(j, initialState.getNodeValue(incomingNodes[j]));
            }
            nextState.set(nodeInExam, this.functionsList.get(nodeInExam).getRowByInput(nodePreviousState).getOutput());
        }
        return new ImmutableBinaryState(this.nodesNumber, nextState);
    }

}

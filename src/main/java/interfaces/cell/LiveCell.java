package interfaces.cell;

import interfaces.sequences.UnboundedSequence;
import interfaces.state.State;

public interface LiveCell<K,V, T extends State> extends UnboundedSequence<T>{

}

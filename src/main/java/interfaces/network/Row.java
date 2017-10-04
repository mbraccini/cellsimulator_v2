package interfaces.network;

/**
 * A row represents a specific combination of the variables of the truth table
 * @author michelebraccini
 *
 */
public interface Row<K,V> { //OK
	
	/* Immutable */
	
	K getInput();
		
	V getOutput();
		
	/* Mutable */
	
	void setInput(K value); // e.g. from "001" to "100"
	
	void setOutput(V value);
		
}

package interfaces.state;

public interface State extends Comparable<State>,Immutable {
	

	/**
	 * Returns a string representation of the state.
	 * @return
	 */
	String getStringRepresentation();


	/**
	 * Returns a copy of this object.
	 * @return
	 */
	//State getCopy();


	/**
	 * Gives the number of nodes that it represents.
	 * @return
	 */
	//Integer getLength();


}

package interfaces.state;

public interface State extends Comparable<State>{
	

	/**
	 * Returns a string representation of the state.
	 * @return
	 */
	String getStringRepresentation();


	/**
	 * Gives the number of nodes that it represents.
	 * @return
	 */
	Integer getLength();

	/**
	 * Returns a copy of this object.
	 * @return
	 */
	//State getCopy();





}

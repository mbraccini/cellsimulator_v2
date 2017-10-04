package interfaces.state;

import java.util.BitSet;

public interface BinaryState extends State {

	/**
	 * Returns the value of the node.
	 * @param index
	 * @return
	 */
	Boolean getNodeValue(Integer index);

	/**
	 * Returns the BitSetRepresentation of this object.
	 * @return
	 */
	BitSet toBitSet();

	/**
	 * Returns a new BinaryState with the specified node flipped.
	 * @param index1
	 * @return
	 */
	BinaryState flipNodesValues(Integer index1);

	/**
	 * 	Returns a new BinaryState with the specified nodes flipped.
	 * @param indices
	 * @return
	 */
	BinaryState flipNodesValues(Integer... indices);


	/**
	 * Returns a new BinaryState with the specified nodes with value 1.
	 * @param indices
	 * @return
	 */
	//BinaryState setNodesValues(Integer... indices);



	/**
	 * Logical negation of the node.
	 * @param index
	 */
	//void flipNodeValue(Integer index);
	
	/**
	 * Sets to 0 all bits.
	 */
	//void clear();
	
	//gli altri operatori ANd, or ... solo più avanti se necessari
	/**
	 * Sets the value of the node.
	 * @param value
	 */
	//void setNodeValue(T value);
}

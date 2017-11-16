package interfaces.tes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DifferentiationTree<E> {

	/**
	 * Nodes which not have a father.
	 * @return list of root nodes
	 */
	List<DifferentiationNode<E>> getRootLevel();

	/**
	 * Ordered thresholds used to construct the differentiation tree.
	 * @return thresholds
	 */
	List<Double> getThresholds();

	/**
	 * If present, it returns the name of the Differentiation Tree (eg. Hematopoietic Tree)
	 * @return an Optional describing the name of this tree, or an empty Optional if it does not have a name.
	 */
	Optional<String> getName(); // e.g. Hematopoietic Tree

	/**
	 * Sets the name of this differentiation tree.
	 * @param name
	 */
	void setName(String name); 	/* The name of a TES might not be known at the time of its creation */
	
	
	/**
	 * Returns the number of levels;
	 * @return
	 */
	Integer getLevelsNumber();
	
	/**
	 * Returns the level required, if it exists.
	 * @param level
	 * @return
	 */
	Optional<Set<DifferentiationNode<E>>> getLevel(Integer level);

	/**
	 * Gets a tree like String representation;
	 * @return
	 */
	public String getTreeRepresentation();

}

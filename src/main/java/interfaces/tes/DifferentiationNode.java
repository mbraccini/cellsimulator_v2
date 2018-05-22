package interfaces.tes;

import java.util.List;

/**
 * It's a graph, not a tree!
 * @author michelebraccini
 *
 * @param <E>
 */
public interface DifferentiationNode<E>{

	/**
	 * Since it's not a single graph but a forest!!!
	 * @return
	 */
	Integer getLevel();

	/**
	 * Branching factor of the node: the number of its children.
	 * Note: the last level hasn't got any children!!!
	 * @return
	 */
	default Integer branchingFactor(){
		return getChildren().size();
	}
	
	void setLevel(Integer level);

	List<DifferentiationNode<E>> getParents();

	List<DifferentiationNode<E>> getChildren();

	void addParent(DifferentiationNode<E> parent);

	void addChild(DifferentiationNode<E> child);

	/**
	 * Retrieves the wrapped element.
	 * @return
	 */
	E getWrappedElement();

	public String getTreeLikeRepresentation(String prefix, boolean isTail);
}

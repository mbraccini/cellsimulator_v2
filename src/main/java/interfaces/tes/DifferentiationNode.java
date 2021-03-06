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

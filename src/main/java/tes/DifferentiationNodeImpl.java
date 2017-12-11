package tes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import interfaces.tes.DifferentiationNode;

public class DifferentiationNodeImpl<E> implements DifferentiationNode<E> {

	protected E object;
	
	protected List<DifferentiationNode<E>> parents;
	protected List<DifferentiationNode<E>> children;
	protected Integer level;
	

	public DifferentiationNodeImpl(E object) {
		this.object = object;
	}

	
	public void setLevel(Integer level){
		this.level = level;
	}
	
	public Integer getLevel(){
		return this.level;
	}

	@Override
	public List<DifferentiationNode<E>> getParents() {
		return this.parents;
	}

	@Override
	public List<DifferentiationNode<E>> getChildren() {
		return this.children;
	}

	@Override
	public void addParent(DifferentiationNode<E> parent) {
		if(this.parents == null){
			this.parents = new ArrayList<>();
		}
		this.parents.add(parent);
	}

	@Override
	public void addChild(DifferentiationNode<E> child) {
		if(this.children == null){
			this.children = new ArrayList<>();
		}
		this.children.add(child);
	}

	@Override
	public E getWrappedElement() {
		return object;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DifferentiationNodeImpl<?> that = (DifferentiationNodeImpl<?>) o;
		return Objects.equals(level, that.level) && Objects.equals(object, that.object);
	}

	@Override
	public int hashCode() {
		return Objects.hash(object, level);
	}

	@Override
	public String toString() {
		if (this.parents == null) { //root
			return "RootDifferentiationNode, lvl:" + this.level +" ["+ this.object.toString()+"]";//+ (this.children != null ? this.children.stream().map(x->x.toString()) : "");
		}

		return "DifferentiationNode, lvl:" + this.level +" ["+ this.object.toString()+"]";//+  (this.children != null ? this.children.stream().map(x->x.toString()) : "");
	}


	public String getTreeLikeRepresentation(String prefix, boolean isTail) {
		StringBuilder sb = new StringBuilder();

		if (Objects.isNull(parents)) { //root
			sb.append((prefix + (isTail ? "o── " : "├── ") + "R:" + object.toString()) + ", lvl=" + level + "\n");
		} else {
			sb.append((prefix + (isTail ? "└── " : "├── ") + object.toString()) + ", lvl=" + level + "\n");
		}
		if (Objects.nonNull(children)) {
			for (int i = 0; i < children.size() - 1; i++) {
				sb.append(children.get(i).getTreeLikeRepresentation(prefix + (isTail ? "    " : "│   "), false));
			}
			if (children.size() > 0) {
				sb.append(children.get(children.size() - 1)
						.getTreeLikeRepresentation(prefix + (isTail ? "    " : "│   "), true));
			}
		}
		return sb.toString();
	}

}

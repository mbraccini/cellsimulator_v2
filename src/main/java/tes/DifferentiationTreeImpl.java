package tes;

import interfaces.tes.DifferentiationNode;
import interfaces.tes.DifferentiationTree;
import utility.GenericUtility;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;


public class DifferentiationTreeImpl<E> implements DifferentiationTree<E> {

	protected String name;
	protected List<DifferentiationNode<E>> rootLevel;
	protected List<Double> thresholds;
	protected int levelsNumber;

	public DifferentiationTreeImpl(List<DifferentiationNode<E>> rootLevel, List<Double> thresholds){
		this.rootLevel = rootLevel;
		this.thresholds = thresholds;
	}

	@Override
	public List<DifferentiationNode<E>> getRootLevel() {
		return Collections.unmodifiableList(this.rootLevel);
	}

	@Override
	public Optional<String> getName() {
		return Optional.ofNullable(name);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "DifferentiationTreeImpl [" + (name != null ? "name=" + name : "") + ", root=" + rootLevel + "]";
	}

	@Override
	public List<Double> getThresholds() {
		return this.thresholds;
	}

	@Override
	public Optional<Set<DifferentiationNode<E>>> getLevel(Integer level) {
		if (levels.get(level) != null) {
			return Optional.of(levels.get(level));
		} else {
			Set<DifferentiationNode<E>> levelNodes = new HashSet<>();
			levelProvider(level, rootLevel, levelNodes);
			if (levelNodes.isEmpty()) {
				return Optional.empty();
			} else {
				levels.put(level, levelNodes);
				return Optional.of(levelNodes);
			}
		}
	}


	Map<Integer, Set<DifferentiationNode<E>>> levels = new HashMap<>();
	private void levelProvider(Integer level, List<DifferentiationNode<E>> nodes, Set<DifferentiationNode<E>> tempChildrenLevel){
		for (Iterator<DifferentiationNode<E>> i = GenericUtility.safeClient(nodes).iterator(); i.hasNext();) {
			DifferentiationNode<E> node = i.next();
			if (node.getLevel().intValue() == level.intValue()) {
				tempChildrenLevel.add(node);
			}
			levelProvider(level, node.getChildren(), tempChildrenLevel);
		}
	}

	private boolean levelsNumberAlreadyComputed = false;
	@Override
	public Integer getLevelsNumber() {
		if (!levelsNumberAlreadyComputed) {
			countingLevels();
		}
		return this.levelsNumber + 1; // We must remember to count the root as a level
	}


	private void countingLevels() {
		int tempLevel;
		for (Iterator<DifferentiationNode<E>> i = GenericUtility.safeClient(rootLevel).iterator(); i.hasNext();) {
			DifferentiationNode<E> node = i.next();
			tempLevel = DepthSearchForComputeLevel(node);
			if (tempLevel > levelsNumber) {
				levelsNumber = tempLevel;
			}
		}
		levelsNumberAlreadyComputed = true;
	}

	Set<DifferentiationNode<E>> alreadyVisited = new HashSet<>();
	private int DepthSearchForComputeLevel(DifferentiationNode<E> node) {
		if (alreadyVisited.contains(node)) {
			return node.getLevel(); // non importa, qualunque valore torniamo non influisce sul risultato massimo che si troverà
		}
		alreadyVisited.add(node);
		int levelMax = node.getLevel();
		int tempLevel;
		for (Iterator<DifferentiationNode<E>> i = GenericUtility.safeClient(node.getChildren()).iterator(); i.hasNext();) {
			DifferentiationNode<E> child = i.next();
			tempLevel = DepthSearchForComputeLevel(child);
			if (tempLevel > levelMax) {
				levelMax = tempLevel;
			}
		}
		return levelMax;
	}


	@Override
	public String getTreeRepresentation() {
		StringBuilder sb = new StringBuilder();

		for (Iterator<DifferentiationNode<E>> i = GenericUtility.safeClient(rootLevel).iterator(); i.hasNext();) {
			DifferentiationNode<E> node = i.next();
			sb.append(node.getTreeLikeRepresentation(generatePrefix(node.getLevel()), true));
			sb.append("\n");
		}
		return sb.toString();
	}

	private String generatePrefix(int level) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < level; i++) {
			sb.append("    ");
		}
		return sb.toString();
	}

}

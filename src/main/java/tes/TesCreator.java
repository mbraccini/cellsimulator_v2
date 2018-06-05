package tes;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import interfaces.attractor.ImmutableAttractor;
import interfaces.state.State;
import interfaces.tes.*;
import io.vavr.Function0;
import org.apache.commons.math3.random.RandomGenerator;

public class TesCreator<T extends State> implements Function0<TESDifferentiationTree<T, Tes<T>>> {

	protected final int NAME_LENGTH = 1;
	protected Atm<T> atm;
	protected Double[][] atmCopy;
	protected List<Double> thresholds;
	protected List<ImmutableAttractor<T>> attractorsList;
	protected Set<String> nameAlreadyUsed = new HashSet<>();
	private Map<Double, Set<List<Integer>>> chachedThresholdTes;
	public TesCreator(Atm<T> atm, List<Double> thresholds,RandomGenerator random) {
		this(atm, random);
		this.atmCopy = this.atm.getMatrixCopy();
		this.thresholds = thresholds;
		// soglie in ordine crescente
		this.thresholds = new ArrayList<>(thresholds);
		if (!this.thresholds.contains(0.0)) {
			this.thresholds.add(0, 0.0); //aggiungo soglia 0.0
		}
		Collections.sort(this.thresholds);
	}

	public TesCreator(Atm<T> atm,RandomGenerator random) {
		this.atm = atm;
		this.attractorsList = this.atm.getAttractors().getAttractors();
		this.nameGenerator = new UniqueNameGenerator(random);
	}

	private UniqueNameGenerator nameGenerator;

	public TESDifferentiationTree<T, Tes<T>> apply() {
		if (this.thresholds == null) {
			searchCorrectThresholds();
		}

		Set<List<Integer>> setTes;

		List<DifferentiationNode<Tes<T>>> previousLevelDifferentiationNodesList = null;

		List<DifferentiationNode<Tes<T>>> DifferentiationNodesList;

		List<DifferentiationNode<Tes<T>>> rootNodesLevel = null;
		DifferentiationNode<Tes<T>> node = null;
		int level = 0;

		for (double threshold : this.thresholds) {
			DifferentiationNodesList = new ArrayList<>();

			if (this.chachedThresholdTes == null) {
				removeValueLowerOrEqualThan(this.atmCopy, threshold);
				setTes = new TesFinderAlgorithm(atmCopy).call();
			} else {
				setTes = this.chachedThresholdTes.get(threshold);
			}

			//per il set
			for (List<Integer> entry : setTes) {
				Tes<T> tes = new TesImpl<>(entry.stream().map(x -> this.attractorsList.get(x)).collect(Collectors.toList()));
				node = new DifferentiationNodeImpl<>(tes);
				node.setLevel(level); //livello
				DifferentiationNodesList.add(node);
				if (previousLevelDifferentiationNodesList == null) { //siamo al livello 0
					tes.setName(generateNameNotAlreadyUsed()); //diamo un nome ad ogni tes
				}
			}

			if (previousLevelDifferentiationNodesList == null) { //costruzione radice (soglia = 0.0)
				//System.out.println("#########_NumDiTesTrovatiComeRoot: " + DifferentiationNodesList.size());
				rootNodesLevel = DifferentiationNodesList;
			} else {

				for (DifferentiationNode<Tes<T>> DifferentiationNode : DifferentiationNodesList) {
					List<DifferentiationNode<Tes<T>>> parentsDifferentiationNode = this.isChildOf(DifferentiationNode, previousLevelDifferentiationNodesList);
					//System.out.println("parentsDifferentiationNode: " + parentsDifferentiationNode);

					if (parentsDifferentiationNode.size() != 0) {
						for (DifferentiationNode<Tes<T>> parentDifferentiationNode: this.isChildOf(DifferentiationNode, previousLevelDifferentiationNodesList)) {
							DifferentiationNode.addParent(parentDifferentiationNode);
							parentDifferentiationNode.addChild(DifferentiationNode);
							if (DifferentiationNode.getWrappedElement().getTesAttractors().equals(parentDifferentiationNode.getWrappedElement().getTesAttractors())) {
								//se gli attrattori sono gli stessi vuol dire che è lo stesso TES
								// allora gli do lo stesso nome
								DifferentiationNode.getWrappedElement().setName(parentDifferentiationNode.getWrappedElement().getName().get());
							} else {
								DifferentiationNode.getWrappedElement().setName(generateNameNotAlreadyUsed());
							}
						}
					} else {
						//caso in cui si origina un nuovo TES che non ha padri!!!
						rootNodesLevel.add(DifferentiationNode);
						DifferentiationNode.getWrappedElement().setName(generateNameNotAlreadyUsed());
						//System.out.println("é una root: " + DifferentiationNode);

					}


				}

			}
			previousLevelDifferentiationNodesList = DifferentiationNodesList;

			//System.out.println("livello: " + level);
			//DifferentiationNodesList.stream().forEach(System.out::println);
			level++;
		}

		return new TESDifferentiationTreeImpl<>(rootNodesLevel, thresholds);

	}

	private void searchCorrectThresholds() {
		this.chachedThresholdTes = new HashMap<>();
		Stream<Double[]> atm_stream = Arrays.stream(atm.getMatrix());
		Stream<Double> atm_stream_flat = atm_stream.flatMap(x -> Arrays.stream(x));
		List<Double> atmFlattenedAndSorted = atm_stream_flat.distinct().sorted(Double::compare).collect(Collectors.toList());

		List<Double> thresholdsFound = new ArrayList<>();
		thresholdsFound.add(0.0);

		Double[][] atmOtherCopy = this.atm.getMatrixCopy();
		Set<List<Integer>> previousTes = new TesFinderAlgorithm(atmOtherCopy).call();
		this.chachedThresholdTes.put(0.0, previousTes);
		Set<List<Integer>> currentTes;
		for (Double value : atmFlattenedAndSorted) {
			removeValueLowerOrEqualThan(atmOtherCopy, value);
			currentTes = new TesFinderAlgorithm(atmOtherCopy).call();
			if (!currentTes.equals(previousTes)) {
				thresholdsFound.add(value);
				this.chachedThresholdTes.put(value, currentTes);
			}
			previousTes = currentTes;
		}
		this.thresholds = thresholdsFound;
	}

	// mi da la lista dei padri di questo nodo
	private List<DifferentiationNode<Tes<T>>> isChildOf(DifferentiationNode<Tes<T>> tes, List<DifferentiationNode<Tes<T>>> parentLevel) {
		List<DifferentiationNode<Tes<T>>> listOfParents = new ArrayList<>();
		for (DifferentiationNode<Tes<T>> parentDifferentiationNode: parentLevel) {
			for (ImmutableAttractor<T> attractor : tes.getWrappedElement().getTesAttractors()) {
				if (parentDifferentiationNode.getWrappedElement().getTesAttractors().stream().anyMatch(x -> x.equals(attractor))) {
					if (listOfParents.contains(parentDifferentiationNode) == false) {
						listOfParents.add(parentDifferentiationNode);
					}
				}
			}
		}
		return listOfParents;
	}


	private static void removeValueLowerOrEqualThan(Double[][] atmCopy, double threshold) {
		for (int i = 0; i < atmCopy.length; i++) {
			for (int j = 0; j < atmCopy.length; j++) {
				if (atmCopy[i][j] <= threshold) {
					atmCopy[i][j] = 0.0;
				}
			}
		}
	}

	private String name;
	private String generateNameNotAlreadyUsed() {
		do {
			name = nameGenerator.randomAlphabeticString(NAME_LENGTH);
		} while (nameAlreadyUsed.stream().anyMatch(x -> x.equals(name)));
		nameAlreadyUsed.add(name);
		return name;
	}


	private class UniqueNameGenerator {

		static final String Az_09 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		static final String Az = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		private RandomGenerator rnd;
		public UniqueNameGenerator(RandomGenerator rnd) {
			this.rnd = rnd;
		}

		public String randomAlphanumericString( int len ){
			StringBuilder sb = new StringBuilder( len );
			for( int i = 0; i < len; i++ )
				sb.append( Az_09.charAt( rnd.nextInt(Az_09.length()) ) );
			return sb.toString();
		}

		public String randomAlphabeticString( int len ){
			StringBuilder sb = new StringBuilder( len );
			for( int i = 0; i < len; i++ )
				sb.append( Az.charAt( rnd.nextInt(Az.length()) ) );
			return sb.toString();
		}
	}

	/*@SuppressWarnings("unchecked")
	public static <T> void main(String[] args) {

		Double[][] adjacencyMatrix2 = new Double[][] {
			// 0	1	  2
			{0.2, 0.8, 0.0},

			{0.8, 0.2, 0.0},

			{0.4, 0.0, 0.6}

		};

		List<ImmutableAttractor<T>> att = (List<ImmutableAttractor<T>>) UtilityFiles.deserializeObject("att_ser.ser");
		List<ImmutableAttractor<T>> att2 = new ArrayList<>();
		att2.add(att.get(0));
		att2.add(att.get(1));
		att2.add(att.get(2));

		Atm<T> atm = AtmImpl.newAtm(att2, adjacencyMatrix2);
		DifferentiationTreeGraphViz<T> diffTree = new DifferentiationTreeGraphViz<>(new TesCreator<>(atm, new ArrayList<>(Arrays.asList(0.2, 0.4 ,0.8))).call());
		diffTree.generateFile("ah.gv");
		
	}*/

}

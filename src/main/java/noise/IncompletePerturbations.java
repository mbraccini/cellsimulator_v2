package noise;

import attractor.AttractorsUtility;
import exceptions.EmptyAttractorsList;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import io.vavr.Function5;
import io.vavr.Function6;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import tes.AtmImpl;
import utility.GenericUtility;
import utility.MatrixUtility;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class permits to instantiate objects to performs the incomplete version of the perturbations; this class
 * cannot derive from the complete version, or vice versa, because this implies stochasticity whilst the complete version
 * is deterministic and follows the definition of theoretical construction of ATM
 * (see "A Dynamical Model of Genetic Networks for Cell Differentiation" Villani, M. Barbieri, A. Serra, R.)
 * @author michelebraccini
 *
 */
public class IncompletePerturbations  {
	
	
	// gli attrattori li guardiamo tutti -> 100%
	// contatore per i tentativi falliti perché potremmo non farcela a prenderli tutti
	// anche la possibilità di inisistere fino ad un determinato numero di tentativi falliti!
	
	public Atm<BinaryState> apply(Attractors<BinaryState> attractors,
								  Dynamics<BinaryState> dynamics,
								  Integer percentageStatesToPerturb,
								  Integer percentageNodesToPerturb,
								  Integer cutoff,RandomGenerator pseudoRandomInstance){
		if (attractors.numberOfAttractors() == 0) {
			throw new EmptyAttractorsList();
		}
		List<ImmutableAttractor<BinaryState>> attractorsList = attractors.getAttractors();


		int[][] atm = new int[attractorsList.size()][attractorsList.size()];
		int nodesNumber = attractorsList.get(0).getFirstState().getLength(); //At least one attractor must exist in a network
		int nodesNumberToPerturb = (int) (Math.ceil((nodesNumber * percentageNodesToPerturb) / 100.0));
		int perturbationsLost = 0;
		//
		int addedIterationsNodes = 0;
		int addedIterationsStates = 0;  //numero di iterazioni aggiunte man mano che le perturbazioni vengono perse

		List<BinaryState> statesInThisAttractor;
		BinaryState stateInExam;
		BinaryState flippedState;
		//Per un sottoinsieme di tutti gli attrattori
		
		for(int attractorIndex = 0; attractorIndex < attractorsList.size(); attractorIndex++){
			statesInThisAttractor = attractorsList.get(attractorIndex).getStates();
			int statesNumber = statesInThisAttractor.size(); 	//numero di stati in questo attrattore!
			/*
			 * Here we calculate the number of the states to perturb
			 */
			int statesNumberToPerturb = (int) (Math.ceil((statesNumber * percentageStatesToPerturb) / 100.0));
			//
			
			//Per un sottoinsieme di stati
			int stateIndex;
			for(int statIn = 0; statIn < statesNumberToPerturb + addedIterationsStates; statIn++){
				stateIndex = pseudoRandomInstance.nextInt(statesNumber);
				stateInExam = statesInThisAttractor.get(stateIndex);
				//Per ogni nodo (bit), applichiamo un flip di un time step!
				int nodeIndex;
				for(int nodIn = 0; nodIn < nodesNumberToPerturb + addedIterationsNodes; nodIn++){
					nodeIndex = pseudoRandomInstance.nextInt(nodesNumber);
					//flip
					flippedState = stateInExam.flipNodesValues(nodeIndex);


					//...dynamics
					int iterations = cutoff; // limite di passi consentiti per ogni perturbazione prima di dire che c'è stata una perturbazione persa
					while(iterations > 0){
						iterations--;
						int otherAttractorIndex = AttractorsUtility.retrieveAttractorListIndex(flippedState, attractorsList);
						if(otherAttractorIndex != -1){
							atm[attractorIndex][otherAttractorIndex] = atm[attractorIndex][otherAttractorIndex] + 1;
							break;
						}
						flippedState = dynamics.nextState(flippedState);
					}
					if(iterations == 0){ //non si è raggiunto nessun attrattore conosciuto (precedentemente trovato)
						if((nodesNumberToPerturb + addedIterationsNodes) < nodesNumber ){
							//aggiungiamo una perturbazione ai nodi
							addedIterationsNodes++;
						} else {
							if((statesNumberToPerturb + addedIterationsStates) < statesNumber ){
								addedIterationsStates++;
							} else{
								perturbationsLost++;
							}
						}
					}

				}
			}
		}
		//System.out.println("Perturbations LOST: " + perturbationsLost);
		return new AtmImpl<>(atm, attractors,perturbationsLost);
	}

	/**
	 *
	 * @param attractors
	 * @param dynamics
	 * @param numberOfPerturbations
	 * @param cutoff
	 * @param pseudoRandomInstance
	 * @param frozenNodesIndices
	 * @return
	 */
	public Atm<BinaryState> apply(Attractors<BinaryState> attractors,
								  Dynamics<BinaryState> dynamics,
								  Integer numberOfPerturbations,
								  Integer cutoff,
								  RandomGenerator pseudoRandomInstance,
								  List<Integer> frozenNodesIndices) {
		if (attractors.numberOfAttractors() == 0) {
			throw new EmptyAttractorsList();
		}
		List<ImmutableAttractor<BinaryState>> attractorsList = attractors.getAttractors();
		int numOfAttractors = attractorsList.size();

		int[][] atm = new int[attractorsList.size()][attractorsList.size()];
		int nodesNumber = attractorsList.get(0).getFirstState().getLength(); //At least one attractor must exist in a network
		//int nodesNumberToPerturb = (int) (Math.ceil((nodesNumber * percentageNodesToPerturb) / 100.0));
		int perturbationsLost = 0;
		//
		int addedIterationsNodes = 0;
		int addedIterationsStates = 0;  //numero di iterazioni aggiunte man mano che le perturbazioni vengono perse

		List<BinaryState> statesInThisAttractor;
		BinaryState stateInExam;
		BinaryState flippedState;
		//Per un sottoinsieme di tutti gli attrattori

		int limitOfAttemptsForAddedIterations = attractorsList.stream().mapToInt(x -> x.getLength()).sum() * nodesNumber;
		int addedIterations = 0;

		//Set Difference to retrieve the perturbable node indices
		Set<Integer> allNodes = IntStream.range(0, nodesNumber).boxed().collect(Collectors.toSet());
		allNodes.removeAll(frozenNodesIndices);//actual set difference operation, which modifies the set previously specified.
		List<Integer> perturbableNodes = new ArrayList<>(allNodes);
		int perturbableNodesNumber = perturbableNodes.size();
		//System.out.println("frozenNodesIndices: " + frozenNodesIndices);
		//System.out.println("perturbableNodes: " + perturbableNodes);

		/* ripetiamo la lista di indici degli attrattori tante volte quante ce ne stanno nel numero di perturbazioni richieste,
		   e poi permutiamo questa lista.
		   In modo tale da avere, sempre che il numero di perturbazioni sia almeno pari al numero di attrattori, una politica
		   di perturbazioni random che permetta di avere almeno una perturbazione per attrattore.
		 */
		List<Integer> attractorsIndicesPermutations = new ArrayList<>();
		int repeatitions = 0;
		while (repeatitions <  ((numberOfPerturbations + limitOfAttemptsForAddedIterations) / numOfAttractors) + 1){
			attractorsIndicesPermutations.addAll(IntStream.range(0,numOfAttractors).boxed().collect(Collectors.toList()));
			repeatitions ++;
		}
		Collections.shuffle(attractorsIndicesPermutations, new RandomAdaptor(pseudoRandomInstance));
		Iterator<Integer> iteratorAttractorsIndicesPermutations = attractorsIndicesPermutations.iterator();
		//System.out.println("attractorsIndicesPermutations " + attractorsIndicesPermutations);
		for (int currentPerturbation = 0; currentPerturbation < numberOfPerturbations + addedIterations; currentPerturbation++) {
			int attractorIndex = iteratorAttractorsIndicesPermutations.next();//pseudoRandomInstance.nextInt(numOfAttractors);
			//System.out.println("attractorIndex " + attractorIndex);

			statesInThisAttractor = attractorsList.get(attractorIndex).getStates(); //scegliamo un attrattore a caso
			int statesNumber = statesInThisAttractor.size();    //numero di stati in questo attrattore!
			int stateIndex = pseudoRandomInstance.nextInt(statesNumber); //scelgo lo fase a caso (all'interno dell'attrattore)
			stateInExam = statesInThisAttractor.get(stateIndex);
			int nodeIndex = perturbableNodes.get(pseudoRandomInstance.nextInt(perturbableNodesNumber));    //scegliamo un nodo a caso

			flippedState = stateInExam.flipNodesValues(nodeIndex);

			//...dynamics
			int iterations = cutoff; // limite di passi consentiti per ogni perturbazione prima di dire che c'è stata una perturbazione persa
			while (iterations > 0) {
				iterations--;
				int otherAttractorIndex = AttractorsUtility.retrieveAttractorListIndex(flippedState, attractorsList);
				if (otherAttractorIndex != -1) {
					atm[attractorIndex][otherAttractorIndex] = atm[attractorIndex][otherAttractorIndex] + 1;
					break;
				}
				flippedState = dynamics.nextState(flippedState);
			}
			if (iterations == 0) { //non si è raggiunto nessun attrattore conosciuto (precedentemente trovato)
				if (addedIterations < limitOfAttemptsForAddedIterations) {
					//aggiungiamo una perturbazione ai nodi
					addedIterations++;
				} else {
					perturbationsLost++;
				}
			}

		}
		//GenericUtility.printMatrix(atm);
		return new AtmImpl<>(atm, attractors,perturbationsLost);

	}


}

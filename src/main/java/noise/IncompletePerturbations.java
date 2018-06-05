package noise;

import attractor.AttractorsUtility;
import exceptions.EmptyAttractorsList;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import io.vavr.Function6;
import org.apache.commons.math3.random.RandomGenerator;
import tes.AtmImpl;
import java.util.List;

/**
 * This class permits to instantiate objects to performs the incomplete version of the perturbations; this class
 * cannot derive from the complete version, or vice versa, because this implies stochasticity whilst the complete version
 * is deterministic and follows the definition of theoretical construction of ATM
 * (see "A Dynamical Model of Genetic Networks for Cell Differentiation" Villani, M. Barbieri, A. Serra, R.)
 * @author michelebraccini
 *
 */
public class IncompletePerturbations implements Function6<Attractors<BinaryState>, Dynamics<BinaryState>, Integer,
		Integer, Integer,RandomGenerator, Atm<BinaryState>> {
	
	
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
		return new AtmImpl<>(atm, attractors);
	}

}

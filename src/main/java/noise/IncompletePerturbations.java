package noise;

import attractor.AttractorsUtility;
import exceptions.EmptyAttractorsList;
import interfaces.attractor.ImmutableAttractor;
import interfaces.attractor.ImmutableList;
import interfaces.dynamic.Dynamics;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import tes.AtmImpl;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * This class permits to instantiate objects to performs the incomplete version of the perturbations; this class
 * cannot derive from the complete version, or vice versa, because this implies stochasticity whilst the complete version
 * is deterministic and follows the definition of theoretical construction of ATM
 * (see "A Dynamical Model of Genetic Networks for Cell Differentiation" Villani, M. Barbieri, A. Serra, R.)
 * @author michelebraccini
 *
 */
public class IncompletePerturbations implements Callable<Atm<BinaryState>> {

	protected final Random pseudoRandomInstance;
	protected ImmutableList<ImmutableAttractor<BinaryState>> attractorsList;
	protected Dynamics<BinaryState> dynamics;
	protected int[][] atm;
	protected int nodesNumber;
	protected int percentageStatesToPerturb; // possono variare in numero in ogni attrattore
	protected int nodesNumberToPerturb;
	protected int cutoff;
	protected int perturbationsLost;

	public IncompletePerturbations(	ImmutableList<ImmutableAttractor<BinaryState>> attractorsList,
									Dynamics<BinaryState> dynamics,
									int percentageStatesToPerturb,
									int percentageNodesToPerturb,
									int cutoff, Random random) {
		if (attractorsList.size() == 0) {
			throw new EmptyAttractorsList();
		}
		this.attractorsList = attractorsList;
		this.dynamics = dynamics;
		this.atm = new int[this.attractorsList.size()][this.attractorsList.size()];
		this.nodesNumber = this.attractorsList.get(0).getFirstState().getLength(); //At least one attractor must exist in a network

		this.percentageStatesToPerturb = percentageStatesToPerturb;
		
		this.nodesNumberToPerturb = (int) (Math.ceil((this.nodesNumber * percentageNodesToPerturb) / 100.0));
		this.cutoff = cutoff;
		this.pseudoRandomInstance = random;
	}
	
	
	// gli attrattori li guardiamo tutti -> 100%
	// contatore per i tentativi falliti perché potremmo non farcela a prenderli tutti
	// anche la possibilità di inisistere fino ad un determinato numero di tentativi falliti!
	
	public Atm<BinaryState> call(){
		int addedIterationsNodes = 0;
		int addedIterationsStates = 0;  //numero di iterazioni aggiunte man mano che le perturbazioni vengono perse

		List<BinaryState> statesInThisAttractor;
		BinaryState stateInExam;
		BinaryState flippedState;
		//Per un sottoinsieme di tutti gli attrattori
		
		for(int attractorIndex = 0; attractorIndex < this.attractorsList.size(); attractorIndex++){
			statesInThisAttractor = this.attractorsList.get(attractorIndex).getStates();
			int statesNumber = statesInThisAttractor.size(); 	//numero di stati in questo attrattore!
			/*
			 * Here we calculate the number of the states to perturb
			 */
			int statesNumberToPerturb = (int) (Math.ceil((statesNumber * this.percentageStatesToPerturb) / 100.0));
			//
			
			//Per un sottoinsieme di stati
			int stateIndex;
			for(int statIn = 0; statIn < statesNumberToPerturb + addedIterationsStates; statIn++){
				stateIndex = pseudoRandomInstance.nextInt(statesNumber);
				stateInExam = statesInThisAttractor.get(stateIndex);
				//Per ogni nodo (bit), applichiamo un flip di un time step!
				int nodeIndex;
				for(int nodIn = 0; nodIn < this.nodesNumberToPerturb + addedIterationsNodes; nodIn++){
					nodeIndex = pseudoRandomInstance.nextInt(this.nodesNumber);
					//flip
					flippedState = stateInExam.flipNodesValues(nodeIndex);


					//...dynamics
					int iterations = this.cutoff; // limite di passi consentiti per ogni perturbazione prima di dire che c'è stata una perturbazione persa
					while(iterations > 0){
						iterations--;
						int otherAttractorIndex = AttractorsUtility.retrieveAttractorListIndex(flippedState, this.attractorsList);
						if(otherAttractorIndex != -1){
							atm[attractorIndex][otherAttractorIndex] = atm[attractorIndex][otherAttractorIndex] + 1;
							break;
						}
						flippedState = dynamics.nextState(flippedState);
					}
					if(iterations == 0){ //non si è raggiunto nessun attrattore conosciuto (precedentemente trovato)
						if((this.nodesNumberToPerturb + addedIterationsNodes) < this.nodesNumber ){
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
		return new AtmImpl<>(this.atm, this.attractorsList);
	}

}

package noise;

import java.util.List;
import java.util.concurrent.Callable;
import attractor.AttractorsUtility;
import exceptions.EmptyAttractorsList;
import interfaces.attractor.ImmutableList;
import interfaces.attractor.LabelledOrderedAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import tes.AtmImpl;

public class CompletePerturbations implements Callable<Atm<BinaryState>> {
	
	protected ImmutableList<LabelledOrderedAttractor<BinaryState>> attractorsList;
	protected Dynamics<BinaryState> dynamics;
	protected int[][] atm;
	protected int nodesNumber;
	protected int cutoff;
	protected int perturbationsLost;

	public CompletePerturbations(ImmutableList<LabelledOrderedAttractor<BinaryState>> attractorsList, Dynamics<BinaryState> dynamics, int cutoff){
		if (attractorsList.size() == 0) {
			throw new EmptyAttractorsList();
		}
		this.attractorsList = attractorsList;
		this.dynamics = dynamics;
		this.atm = new int[this.attractorsList.size()][this.attractorsList.size()];
		this.nodesNumber = this.attractorsList.get(0).getFirstState().getLength(); //At least one attractor must exist in a network
		this.cutoff = cutoff;
	}
	
	public Atm<BinaryState> call(){
		List<BinaryState> statesInThisAttractor;
		BinaryState stateInExam;
		BinaryState flippedState;
		int statesNumber;
		//Per ogni attrattore
		for(int attractorIndex = 0; attractorIndex < this.attractorsList.size(); attractorIndex++){
			statesInThisAttractor = this.attractorsList.get(attractorIndex).getStates();
			statesNumber = statesInThisAttractor.size(); 	//numero di stati in questo attrattore!

			//Per ogni stato di ogni attrattore
			for(int stateIndex = 0; stateIndex < statesNumber; stateIndex++){
				stateInExam = statesInThisAttractor.get(stateIndex);
				//Per ogni nodo (bit), applichiamo un flip di un time step!
				for(int nodeIndex = nodesNumber - 1; nodeIndex >= 0; nodeIndex--){
					//iniziamo dall'n-1 nodo (->'0'000101)
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
						perturbationsLost++;
					}
				}
			}
		}
		System.out.println("Perturbations LOST: " + perturbationsLost);
		return new AtmImpl<>(this.atm, this.attractorsList);
	}
	
	
}

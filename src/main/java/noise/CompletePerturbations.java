package noise;

import attractor.AttractorsUtility;
import exceptions.EmptyAttractorsList;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import io.vavr.Function3;
import tes.AtmImpl;
import java.util.List;

public class CompletePerturbations implements Function3<Attractors<BinaryState>, Dynamics<BinaryState>, Integer, Atm<BinaryState>> {

    public Atm<BinaryState> apply(Attractors<BinaryState> attractors, Dynamics<BinaryState> dynamics, Integer cutoff) {
        if (attractors.numberOfAttractors() == 0) {
            throw new EmptyAttractorsList();
        }
        List<ImmutableAttractor<BinaryState>> attractorsList = attractors.getAttractors();

        int[][] atm = new int[attractorsList.size()][attractorsList.size()];
        int nodesNumber = attractorsList.get(0).getFirstState().getLength(); //At least one attractor must exist in a network
        int perturbationsLost = 0;
        //
        List<BinaryState> statesInThisAttractor;
        BinaryState stateInExam;
        BinaryState flippedState;
        int statesNumber;
        //Per ogni attrattore
        for (int attractorIndex = 0; attractorIndex < attractorsList.size(); attractorIndex++) {
            statesInThisAttractor = attractorsList.get(attractorIndex).getStates();
            statesNumber = statesInThisAttractor.size();    //numero di stati in questo attrattore!

            //Per ogni stato di ogni attrattore
            for (int stateIndex = 0; stateIndex < statesNumber; stateIndex++) {
                stateInExam = statesInThisAttractor.get(stateIndex);
                //Per ogni nodo (bit), applichiamo un flip di un time step!
                for (int nodeIndex = nodesNumber - 1; nodeIndex >= 0; nodeIndex--) {
                    //iniziamo dall'n-1 nodo (->'0'000101)
                    //flip
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
                        perturbationsLost++;
                    }
                }
            }
        }
        //System.out.println("Perturbations LOST: " + perturbationsLost);
        return new AtmImpl<>(atm, attractors);
    }

    /**
     * Aggiunti i nodi "congelati" in modo che non li perturbi
     * @param attractors
     * @param dynamics
     * @param cutoff
     * @param frozenNodesIndices
     * @return
     */
    public Atm<BinaryState> apply(Attractors<BinaryState> attractors, Dynamics<BinaryState> dynamics, Integer cutoff, List<Integer> frozenNodesIndices) {
        if (attractors.numberOfAttractors() == 0) {
            throw new EmptyAttractorsList();
        }
        List<ImmutableAttractor<BinaryState>> attractorsList = attractors.getAttractors();

        int[][] atm = new int[attractorsList.size()][attractorsList.size()];
        int nodesNumber = attractorsList.get(0).getFirstState().getLength(); //At least one attractor must exist in a network
        int perturbationsLost = 0;
        //
        List<BinaryState> statesInThisAttractor;
        BinaryState stateInExam;
        BinaryState flippedState;
        int statesNumber;
        //Per ogni attrattore
        for (int attractorIndex = 0; attractorIndex < attractorsList.size(); attractorIndex++) {
            statesInThisAttractor = attractorsList.get(attractorIndex).getStates();
            statesNumber = statesInThisAttractor.size();    //numero di stati in questo attrattore!

            //Per ogni stato di ogni attrattore
            for (int stateIndex = 0; stateIndex < statesNumber; stateIndex++) {
                stateInExam = statesInThisAttractor.get(stateIndex);
                //Per ogni nodo (bit), applichiamo un flip di un time step!
                for (int nodeIndex = nodesNumber - 1; nodeIndex >= 0; nodeIndex--) {
                    //iniziamo dall'n-1 nodo (->'0'000101)
                    //System.out.println("nodeIndex: "+ nodeIndex);

                    if (frozenNodesIndices.contains(nodeIndex)){
                        //we skip this perturbation
                        //System.out.println("la skippo");
                        continue;
                    }
                    //System.out.println("la faccio");
                    //flip
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
                        perturbationsLost++;
                    }
                }
            }
        }
        //System.out.println("Perturbations LOST: " + perturbationsLost);
        return new AtmImpl<>(atm, attractors);
    }

}

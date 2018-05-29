package tes;

import interfaces.attractor.Attractors;
import interfaces.dynamic.Dynamics;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import interfaces.tes.TESDifferentiationTree;
import interfaces.tes.Tes;
import noise.CompletePerturbations;
import noise.IncompletePerturbations;
import simulator.AttractorsFinderService;
import utility.Constant;
import java.util.Random;


public class StaticAnalysisTES {
    private StaticAnalysisTES(){}

    /**
     * Attractors
     * @param generator
     * @param dynamics
     * @return
     */
    public static Attractors<BinaryState> attractors(Generator<BinaryState> generator, Dynamics<BinaryState> dynamics){
       return new AttractorsFinderService<BinaryState>().apply(generator,
                                                                dynamics,
                                                                Constant.BASIN_COMPUTATION_DEFAULT_VALUE,
                                                                Constant.TRANSIENTS_COMPUTATION_DEFAULT_VALUE);
    }

    /**
     * ATM with complete perturbations
     * @param attractors
     * @param dynamics
     * @return
     */
    public static Atm<BinaryState> atmFromCompletePerturbations(Attractors<BinaryState> attractors, Dynamics<BinaryState> dynamics){
        return new CompletePerturbations().apply(attractors, dynamics, Constant.PERTURBATIONS_CUTOFF);
    }


    /**
     * ATM with incomplete perturbations
     * @param attractors
     * @param dynamics
     * @param percentageStatesToPerturb
     * @param percentageNodesToPerturb
     * @param r
     * @return
     */
    public static Atm<BinaryState> atmFromIncompletePerturbations(Attractors<BinaryState> attractors,
                                                       Dynamics<BinaryState> dynamics,
                                                       Integer percentageStatesToPerturb,
                                                       Integer percentageNodesToPerturb,
                                                       Random r){
        return new IncompletePerturbations().apply(attractors, dynamics,percentageStatesToPerturb, percentageNodesToPerturb, Constant.PERTURBATIONS_CUTOFF,r);
    }

    /**
     * TES tree
     * @param atm
     * @param r
     * @return
     */
    public static TESDifferentiationTree<BinaryState, Tes<BinaryState>> TESDifferentiationTree(Atm<BinaryState> atm, Random r){
        return new TesCreator<>(atm, r).apply();
    }


}

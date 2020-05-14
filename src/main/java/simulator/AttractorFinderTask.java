package simulator;

import attractor.AttractorsUtility;
import attractor.MutableAttractorImpl;
import attractor.TransientImpl;
import interfaces.attractor.MutableAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.simulator.AttractorFinderResult;
import interfaces.simulator.ExperimentTraceabilityInfo;
import interfaces.state.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class AttractorFinderTask<T extends State>{
    //private final MyCountDownLatch latch;
    private final T initialState;
    private final Collection<MutableAttractor<T>> collectionMutableAttractor;
    private final Dynamics<T> dynamics;
    private final List<T> states;
    private final Boolean basinsComputation, transientsComputation;
    private List<T> transientList;
    private List<T> transientTemp;
    private final Predicate<Integer> terminationCondition;

    public AttractorFinderTask(T initialState,
                               Dynamics<T> dynamics,
                               //MyCountDownLatch latch,
                               Collection<MutableAttractor<T>> collectionMutableAttractor,
                               Boolean basinsComputation,
                               Boolean transientsComputation,
                               Predicate<Integer> terminationCondition) {
        this.initialState = initialState;
        this.dynamics = dynamics;
        //this.latch = latch;
        this.collectionMutableAttractor = collectionMutableAttractor;
        this.states = new ArrayList<>();
        this.basinsComputation = basinsComputation;
        this.transientsComputation = transientsComputation;
        this.terminationCondition = terminationCondition;
    }

    /*public Void call() throws Exception {

        findAttractor();

        //latch.countDown();
        return null;
    }*/

    public AttractorFinderResult<T> findAttractor() {
        T state = initialState;

        int iter = 0;
        while (terminationCondition.test(iter)) {

            if (checksIfAlreadyPresent(state)) {
                transientList = states;
                checkAndUpdateBasinAndTransient(state);
                //return null; //se è presente esco!
                return new AttractorFinderResult(){
                    @Override
                    public MutableAttractor attractorFound() {
                        return null;
                    }

                    @Override
                    public Boolean isCutOff() {
                        return Boolean.FALSE;
                    }

                    @Override
                    public Boolean wasAlreadyPresent() {
                        return Boolean.TRUE;
                    }
                };
            }

            if (this.states.contains(state)) {
                transientTemp = states.subList(0, states.indexOf(state)); //N.B. non è una nuova lista (ma uno spaccato della "states")
                transientList = new ArrayList<>(transientTemp);
                transientTemp.clear(); //rimuovo gli stati da quello trovato (escluso) all'indietro
                MutableAttractor<T> attractor = new MutableAttractorImpl<>(states);
                //collectionMutableAttractor.add(attractor);
                updateItsBasin(attractor);
                updateItsTransient(attractor);
                return new AttractorFinderResult(){
                    @Override
                    public MutableAttractor attractorFound() {
                        return attractor;
                    }

                    @Override
                    public Boolean isCutOff() {
                        return Boolean.FALSE;
                    }

                    @Override
                    public Boolean wasAlreadyPresent() {
                        return Boolean.FALSE;
                    }
                };
            }

            states.add(state);
            state = dynamics.nextState(state);

            iter++;
        }

        return new AttractorFinderResult(){
            @Override
            public MutableAttractor attractorFound() {
                return null;
            }

            @Override
            public Boolean isCutOff() {
                return Boolean.TRUE;
            }

            @Override
            public Boolean wasAlreadyPresent() {
                return Boolean.FALSE;
            }
        };
    }

    protected boolean checksIfAlreadyPresent(T state) {
        if (collectionMutableAttractor.stream().anyMatch(x -> x.getStates().contains(state))) {
            return true;
        }
        return false;
    }

    private void checkAndUpdateBasinAndTransient(T state) {
        AttractorsUtility.retrieveAttractor(state, collectionMutableAttractor)
                        .ifPresent(attractor -> {   MutableAttractor<T> att = (MutableAttractor<T>) attractor;
                                                    updateItsBasin(att);
                                                    updateItsTransient(att);
                                                });
    }

    private void updateItsBasin(MutableAttractor<T> attractor) {
        if (basinsComputation) {
            attractor.updateBasin(initialState);
        } else {
            attractor.updateBasinDimension(1);
        }
    }

    private void updateItsTransient(MutableAttractor<T> attractor) {
        if (transientsComputation) {
            attractor.addTransient(new TransientImpl<>(transientList));
        } else {
            attractor.addTransientLength(transientList.size());
        }
    }



}

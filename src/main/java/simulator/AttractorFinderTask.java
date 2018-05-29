package simulator;

import attractor.AttractorsUtility;
import attractor.MutableAttractorImpl;
import attractor.TransientImpl;
import interfaces.attractor.MutableAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.state.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class AttractorFinderTask<T extends State> implements Callable<Void> {

    private final MyCountDownLatch latch;
    private final T initialState;
    private final Collection<MutableAttractor<T>> collectionMutableAttractor;
    private final Dynamics<T> dynamics;
    private final List<T> states;
    private final Boolean basinsComputation, transientsComputation;
    private List<T> transientList;
    private List<T> transientTemp;

    public AttractorFinderTask(T initialState,
                               Dynamics<T> dynamics,
                               MyCountDownLatch latch,
                               Collection<MutableAttractor<T>> collectionMutableAttractor,
                               Boolean basinsComputation,
                               Boolean transientsComputation) {
        this.initialState = initialState;
        this.dynamics = dynamics;
        this.latch = latch;
        this.collectionMutableAttractor = collectionMutableAttractor;
        this.states = new ArrayList<>();
        this.basinsComputation = basinsComputation;
        this.transientsComputation = transientsComputation;
    }

    @Override
    public Void call() throws Exception {

        findAttractor();

        latch.countDown();
        return null;
    }

    private void findAttractor() {
        T state = initialState;

        while (true) {

            if (checksIfAlreadyPresent(state)) {
                transientList = states;
                checkAndUpdateBasinAndTransient(state);
                return; //se è presente esco!
            }

            if (this.states.contains(state)) {
                transientTemp = states.subList(0, states.indexOf(state)); //N.B. non è una nuova lista (ma uno spaccato della "states")
                transientList = new ArrayList<>(transientTemp);
                transientTemp.clear(); //rimuovo gli stati da quello trovato (escluso) all'indietro
                MutableAttractor<T> attractor = new MutableAttractorImpl<>(states);
                collectionMutableAttractor.add(attractor);
                updateItsBasin(attractor);
                updateItsTransient(attractor);
                return;
            }

            states.add(state);
            state = dynamics.nextState(state);
        }


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

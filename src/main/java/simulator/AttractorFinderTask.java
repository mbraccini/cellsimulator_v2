package simulator;

import attractor.MutableAttractorImpl;
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

    public AttractorFinderTask(T initialState, Dynamics<T> dynamics, MyCountDownLatch latch, Collection<MutableAttractor<T>> collectionMutableAttractor) {
        this.initialState = initialState;
        this.dynamics = dynamics;
        this.latch = latch;
        this.collectionMutableAttractor = collectionMutableAttractor;
        this.states = new ArrayList<>();
    }

    @Override
    public Void call() throws Exception {
        //System.out.println(Thread.currentThread().getName() + ", Long task executing.... " + initialState);

        findAttractor();

        latch.countDown();
        return null;
    }

    private void findAttractor() {
        T state = initialState;

        while (true) {

            if (checksIfAlreadyPresent(state)) {
                return; //se è presente esco!
            }

            if (this.states.contains(state)) {
                states.subList(0, states.indexOf(state)).clear(); //rimuovo gli stati da quello trovato (escluso) all'indietro
                collectionMutableAttractor.add(new MutableAttractorImpl<>(states));
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



}

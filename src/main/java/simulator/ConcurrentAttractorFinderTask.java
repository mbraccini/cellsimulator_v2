package simulator;

import attractor.AttractorInfoImpl;
import interfaces.attractor.AttractorInfo;
import interfaces.dynamic.Dynamics;
import interfaces.state.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class ConcurrentAttractorFinderTask<T extends State> implements Callable<Void> {


    private final MyCountDownLatch latch;
    private final T initialState;
    private final ThreadSafeArrayList<T> collectionAttractorInfo;
    private final Dynamics<T> dynamics;
    private final List<T> states;

    public ConcurrentAttractorFinderTask(T state, Dynamics<T> dynamics, MyCountDownLatch latch, ThreadSafeArrayList<T> collectionAttractorInfo) {
        this.initialState = state;
        this.dynamics = dynamics;
        this.latch = latch;
        this.collectionAttractorInfo = collectionAttractorInfo;
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

            /*if (checksIfAlreadyPresent(state)) {
                return; //se è presente esco!
            }*/

            if (this.states.contains(state)) {
                states.subList(0, states.indexOf(state)).clear(); //rimuovo gli stati da quello trovato (escluso) all'indietro
                collectionAttractorInfo.add(new AttractorInfoImpl<>(states));
                return;
            }

            states.add(state);
            state = dynamics.nextState(state);
        }


    }

    private boolean checksIfAlreadyPresent(T state) {
        if (collectionAttractorInfo.containsState(state)) {
            return true;
        }
        return false;

    }



}

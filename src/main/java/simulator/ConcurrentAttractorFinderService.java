package simulator;

import attractor.AttractorsUtility;
import interfaces.attractor.*;
import interfaces.dynamic.Dynamics;
import interfaces.state.BinaryState;
import interfaces.state.State;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentAttractorFinderService<T extends State> implements Callable<ImmutableList<LabelledOrderedAttractor<T>>> {

    private final Generator<T> generator;
    private final Dynamics<T> dynamics;
    private final ExecutorService executor;


    public ConcurrentAttractorFinderService(Generator<T> generator, Dynamics<T> dynamics, int poolSize) {
        this.generator = generator;
        this.dynamics = dynamics;

        /* the number of worker threads to be created */
        //final int poolSize = Runtime.getRuntime().availableProcessors() + 1;

        /* creates a fixed thread pool */
        this.executor = Executors.newFixedThreadPool(poolSize);

    }

    @Override
    public ImmutableList<LabelledOrderedAttractor<T>> call(){
        BigInteger combinations = this.generator.totalNumberOfSamplesToBeGenerated();
        MyCountDownLatch latch = new MyCountDownLatch(combinations);
        ThreadSafeArrayList<T> list = new ThreadSafeArrayList<>(); //THREAD-SAFE

        T state = generator.nextSample();
        while (state != null) {
            try {
                executor.submit(new ConcurrentAttractorFinderTask<>(state, dynamics, latch, list));
            } catch (Exception e) {
                e.printStackTrace();
            }
            state = generator.nextSample();
        }
        latch.await(); //AWAIT
        executor.shutdown();

        // 1 THREAD
        //list.forEach(x->System.out.println(x.getStates()));
        ImmutableList<LabelledOrderedAttractor<T>> l = AttractorsUtility.fromInfoToAttractors(list.toImmutableList());
        return l;
    }



}

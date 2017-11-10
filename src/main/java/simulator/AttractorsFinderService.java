package simulator;

import attractor.ImmutableAttractorsListImpl;
import interfaces.attractor.*;
import interfaces.dynamic.Dynamics;
import interfaces.state.BinaryState;
import interfaces.state.State;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class AttractorsFinderService<T extends State> implements Callable<List<LabelledOrderedAttractor<T>>> {
    /* thread pool */
    //final ExecutorService executor;
    private final Generator<T> generator;
    private final Dynamics<T> dynamics;

    public AttractorsFinderService(Generator<T> generator, Dynamics<T> dynamics) {
        this.generator = generator;
        this.dynamics = dynamics;
        	/* the number of worker threads to be created */
        //final int poolSize = Runtime.getRuntime().availableProcessors() + 1;

            /* creates a fixed thread pool */
        //this.executor = Executors.newFixedThreadPool(poolSize);

    }

    @Override
    public List<LabelledOrderedAttractor<T>> call(){
        BigInteger combinations = this.generator.totalNumberOfSamplesToBeGenerated();
        MyCountDownLatch latch = new MyCountDownLatch(combinations);
        Collection<AttractorInfo<T>> list = new ArrayList<>();

        T state = generator.nextSample();
        while (state != null) {
            try {
                new AttractorFinderTask<>(state, dynamics, latch, list).call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            state = generator.nextSample();
        }
        latch.await();

        list.forEach(x->System.out.println(x.getStates()));
        return ImmutableAttractorsList.fromInfoToAttractors(list);
    }

    /*public List<OrderedAttractor<T>> find() {
        BigInteger combinations = this.generator.totalNumberOfSamplesToBeGenerated();
        MyCountDownLatch latch = new MyCountDownLatch(combinations);
        ThreadSafeArrayList<OrderedAttractor<BinaryState>> list = new ThreadSafeArrayList<>();

        T state = generator.nextSample();
        while (state != null) {
            executor.submit(new ConcurrentAttractorFinderTask(state, dynamics, latch, list));
            state = generator.nextSample();
        }

        latch.await();
        System.out.println("Urrà");
        System.out.println("Count -> " + latch.getCount());
        executor.shutdownNow();

        System.out.println("size ->" + list.size());
        return list.toImmutableList();

    }
*/



}

package simulator;

import attractor.AttractorsUtility;
import interfaces.attractor.*;
import interfaces.dynamic.Dynamics;
import interfaces.pipeline.Pipe;
import interfaces.sequences.Generator;
import interfaces.state.State;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

public class AttractorsFinderService<T extends State> implements Callable<ImmutableList<ImmutableAttractor<T>>>, Pipe<AttractorFinderInput<T>, AttractorFinderOutput<T>> {
    /* thread pool */
    //final ExecutorService executor;
    private Generator<T> generator;
    private Dynamics<T> dynamics;

    public AttractorsFinderService() { }


    public AttractorsFinderService(Generator<T> generator, Dynamics<T> dynamics) {
        this.generator = generator;
        this.dynamics = dynamics;
        	/* the number of worker threads to be created */
        //final int poolSize = Runtime.getRuntime().availableProcessors() + 1;

            /* creates a fixed thread pool */
        //this.executor = Executors.newFixedThreadPool(poolSize);

    }

    @Override
    public ImmutableList<ImmutableAttractor<T>> call(){
        BigInteger combinations = this.generator.totalNumberOfSamplesToBeGenerated();
        MyCountDownLatch latch = new MyCountDownLatch(combinations);
        Collection<MutableAttractor<T>> list = new ArrayList<>();

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

        //list.forEach(x->System.out.println(x.getStates()));
        ImmutableList<ImmutableAttractor<T>> l = AttractorsUtility.fromInfoToAttractors(list);
        return l;
    }

    @Override
    public AttractorFinderOutput<T> apply(AttractorFinderInput<T> attractorFinderInput) {

        Generator<T> gen = attractorFinderInput.generator();
        Dynamics<T> dyn = attractorFinderInput.dynamics();
        BigInteger combinations = gen.totalNumberOfSamplesToBeGenerated();
        MyCountDownLatch latch = new MyCountDownLatch(combinations);
        Collection<MutableAttractor<T>> list = new ArrayList<>();

        T state = gen.nextSample();
        while (state != null) {
            try {
                new AttractorFinderTask<>(state, dyn, latch, list).call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            state = gen.nextSample();
        }
        latch.await();

        //list.forEach(x->System.out.println(x.getStates()));
        ImmutableList<ImmutableAttractor<T>> l = AttractorsUtility.fromInfoToAttractors(list);
        return new AttractorFinderOutput.AttractorFinderOutputImpl<>(l,dyn);
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

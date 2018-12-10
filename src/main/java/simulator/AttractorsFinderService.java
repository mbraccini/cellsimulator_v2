package simulator;

import attractor.AttractorsImpl;
import attractor.AttractorsUtility;
import interfaces.attractor.*;
import interfaces.dynamic.Dynamics;
import interfaces.pipeline.Pipe;
import interfaces.sequences.Generator;
import interfaces.simulator.AttractorFinderResult;
import interfaces.simulator.ExperimentTraceability;
import interfaces.state.State;
import io.vavr.Function2;
import io.vavr.Function4;
import io.vavr.Function5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

public class AttractorsFinderService  {
    private AttractorsFinderService(){}
    /* thread pool */
    //final ExecutorService executor;
    /*private Generator<T> generator;
    private Dynamics<T> dynamics;

    public AttractorsFinderService() { }


    public AttractorsFinderService(Generator<T> generator, Dynamics<T> dynamics) {
        this.generator = generator;
        this.dynamics = dynamics;
        	// the number of worker threads to be created
        //final int poolSize = Runtime.getRuntime().availableProcessors() + 1;

            // creates a fixed thread pool
        //this.executor = Executors.newFixedThreadPool(poolSize);

    }*/

    public static Predicate<Integer> TRUE_TERMINATION = x -> true;
    public static Function<Integer, Predicate<Integer>> CUT_OFF_PERCENTAGE_TERMINATION = nodesNumber -> (x -> (x < (nodesNumber * 20)));


    public static <T extends State> Attractors<T> apply(Generator<T> generator, Dynamics<T> dynamics, Boolean basin, Boolean transients, Predicate<Integer> terminationCondition) {
        BigInteger combinations = generator.totalNumberOfSamplesToBeGenerated();
        //MyCountDownLatch latch = new MyCountDownLatch(combinations);
        Collection<MutableAttractor<T>> mutableAttractors = new ArrayList<>();
        AttractorFinderResult result;
        int initialStatesCutOff = 0;
        T state = generator.nextSample();
        while (state != null) {
            try {
                result = new AttractorFinderTask<>(state, dynamics, mutableAttractors, basin, transients, terminationCondition).findAttractor();
                if (result != null && result.isCutOff()) initialStatesCutOff++;
            } catch (Exception e) {
                e.printStackTrace();
            }
            state = generator.nextSample();
        }
        //latch.await();

        return new AttractorsImpl<>(mutableAttractors, new ExperimentTraceability(null, Map.of("initialStatesCutOff",initialStatesCutOff)));
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

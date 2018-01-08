package experiments.selfLoop;

import exceptions.InputConnectionsException;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.Factory;
import io.jenetics.util.IntRange;
import utility.Files;


import static io.jenetics.engine.Limits.bySteadyFitness;


public class Main {
    /**
     * PARAMETERS
     */
    static final long MAX_GENERATIONS = 10000;
    static final double ELITISM_FRACTION = 0.1;
    static final int POPULATION_SIZE = 50;
    static final int STEADY_FITNESS_LIMIT = 500; //idle iterations
    public static final int NODES_NUMBER = 20;
    public static final int K = 2;

    static final int Boolean_Function_Max_Value = (int) Math.round(Math.pow(2, Math.pow(2, K))) - 1;

    public static void main(String[] args) {
        // 1.) Define the genotype (factory) suitable
        //     for the problem.

        System.out.println("NODES_NUMBER: " + NODES_NUMBER);
        System.out.println("K: " + K);
        System.out.println("MAX_GENERATIONS: " + MAX_GENERATIONS);
        System.out.println("ELITISM_FRACTION: " + ELITISM_FRACTION);
        System.out.println("POPULATION_SIZE: " + POPULATION_SIZE);
        System.out.println("STEADY_FITNESS_LIMIT: " + STEADY_FITNESS_LIMIT);

        System.out.println("versione 3.0");

        if (K > NODES_NUMBER) {
            throw new InputConnectionsException("K must be <= #nodes!");
        }
        /*Factory<Genotype<IntegerGene>> gtf =
                Genotype.of(IntegerChromosome.of(IntRange.of(0, NODES_NUMBER - 1),K * NODES_NUMBER),
                        IntegerChromosome.of(IntRange.of(0, Boolean_Function_Max_Value), NODES_NUMBER)); //rappresenta 1 individuo*/

        Factory<Genotype<IntegerGene>> gtf =
                Genotype.of(TopologyFixedKBNChromosome.of(IntRange.of(0, NODES_NUMBER - 1), K * NODES_NUMBER, K, true),
                        IntegerChromosome.of(IntRange.of(0, Boolean_Function_Max_Value), NODES_NUMBER)); //rappresenta 1 individuo


        // 3.) Create the execution environment.
        Engine<IntegerGene, Double> engine = Engine
                .builder(BNGeneticAlgFitness::eval, gtf)
                .populationSize(POPULATION_SIZE)
                .offspringFraction(1 - ELITISM_FRACTION)
                .survivorsSelector(new TruncationSelector<>())
                .offspringSelector(new TournamentSelector<>(2))
                .alterers(
                        new Mutator<>(0.05),
                        new SinglePointCrossover<>(0.5))
                .build();

        final EvolutionStatistics<Double, ?>
                statistics = EvolutionStatistics.ofNumber();

        // 4.) Start the execution (evolution) and
        //     collect the result.
        Genotype<IntegerGene> result = engine.stream()
                .limit(bySteadyFitness(STEADY_FITNESS_LIMIT))
                .limit(MAX_GENERATIONS)
                .peek(statistics)
                .peek(r -> System.out.println("PEEK"))
                .collect(EvolutionResult.toBestGenotype());


        Files.createDirectories("GeneticAlg");
        Files.writeStringToFileUTF8("GeneticAlg/results.txt", "genotipo\n" + result.toString() + "\nstats\n" + statistics);
        Files.serializeObject(result, "GeneticAlg/BestGenotype");

    }


}

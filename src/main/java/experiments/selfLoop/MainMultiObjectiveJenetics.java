package experiments.selfLoop;

import exceptions.InputConnectionsException;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.ext.moea.MOEA;
import io.jenetics.ext.moea.UFTournamentSelector;
import io.jenetics.ext.moea.Vec;
import io.jenetics.util.Factory;
import io.jenetics.util.IntRange;
import org.jooq.lambda.tuple.Tuple2;
import utility.Files;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.jenetics.engine.Limits.bySteadyFitness;


public class MainMultiObjectiveJenetics {
    /**
     * PARAMETERS
     */


    static final long MAX_GENERATIONS = 10000;
    static final double ELITISM_FRACTION = 0.1;
    static final int POPULATION_SIZE = 20;
    static final int STEADY_FITNESS_LIMIT = 1000; //idle iterations
    public static final int NODES_NUMBER = 15;
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

        System.out.println("versione -1.0");

        if (K > NODES_NUMBER) {
            throw new InputConnectionsException("K must be <= #nodes!");
        }

        Factory<Genotype<IntegerGene>> gtf =
                Genotype.of(TopologyFixedKBNChromosome.of(IntRange.of(0, NODES_NUMBER - 1), K * NODES_NUMBER, K, false),
                        IntegerChromosome.of(IntRange.of(0, Boolean_Function_Max_Value), NODES_NUMBER)); //rappresenta 1 individuo


        Engine<IntegerGene, Vec<double[]>> engine = Engine
                .builder(GeneticAlgFitness::evalMultiObjective, gtf)
                .populationSize(POPULATION_SIZE)
                .offspringFraction(1 - ELITISM_FRACTION)
                .offspringSelector(new TournamentSelector<>(2))
                .survivorsSelector(UFTournamentSelector.ofVec())
                .alterers(
                        new Mutator<>(0.05),
                        new SinglePointCrossover<>(0.5))
                .build();

        final EvolutionStatistics<Vec<double[]>, ?>
                statistics = EvolutionStatistics.ofComparable();

        StringBuilder generationsStats = new StringBuilder();


        List<Tuple2<Vec<double[]>, Genotype<IntegerGene>>> result = engine.stream()
                .limit(bySteadyFitness(STEADY_FITNESS_LIMIT))
                .limit(MAX_GENERATIONS)
                .peek(statistics)
                .peek(r -> r.getPopulation().stream().forEach(x -> generationsStats.append("(" + r.getGeneration() + "," + x.getGeneration() + ")" + x.getFitness() + "\n")))
                .collect(MOEA.toParetoSet()).stream().map(x -> new Tuple2<>(x.getFitness(), x.getGenotype())).collect(Collectors.toList());

        String paretoSet = result.stream().map(x -> x.v1() + "; " + x.v2()).collect(StringBuilder::new, (x, y) -> x.append(y + "\n"), StringBuilder::append).toString();

        //Tuple2<Vec<double[]>, Genotype<IntegerGene>> best = result.stream().max((x, y) -> Optimize.MAXIMUM.<Vec<double[]>>compare(x.v1(), y.v1())).get();

        Files.createDirectories("GeneticAlg");
        Files.createDirectories("GeneticAlg/paretoObj");

        result.forEach(x -> Files.serializeObject(x.v2(), "GeneticAlg/paretoObj/" + x.v1().toString().replaceAll(", |\\[|\\]","_") ));

        Files.writeStringToFileUTF8("GeneticAlg/generationsStats.txt", generationsStats.toString());
        Files.writeStringToFileUTF8("GeneticAlg/stats.txt", statistics.toString());
        Files.writeStringToFileUTF8("GeneticAlg/pareto.txt", paretoSet);
        Files.writeListsToCsv(
                List.of(
                        List.of("NODES_NUMBER: " + NODES_NUMBER),
                        List.of("K: " + K),
                        List.of("MAX_GENERATIONS: " + MAX_GENERATIONS),
                        List.of("ELITISM_FRACTION: " + ELITISM_FRACTION),
                        List.of("POPULATION_SIZE: " + POPULATION_SIZE),
                        List.of("STEADY_FITNESS_LIMIT: " + STEADY_FITNESS_LIMIT)
                ), "GeneticAlg/parameters.csv");

/*
       Vec<double[]> a = Vec.of(0,1.0,2);
       Vec<double[]> b = Vec.of(1.0,0,0);

        Vec<double[]> c = Vec.of(3.0,4.0,4);
        System.out.println(c.toString().replaceAll(", |\\[|\\]","_"));
        List<Vec<double[]>> l =  new ArrayList<>(List.of(b,c,a));
       //Optimize o = Optimize.MINIMUM;
       //l.sort(o.<Vec<double[]>>ascending());
        //List<double[]> l1 = l.stream().map(x->x.data()).sorted(c.dominance()).collect(Collectors.toList());
       // l1.forEach(x-> Arrays.stream(x).forEach(y->System.out.print(y)));
*/

    }


}

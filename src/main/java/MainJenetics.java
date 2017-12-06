import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.Factory;
import io.jenetics.util.IntRange;

import static io.jenetics.engine.Limits.bySteadyFitness;


public class MainJenetics {
    static final long MAX_GENERATIONS = 1;
    static final int ELITISM = 1;
    static final int POPULATION_SIZE = 1;
    static final int STEADY_FITNESS_LIMIT = 1000;

    public static final int NODES_NUMBER = 4;
    public static final int K = 2;
    static final int Boolean_Function_Max_Value = (int)Math.round(Math.pow(2,Math.pow(2,K))) - 1;




    public static void main(String[] args) {
        // 1.) Define the genotype (factory) suitable
        //     for the problem.
        /*Factory<Genotype<IntegerGene>> gtf =
                Genotype.of(IntegerChromosome.of(IntRange.of(0, 19)), IntegerChromosome.of(IntRange.of(0, 19)), IntegerChromosome.of(IntRange.of(0, 15))); //rappresenta 1 individuo
*/
        /*Factory<Genotype<BitGene>> gtf =
                Genotype.of(BitChromosome.of(10 , 0.5));*/

        System.out.println("Boolean_Function_Max_Value " + Boolean_Function_Max_Value);
        Factory<Genotype<IntegerGene>> gtf =
                Genotype.of(IntegerChromosome.of(IntRange.of(0, NODES_NUMBER - 1),K * NODES_NUMBER),
                        IntegerChromosome.of(IntRange.of(0, Boolean_Function_Max_Value), NODES_NUMBER)); //rappresenta 1 individuo


        // 3.) Create the execution environment.
        Engine<IntegerGene, Double> engine = Engine
                .builder(BNGeneticAlgFitness::eval, gtf)
                .populationSize(POPULATION_SIZE)
                .offspringSelector(new TournamentSelector<>(2))
                .survivorsSize(POPULATION_SIZE-ELITISM)
                .survivorsSelector(new EliteSelector<IntegerGene, Double>(ELITISM, new TournamentSelector<>(2)))
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
                //.collect(EvolutionResult.toBestGenotype());
                .peek(r -> System.out.println(r.getGenotypes()))
                .collect(EvolutionResult.toBestGenotype());

        System.out.println("Hello World:\n" + result);
        System.out.println("Stats:\n" + statistics);

    }
}

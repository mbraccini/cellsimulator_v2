package experiments.methylation;

import dynamic.KnockOutDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.state.BinaryState;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import utility.RandomnessFactory;

import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PathDependencyTCS {
    /**                                  |
     *  __    __   __    __              |  __    __   __    __
     * |F |  |F | |F |  |F |  ...        | |  |  |  | |  |  |  |
     *  --    --   --    --              |  --    --   --    --
     * [congelabili] [(1-M)/100]*No.Nodes| (M/100)*No.Nodes  [proiezione/fenotipo]
     */


   /* public static void tryAtriplet(){
        Dynamics<BinaryState> dynamicsKO = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToKnockOut));
    }

    public static void differentLevelOfFreezing(List<Integer> indicesToKnockOut){

    }
*/

    public static void investigatePathDependecyProperty(final int nodesNumber,
                                                    final int k,
                                                    final double bias,
                                                    final int howManyNetworks,
                                                    final List<Double> freezingLevelFraction,
                                                    final Double phenotypeFraction,
                                                    final RandomGenerator r){
        //freezingLevelFraction rappresenta la frazione di nodi da congelare che appartiene alla parte dei geni congelabili, e quindi non quelli su cui si farà la proiezione degli attrattori

        for (int i = 0; i < howManyNetworks; i++) {
            BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
            bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);

            for(Double frozenFraction : freezingLevelFraction){
                 int numberOfFrozenStartingNodes = (int)Math.ceil(frozenFraction * (1-phenotypeFraction) * nodesNumber);
                //differentLevelOfFreezing(frozenFraction);
            }
        }


    }

    public static void main(String args[]){
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        final int nodesNumber = 100;
        List<Integer> list_indices = IntStream.range(0,nodesNumber).boxed().collect(Collectors.toList());

        final int k = 2;
        final double bias = 0.5;
        final int howManyNetworks = 10;
        investigatePathDependecyProperty(nodesNumber,k,bias,howManyNetworks,List.of(0.2,0.4,0.6), 0.3 ,r);
    }
}

package utility;

import interfaces.dynamic.Dynamics;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import org.apache.commons.text.similarity.HammingDistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnalysisUtility {

    private AnalysisUtility() {

    }

    /***
     * Mean of means of the one-step Derrida computed along each sample in samples and with perturbation for each index in indices
     * @param samples
     * @param dyn
     * @param indices
     * @return
     */
    static public double Derrida(final Generator<BinaryState> samples,
                                  final Dynamics<BinaryState> dyn,
                                  final Set<Integer> indices){
        //mean of means
        BinaryState state = samples.nextSample();
        HammingDistance hd = new HammingDistance();
        List<Double> means = new ArrayList<>();
        int hammingDistSum;
        while(state != null){
            //
            hammingDistSum = 0;
            for (Integer idx : indices) {
                /*
                System.out.println("state: " + state);
                System.out.println("afterFlip: " + dyn.nextState(state.flipNodesValues(idx)));
                System.out.println("onlyUpdate: " + dyn.nextState(state));
                System.out.println("hamming:" + hd.apply(dyn.nextState(state.flipNodesValues(idx)).getStringRepresentation(), dyn.nextState(state).getStringRepresentation()));
                */
                hammingDistSum += hd.apply(dyn.nextState(state.flipNodesValues(idx)).getStringRepresentation(), dyn.nextState(state).getStringRepresentation());
            }
            means.add((double)hammingDistSum / indices.size());

            // next state
            state = samples.nextSample();
        }
        //System.out.println("means: " + means);
        return means.stream().mapToDouble(x -> x).average().orElse(-1);
    }
}

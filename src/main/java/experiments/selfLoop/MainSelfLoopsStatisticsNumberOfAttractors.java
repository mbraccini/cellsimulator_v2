package experiments.selfLoop;

import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import interfaces.attractor.Attractors;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import simulator.AttractorsFinderService;
import utility.Files;
import utility.RandomnessFactory;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class MainSelfLoopsStatisticsNumberOfAttractors {


    public static final int SAMPLES_NUMBER = 10000; //for each configuration
    public static final int FROM_NUMBER_OF_SELFLOOPS = 0;
    public static final int TO_NUMBER_OF_SELFLOOPS = 11;


    public static void main(String[] args) {
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn = null;


        for (BooleanNetworkFactory.WIRING_TYPE wiringType : BooleanNetworkFactory.WIRING_TYPE.values()) {
            String directory = wiringType + Files.FILE_SEPARATOR;
            Files.createDirectories(directory);
            String subBNfolder = directory + "BNs" + Files.FILE_SEPARATOR;
            Files.createDirectories(subBNfolder);

            for (int i = FROM_NUMBER_OF_SELFLOOPS; i <= TO_NUMBER_OF_SELFLOOPS; i++) {
                List<Integer> numOfAtts = new ArrayList<>();
                for (int j = 0; j < SAMPLES_NUMBER; j++) {
                    bn = BooleanNetworkFactory.newBNwithSelfLoop(2, 0.5, 15, r, i, wiringType);

                    Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());
                    Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
                    Attractors<BinaryState> attractors = new AttractorsFinderService<BinaryState>().apply(generator, dynamics, true,false);
                    numOfAtts.add(attractors.numberOfAttractors());
                    Files.writeBooleanNetworkToFile(bn, subBNfolder + Files.FILE_SEPARATOR + "bn_sl_" + i + "#_" + j);
                }

                Files.writeListToTxt(numOfAtts, directory + Files.FILE_SEPARATOR + i);
            }
            Files.zip(subBNfolder, subBNfolder,true); //zip and delete
        }
    }



}

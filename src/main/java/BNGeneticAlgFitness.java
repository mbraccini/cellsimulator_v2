import interfaces.network.BooleanNetwork;
import interfaces.networkdescription.ExplicitFunExpr;
import interfaces.networkdescription.NameExpr;
import interfaces.networkdescription.NetworkAST;
import interfaces.networkdescription.TopologyExpr;
import io.jenetics.*;
import network.BooleanNetworkFactory;
import network.NaiveBNParser;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;


public class BNGeneticAlgFitness {
    private BNGeneticAlgFitness() {

    }


    public static int BINARY_DIGIT_NUMBER = (int) Math.round(Math.pow(2, MainJenetics.K));

    public static double eval(Genotype<IntegerGene> gt) {
        /**
         * INPUT_NODES
         */
        Chromosome<IntegerGene> inputNodes = gt.getChromosome(0);

        List<List<IntegerGene>> topology = new ArrayList<>();
        List<IntegerGene> node = new ArrayList<>();

        for (int i = 0, nodeNumber = 1; i < inputNodes.length(); i++) {
            node.add(inputNodes.getGene(i));
            if (i == (nodeNumber * MainJenetics.K) - 1) {
                //Node found
                topology.add(new ArrayList<>(node));
                node.clear();
                nodeNumber++;
            }
        }
        List<TopologyExpr> topologyExprList = new ArrayList<>();
        for (int nodeIndex = 0; nodeIndex < topology.size(); nodeIndex++) {
            topologyExprList.add(new NaiveBNParser.TopologyExprImpl("" + nodeIndex, topology.get(nodeIndex).stream().map(x -> "" + x.getAllele()).collect(Collectors.toList())));
        }

        /**
         * BOOLEAN_FUNCTIONS
         */
        Chromosome<IntegerGene> booleanFunctions = gt.getChromosome(1);
        List<ExplicitFunExpr> explicitFunExprList = new ArrayList<>();
        List<NameExpr> nameExprList = new ArrayList<>();

        // List<String> booleanFunctionsOutput = booleanFunctions.stream().map(x -> ).collect(Collectors.toList());
        for (int bfIndex = 0; bfIndex < booleanFunctions.length(); bfIndex++) {
            explicitFunExprList.add(new NaiveBNParser.ExplicitFunExprImpl("" + bfIndex, convertNumToBitString(booleanFunctions.getGene(bfIndex).getAllele(), BINARY_DIGIT_NUMBER)));
            nameExprList.add(new NaiveBNParser.NameExprImpl("" + bfIndex, "gene_" + bfIndex));
        }

        NetworkAST ast = new NaiveBNParser.AST(topologyExprList, explicitFunExprList, new ArrayList<>() , nameExprList);

        BooleanNetwork<BitSet, Boolean> bn = BooleanNetworkFactory.newNetworkFromAST(ast);
        //System.out.print(bn);


        return 0;
    }


    /**
     * Converts a number in decimal representation to a binary representation with a number of digitNUmber digit
     *
     * @param value
     * @param digitNumber
     * @return
     */
    public static String convertNumToBitString(int value, int digitNumber) {
        StringBuilder postfix = new StringBuilder();
        StringBuilder prefix = new StringBuilder();

        postfix.append(Integer.toBinaryString(value));
        while ((prefix.length() + postfix.length()) < digitNumber) {
            prefix.append('0');
        }

        prefix.append(postfix);
        return prefix.toString();
    }
}

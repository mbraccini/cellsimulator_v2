package network;

import exceptions.SemanticException;
import exceptions.SyntaxParserException;
import interfaces.network.BNClassic;
import interfaces.network.Node;
import interfaces.network.NodeDeterministic;
import interfaces.network.Table;
import interfaces.networkdescription.*;
import javafx.util.Builder;
import states.States;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BNFromASTDescription implements interfaces.network.Builder<BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>>> {


    private final NetworkAST ast;
    private final int nodesNumber;
    private List<NodeDeterministic<BitSet,Boolean>> nodesList = new ArrayList<>();
    private Map<Integer, List<Integer>> nodesMap = new HashMap<>();

    public BNFromASTDescription(NetworkAST ast) {
        this.ast = ast;
        this.nodesNumber = ast.getTopology().size();
        configure();
    }

    /*public static BNFromASTDescription newNetworkFromFile(String filename) {
        NetworkAST ast = new NaiveBNParser(filename).parse();
        return new BNFromASTDescription(ast.getTopology().size(), ast);
    }*/

    private final void configure() {
        checkNodeIndices();
        initNodes();
        initTopology();
    }

    /**
     * Check if in the description file there are node from 0 index to n - 1.
     */
    private void checkNodeIndices() {
        TopologyExpr topExpr = null;
        for (int id = 0; id < this.nodesNumber; id++) {
            topExpr = retrieveNodeTopology("" + id);
            if (Objects.isNull(topExpr)) {
                throw new SyntaxParserException("Node with index " + id + " missing. Node indices must be from 0 to #nodes - 1");
            }
        }
    }

    private void initNodes() {
        Function<Integer, Table<BitSet, Boolean>> function;

        if (ast.getImplicit().size() > 0) {
            function = x -> {
                return implicit(x);
            };
        } else if (ast.getExplicit().size() > 0) {
            function = x -> {
                return explicit(x);
            };
        } else {
            throw new SyntaxParserException("Syntax Error: Missing functions descriptions!");
        }

        for (int id = 0; id < this.nodesNumber; id++) {

            Table<BitSet, Boolean> table = function.apply(id);

            String nodeName = retrieveNodeName("" + id).getName();
            if (Objects.nonNull(nodeName)) {
                nodesList.add(new NodeDeterministicImpl<>(nodeName, id, table));
            } else {
                nodesList.add(new NodeDeterministicImpl<>("gene_" + id, id, table));
            }
        }

    }

    private Table<BitSet, Boolean> explicit(int id) {
        List<Boolean> outputsList = new ArrayList<Boolean>();

        ExplicitFunExpr funExpr = retrieveExplicitFun("" + id);
        String outputsString = funExpr.getOutput();

        int numberOfIncomingNodesComputed = (int) (Math.log10(outputsString.length()) / Math.log10(2)); //log_2(#output) = #ingressi
        if (retrieveNodeTopology("" + id).getInput().size() != numberOfIncomingNodesComputed) {
            throw new SemanticException("Incoming nodes don't match with the topology section... " + funExpr);
        }

        for (int i = 0; i < outputsString.length(); i++) {
            outputsList.add(outputsString.charAt(i) == '1' ? true : false);
        }
        //////////////////\\\\\\\\\\\\\\\\\\\
        return new ConfigurableTable(numberOfIncomingNodesComputed, outputsList);
        //////////////////\\\\\\\\\\\\\\\\\\\
    }

    private Table<BitSet, Boolean> implicit(int id) {
        TopologyExpr topolExpr = retrieveNodeTopology("" + id);
        int numberOfIncomingNodes = topolExpr.getInput().size(); //numero di incomingNodes per nodo //da verificare con quello che si è scritto nel minTermine
        List<Boolean> outputsList = new ArrayList<Boolean>(Collections.nCopies((int)(Math.pow(2, numberOfIncomingNodes)), false));

        List<Integer> incomingNodesIndices = topolExpr.getInput().stream().map(x -> Integer.valueOf(x)).collect(Collectors.toList());

        ImplicitFunExpr implExpr = retrieveImplicitFun("" + id);
        if (Objects.isNull(implExpr)) {
            throw new SyntaxParserException("Implicit function declarition missing for the index node " + id);
        }
        List<String> sumOfProducts = implExpr.getTerms();

        if (Objects.isNull(sumOfProducts)) {
            switch (implExpr.getDescriptiveFunction()) {
                case CONTRADICTION: // do nothing
                    break;
                case AND:
                    return new AndTable(numberOfIncomingNodes);
                case OR:
                    return new OrTable(numberOfIncomingNodes);
                default:
                    throw new SyntaxParserException("Error in implicit function declaration!");
            }

        } else {
            for (String product : sumOfProducts) {

                String productCleaned = product.replaceAll("!", ""); //rimuove i punti esclamativi
                String[] nodes = productCleaned.split("\\*");

                Set<Integer> incomingNodesDeducedFromTerms = Arrays.asList(nodes).stream().map(x -> Integer.valueOf(x)).collect(Collectors.toSet());
                System.out.println("incomingNodesDeducedFromTerms "+ incomingNodesDeducedFromTerms);
                System.out.println("product  "+ product);System.out.println("numberOfIncomingNodes  "+ numberOfIncomingNodes);

                if (incomingNodesDeducedFromTerms.size() != numberOfIncomingNodes) {
                    throw new SemanticException("Number of incoming nodes in the minTerm " + product + " does not reflect the number of incoming node express in the topology section");
                }

                BitSet binaryEntry = new BitSet(numberOfIncomingNodes);
                int input;
                boolean negate = false;
                String inputString = "";
                for (int i = 0; i < product.length(); i++) {
                    if (product.charAt(i) == '!') {
                        negate = true;
                    } else if (product.charAt(i) == '*') {
                        if (negate) {
                            throw new SyntaxParserException("Syntax Error: Inserted the ! before the *.");
                        } else {
                            continue;
                        }
                    } else {
                        while (i < product.length() && Character.toString(product.charAt(i)).matches("\\d")) {
                            /** we create a string with the digits of the number representing the index of the node **/
                            inputString += product.charAt(i);
                            i++;
                        }
                        input = Integer.valueOf(inputString);
                        inputString = "";

                        /* Checks if the nodes specified in the sum of products expression reflects the incoming nodes in the topology */
                        if (!incomingNodesIndices.contains(input)) {
                            throw new SemanticException("Boolean expression " + product + " does not respect the incoming nodes specified in the topology section!");
                        }

					/* Retrieves the position in the truth table of the node in order to set the 1 value in the correct position */
                        for (int position = 0; position < incomingNodesIndices.size(); position++) {
                            if (incomingNodesIndices.get(position).intValue() == input) {
                                binaryEntry.set(position, !negate);
                                negate = false;
                                break;
                            }
                        }
                    }

                }
                int entryWithValue1 = Math.toIntExact(States.convert(binaryEntry));
                outputsList.set(entryWithValue1, true);
            }

        }

        //////////////////\\\\\\\\\\\\\\\\\\\
        return new ConfigurableTable(numberOfIncomingNodes, outputsList);
        //////////////////\\\\\\\\\\\\\\\\\\\
    }

    private NameExpr retrieveNodeName(String id) {
        for (NameExpr nameExpr : ast.getNames()) {
            if (nameExpr.getNode().equals(id)) {
                return nameExpr;
            }
        }
        return null;
    }

    private ExplicitFunExpr retrieveExplicitFun(String id) {
        for (ExplicitFunExpr expl : ast.getExplicit()) {
            if (expl.getNode().equals(id)) {
                return expl;
            }
        }
        return null;
    }

    private ImplicitFunExpr retrieveImplicitFun(String id) {
        for (ImplicitFunExpr expl : ast.getImplicit()) {
            if (expl.getNode().equals(id)) {
                return expl;
            }
        }
        return null;
    }

    private TopologyExpr retrieveNodeTopology(String id) {
        for (TopologyExpr expl : ast.getTopology()) {
            if (expl.getNode().equals(id)) {
                return expl;
            }
        }
        return null;
    }

    private void initTopology() {
        List<Integer> incomingNodes;
        TopologyExpr topExpr = null;
        for (int id = 0; id < this.nodesNumber; id++) {
            topExpr = retrieveNodeTopology("" + id);
            incomingNodes = topExpr.getInput().stream().map(x -> Integer.valueOf(x)).collect(Collectors.toList());
            this.addNodes(id, incomingNodes);
        }
    }

    private void addNodes(int indexNode, List<Integer> indexListToAdd) {
        /*List<NodeDeterministic<BitSet, Boolean>> list = new ArrayList<>();
        for (int i : indexListToAdd) {
            list.add(nodesList.get(i));
        }*/
        this.nodesMap.put(indexNode, indexListToAdd);
    }





    @Override
    public BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> build() {
        return new BNClassicImpl<>(nodesList, nodesMap);
    }


    public static void main(String args[]) {
        NetworkAST ast = new NaiveBNParser("bn_test").parse();
        System.out.println(new BNFromASTDescription(ast).build());
    }
}

package network;

import exceptions.SemanticException;
import exceptions.SyntaxParserException;
import interfaces.network.Node;
import interfaces.network.Table;
import states.States;
import utility.Files;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NetworkFromFile extends AbstractBooleanNetwork<BitSet, Boolean> {


    private final NaiveBNParser.AST ast;

    private NetworkFromFile(int nodesNumber, NaiveBNParser.AST ast) {
        super(nodesNumber);
        this.ast = ast;
        configure();

    }

    public static NetworkFromFile newNetworkFromFile(String filename) {
        NaiveBNParser.AST ast = new NaiveBNParser(filename).parse();
        return new NetworkFromFile(ast.getTopology().size(), ast);
    }

    private final void configure() {
        checkNodeIndices();
        initNodes();
        initTopology();
    }

    /**
     * Check if in the description file there are node from 0 index to n - 1.
     */
    private void checkNodeIndices() {
        NaiveBNParser.TopologyExpr topExpr = null;
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
                nodesList.add(new NodeImpl<>(nodeName, id, table));
            } else {
                nodesList.add(new NodeImpl<>("gene_" + id, id, table));
            }
        }

    }

    private Table<BitSet, Boolean> explicit(int id) {
        List<Boolean> outputsList = new ArrayList<Boolean>();

        NaiveBNParser.ExplicitFunExpr funExpr = retrieveExplicitFun("" + id);
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
        NaiveBNParser.TopologyExpr topolExpr = retrieveNodeTopology("" + id);
        int numberOfIncomingNodes = topolExpr.getInput().size(); //numero di incomingNodes per nodo //da verificare con quello che si è scritto nel minTermine
        List<Boolean> outputsList = new ArrayList<Boolean>(Collections.nCopies((int)(Math.pow(2, numberOfIncomingNodes)), false));

        List<Integer> incomingNodesIndices = topolExpr.getInput().stream().map(x -> Integer.valueOf(x)).collect(Collectors.toList());

        NaiveBNParser.ImplicitFunExpr implExpr = retrieveImplicitFun("" + id);
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
                String[] terms = productCleaned.split("\\*");

                Set<Integer> incomingNodesDeducedFromTerms = Arrays.asList(terms).stream().map(x -> Integer.valueOf(x)).collect(Collectors.toSet());
                System.out.println("incomingNodesDeducedFromTerms "+ incomingNodesDeducedFromTerms);
                System.out.println("product  "+ product);System.out.println("numberOfIncomingNodes  "+ numberOfIncomingNodes);

                if (incomingNodesDeducedFromTerms.size() != numberOfIncomingNodes) {
                    throw new SemanticException("Number of incoming nodes in the minTerm " + product + " does not reflect the number of incoming node express in the topology section");
                }

                BitSet binaryEntry = new BitSet(numberOfIncomingNodes);
                int input;
                boolean negate = false;
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
                        input = Character.getNumericValue(product.charAt(i));

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

    private NaiveBNParser.NameExpr retrieveNodeName(String id) {
        for (NaiveBNParser.NameExpr nameExpr : ast.getNames()) {
            if (nameExpr.getNode().equals(id)) {
                return nameExpr;
            }
        }
        return null;
    }

    private NaiveBNParser.ExplicitFunExpr retrieveExplicitFun(String id) {
        for (NaiveBNParser.ExplicitFunExpr expl : ast.getExplicit()) {
            if (expl.getNode().equals(id)) {
                return expl;
            }
        }
        return null;
    }

    private NaiveBNParser.ImplicitFunExpr retrieveImplicitFun(String id) {
        for (NaiveBNParser.ImplicitFunExpr expl : ast.getImplicit()) {
            if (expl.getNode().equals(id)) {
                return expl;
            }
        }
        return null;
    }

    private NaiveBNParser.TopologyExpr retrieveNodeTopology(String id) {
        for (NaiveBNParser.TopologyExpr expl : ast.getTopology()) {
            if (expl.getNode().equals(id)) {
                return expl;
            }
        }
        return null;
    }

    private void initTopology() {
        List<Integer> incomingNodes;
        NaiveBNParser.TopologyExpr topExpr = null;
        for (int id = 0; id < this.nodesNumber; id++) {
            topExpr = retrieveNodeTopology("" + id);
            incomingNodes = topExpr.getInput().stream().map(x -> Integer.valueOf(x)).collect(Collectors.toList());
            this.addNodes(id, incomingNodes);
        }
    }

    private void addNodes(int indexNode, List<Integer> indexListToAdd) {
        List<Node<BitSet, Boolean>> list = new ArrayList<>();
        for (int i : indexListToAdd) {
            list.add(nodesList.get(i));
        }
        this.nodesMap.put(this.nodesList.get(indexNode), list);

    }


    /**
     * Parser
     */
    public static class NaiveBNParser {

        private static final String TOPOLOGY_keyword = "Topology:";
        private static final String EXPLICIT_keyword = "Functions E:";
        private static final String IMPLICIT_keyword = "Functions I:";
        private static final String NAMES_keyword = "Names:";

        private Set<String> keyWord = new HashSet<>(Arrays.asList(TOPOLOGY_keyword, EXPLICIT_keyword, IMPLICIT_keyword, NAMES_keyword));

        private enum ASFState {
            INIT, TOPOLOGY, FUNCTIONS_E, FUNCTIONS_I, NAMES
        }

        private String description;
        private String[] lines;

        private ASFState state;

        public NaiveBNParser(String filename) {
            this.description = Files.readFile(filename);

            state = ASFState.INIT;      // init State

            this.lines = description.split("[\\r\\n]+"); // split the lines using the newLine as separator
        }


        /**
         * Topology:
         * 0 : 1, 2
         * 1 : 0, 3
         * 2 : 1, 3
         * 3 : 0, 2
         * <p>
         * <p>
         * # 0: 1, 2 ->corrisponde alla Truth Table:
         * # x2 x1 | (t+1)
         * # 0  0  | ...
         * # 0  1  | ...
         * # ...   | ...
         * <p>
         * Functions E:
         * 0: 0111
         * 1: 0001
         * 2 :0111
         * 3: 0001
         * <p>
         * Functions I:
         * #0:   !1*2  +  1*2
         * #1:   0*2  +  !0*!2
         * <p>
         * Names:
         * 0: Gene_0
         * 1: Gene_1
         * 2: Gene_2
         * 3: Gene_3
         */


        /**
         * Check if the line corresponds to keyWord (actually keyLine)
         *
         * @param line
         * @return
         */
        private boolean checkIfEqualsToKeyWords(String line) {
            for (String s : keyWord) {
                if (s.equals(line)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Transition function
         *
         * @param inputLine
         * @return
         */
        private ASFState transition(String inputLine) {
            switch (state) {
                case INIT:
                    if (!inputLine.equals(TOPOLOGY_keyword)) {
                        throw new SyntaxParserException("Expected TOPOLOGY section");
                    } else {
                        return ASFState.TOPOLOGY;
                    }
                case TOPOLOGY:
                    if (inputLine.equals(EXPLICIT_keyword)) {
                        return ASFState.FUNCTIONS_E;
                    } else if (inputLine.equals(IMPLICIT_keyword)) {
                        return ASFState.FUNCTIONS_I;
                    } else {
                        throw new SyntaxParserException("Expected FUNCTIONS section");
                    }
                case FUNCTIONS_E:
                    if (!inputLine.equals(NAMES_keyword)) {
                        throw new SyntaxParserException("Expected NAMES section");
                    } else {
                        return ASFState.NAMES;
                    }
                case FUNCTIONS_I:
                    if (!inputLine.equals(NAMES_keyword)) {
                        throw new SyntaxParserException("Expected NAMES section");
                    } else {
                        return ASFState.NAMES;
                    }
                case NAMES:
                    if (checkIfEqualsToKeyWords(inputLine)) {
                        throw new SyntaxParserException("Description should be terminated");
                    } else {
                        return state;
                    }
            }
            return null;
        }

        private TopologyExpr topology(String line) {
            TopologyExpr topExpr = null;
            if (line.matches("\\d(\\s+)?:(\\s+)?(\\d(\\s+)?((\\s+)?,(\\s+)?\\d)*(\\s+)?)?")) {
                String readWithoutSpaces = line.replaceAll(" +", ""); //rimuove gli spazi
                String[] splitted = readWithoutSpaces.split("\\:");
                String[] incomingNodes = splitted[1].split("\\,");
                topExpr = new TopologyExpr(splitted[0], Arrays.asList(incomingNodes));
            }
            return topExpr;
        }

        private ExplicitFunExpr explicitFun(String line) {
            ExplicitFunExpr explicitExpr = null;
            if (line.matches("\\d(\\s+)?:(\\s+)?\\d+(\\s+)?")) {
                String readWithoutSpaces = line.replaceAll(" +", ""); //rimuove gli spazi
                String[] splitted = readWithoutSpaces.split("\\:");
                explicitExpr = new ExplicitFunExpr(splitted[0], splitted[1]);
            }
            return explicitExpr;
        }

        private ImplicitFunExpr implicitFun(String line) {
            ImplicitFunExpr implicitExpr = null;

            if (line.matches("\\d(\\s+)?:(\\s+)?(((((!)?\\d(\\*(!)?\\d)*)((\\s+)?\\+(\\s+)?((!)?\\d(\\*(!)?\\d)*))*)|(or)|(and))(\\s+)?)?")) {
                String readWithoutSpaces = line.replaceAll(" +", ""); //rimuove gli spazi
                String[] splitted = readWithoutSpaces.split("\\:");
                if (splitted.length > 1) { // vuol dire che c'è almeno un prodotto e quindi un 1 nella truth table
                    if (splitted[1].equals("or")) {
                        implicitExpr = new ImplicitFunExpr(splitted[0], "or");
                    } else if (splitted[1].equals("and")) {
                        implicitExpr = new ImplicitFunExpr(splitted[0], "and");
                    } else {
                        String[] products = splitted[1].split("\\+");
                        implicitExpr = new ImplicitFunExpr(splitted[0], Arrays.asList(products));
                    }
                } else {
                    implicitExpr = new ImplicitFunExpr(splitted[0], "contradiction");
                }
            }
            System.out.println(implicitExpr);
            return implicitExpr;
        }

        private NameExpr name(String line) {
            NameExpr name = null;

            if (line.matches("\\d(\\s+)?:(\\s+)?(\\w)*(\\s+)?")) {
                String readWithoutSpaces = line.replaceAll(" +", ""); //rimuove gli spazi
                String[] splitted = readWithoutSpaces.split("\\:");
                name = new NameExpr(splitted[0], splitted[1]);
            }
            return name;
        }

        // Collections
        private List<TopologyExpr> topology = new ArrayList<>();
        private List<ImplicitFunExpr> implicit = new ArrayList<>();
        private List<ExplicitFunExpr> explicit = new ArrayList<>();
        private List<NameExpr> names = new ArrayList<>();

        // parse
        public AST parse() {
            TopologyExpr top = null;
            ImplicitFunExpr impl = null;
            ExplicitFunExpr expl = null;
            NameExpr name = null;

            for (String line : lines) {
                if (line.startsWith("#")) {     //we skip the line
                    continue;
                }
                if (checkIfEqualsToKeyWords(line)) {
                    state = transition(line);   //we check the state change
                } else {
                    if (state != ASFState.INIT) {
                        switch (state) {
                            case TOPOLOGY:
                                top = topology(line);
                                if (top == null) {
                                    throw new SyntaxParserException("This isn't a topology description section: " + line);
                                } else {
                                    topology.add(top);
                                }
                                break;
                            case FUNCTIONS_E:
                                expl = explicitFun(line);
                                if (expl == null) {
                                    throw new SyntaxParserException("This isn't an explicit function description section: " + line);
                                } else {
                                    explicit.add(expl);
                                }
                                break;
                            case FUNCTIONS_I:
                                impl = implicitFun(line);
                                if (impl == null) {
                                    throw new SyntaxParserException("This isn't an implicit function description section: " + line);
                                } else {
                                    implicit.add(impl);
                                }
                                break;
                            case NAMES:
                                name = name(line);
                                if (name == null) {
                                    throw new SyntaxParserException("This isn't a names description section: " + line);
                                } else {
                                    names.add(name);
                                }
                                break;
                        }
                    }
                }
            }
            return new AST(topology, explicit, implicit, names);
        }

        private void removeAllSpaces() {
            description = description.replaceAll("\\s+", "");
        }


        public static class AST {
            private final Collection<TopologyExpr> topology;
            private Collection<ImplicitFunExpr> implicit;
            private Collection<ExplicitFunExpr> explicit;
            private Collection<NameExpr> names;

            public AST(Collection<TopologyExpr> topology,
                       Collection<ExplicitFunExpr> explicit,
                       Collection<ImplicitFunExpr> implicit,
                       Collection<NameExpr> names) {
                this.topology = topology;
                this.explicit = explicit;
                this.implicit = implicit;
                this.names = names;

            }

            public Collection<TopologyExpr> getTopology() {
                return topology;
            }

            public Collection<ImplicitFunExpr> getImplicit() {
                return implicit;
            }

            public Collection<ExplicitFunExpr> getExplicit() {
                return explicit;
            }

            public Collection<NameExpr> getNames() {
                return names;
            }

            @Override
            public String toString() {
                return "AST\n{" +
                        "\n topology=" + topology +
                        ",\n implicit=" + implicit +
                        ",\n explicit=" + explicit +
                        ",\n names=" + names +
                        "\n}";
            }
        }

        public static class TopologyExpr {
            private final String node;
            private final List<String> input;

            public TopologyExpr(String node, List<String> input) {
                this.node = node;
                this.input = input;
            }

            public String getNode() {
                return node;
            }

            public List<String> getInput() {
                return input;
            }

            @Override
            public String toString() {
                return "TopologyExpr{" +
                        "node='" + node + '\'' +
                        ", input=" + input +
                        '}';
            }
        }

        public static class ExplicitFunExpr {
            private final String node;
            private final String output;

            public ExplicitFunExpr(String node, String output) {
                this.node = node;
                this.output = output;
            }

            public String getNode() {
                return node;
            }

            public String getOutput() {
                return output;
            }

            @Override
            public String toString() {
                return "ExplicitFunExpr{" +
                        "node='" + node + '\'' +
                        ", output='" + output + '\'' +
                        '}';
            }
        }

        public static class ImplicitFunExpr {
            private enum DescriptiveFunction{
                CONTRADICTION, OR, AND, ERROR
            }
            private final String node;
            private List<String> terms;

            private DescriptiveFunction descriptiveFunction;

            public ImplicitFunExpr(String node, List<String> terms) {
                this.node = node;
                this.terms = terms;
            }

            public ImplicitFunExpr(String node, String descriptionWord) { // e.g. descriptionWord = "contradiction"
                this.node = node;
                checkDescriptiveFunction(descriptionWord);
            }

            private void checkDescriptiveFunction(String descritpion) {
                switch (descritpion) {
                    case "contradiction":
                        descriptiveFunction = DescriptiveFunction.CONTRADICTION;
                        break;
                    case "or":
                        descriptiveFunction = DescriptiveFunction.OR;
                        break;
                    case "and":
                        descriptiveFunction = descriptiveFunction.AND;
                        break;
                    default:
                        descriptiveFunction = descriptiveFunction.ERROR;
                        break;
                }
            }

            public DescriptiveFunction getDescriptiveFunction() {
                return descriptiveFunction;
            }

            public String getNode() {
                return node;
            }

            public List<String> getTerms() {
                return terms;
            }

            @Override
            public String toString() {
                return "ImplicitFunExpr{" +
                        "node='" + node + '\'' +
                        ", terms=" + terms +
                        ", descriptiveFunction='" + descriptiveFunction + '\'' +
                        '}';
            }
        }

        public static class NameExpr {
            private final String node;
            private final String name;

            public NameExpr(String node, String name) {
                this.node = node;
                this.name = name;
            }

            public String getNode() {
                return node;
            }

            public String getName() {
                return name;
            }

            @Override
            public String toString() {
                return "NameExpr{" +
                        "node='" + node + '\'' +
                        ", name='" + name + '\'' +
                        '}';
            }
        }


    }

    public static void main(String args[]) {
        System.out.print(NetworkFromFile.newNetworkFromFile("/Users/michelebraccini/IdeaProjects/cellsimulator/bn"));
    }
}

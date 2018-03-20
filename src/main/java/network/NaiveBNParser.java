package network;

import exceptions.SyntaxParserException;
import interfaces.networkdescription.*;
import utility.Files;

import java.util.*;

/**
 * Parser
 */
public class NaiveBNParser {

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
        if (line.matches("\\d+(\\s+)?:(\\s+)?(\\d+(\\s+)?((\\s+)?,(\\s+)?\\d+)*(\\s+)?)?")) {
            String readWithoutSpaces = line.replaceAll(" +", ""); //rimuove gli spazi
            String[] splitted = readWithoutSpaces.split("\\:");
            String[] incomingNodes = splitted[1].split("\\,");
            topExpr = new TopologyExprImpl(splitted[0], Arrays.asList(incomingNodes));
        }
        return topExpr;
    }

    private ExplicitFunExpr explicitFun(String line) {
        ExplicitFunExpr explicitExpr = null;
        if (line.matches("\\d+(\\s+)?:(\\s+)?\\d+(\\s+)?")) {
            String readWithoutSpaces = line.replaceAll(" +", ""); //rimuove gli spazi
            String[] splitted = readWithoutSpaces.split("\\:");
            explicitExpr = new ExplicitFunExprImpl(splitted[0], splitted[1]);
        }
        return explicitExpr;
    }

    private ImplicitFunExpr implicitFun(String line) {
        ImplicitFunExpr implicitExpr = null;

        if (line.matches("\\d+(\\s+)?:(\\s+)?(((((!)?\\d+(\\*(!)?\\d+)*)((\\s+)?\\+(\\s+)?((!)?\\d+(\\*(!)?\\d+)*))*)|(or)|(and))(\\s+)?)?")) {
            String readWithoutSpaces = line.replaceAll(" +", ""); //rimuove gli spazi
            String[] splitted = readWithoutSpaces.split("\\:");
            if (splitted.length > 1) { // vuol dire che c'è almeno un prodotto e quindi un 1 nella truth table
                if (splitted[1].equals("or")) {
                    implicitExpr = new ImplicitFunExprImpl(splitted[0], "or");
                } else if (splitted[1].equals("and")) {
                    implicitExpr = new ImplicitFunExprImpl(splitted[0], "and");
                } else {
                    String[] products = splitted[1].split("\\+");
                    implicitExpr = new ImplicitFunExprImpl(splitted[0], Arrays.asList(products));
                }
            } else {
                implicitExpr = new ImplicitFunExprImpl(splitted[0], "contradiction");
            }
        }
        System.out.println(implicitExpr);
        return implicitExpr;
    }

    private NameExpr name(String line) {
        NameExpr name = null;

        if (line.matches("\\d+(\\s+)?:(\\s+)?(\\w)*(\\s+)?")) {
            String readWithoutSpaces = line.replaceAll(" +", ""); //rimuove gli spazi
            String[] splitted = readWithoutSpaces.split("\\:");
            name = new NameExprImpl(splitted[0], splitted[1]);
        }
        return name;
    }

    // Collections
    private List<TopologyExpr> topology = new ArrayList<>();
    private List<ImplicitFunExpr> implicit = new ArrayList<>();
    private List<ExplicitFunExpr> explicit = new ArrayList<>();
    private List<NameExpr> names = new ArrayList<>();

    // parse
    public NetworkAST parse() {
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


    public static class AST implements NetworkAST {
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

    public static class TopologyExprImpl implements TopologyExpr{
        private final String node;
        private final List<String> input;

        public TopologyExprImpl(String node, List<String> input) {
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

    public static class ExplicitFunExprImpl implements ExplicitFunExpr{
        private final String node;
        private final String output;

        public ExplicitFunExprImpl(String node, String output) {
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

    public static class ImplicitFunExprImpl implements ImplicitFunExpr{

        private final String node;
        private List<String> terms;

        private DescriptiveFunction descriptiveFunction;

        public ImplicitFunExprImpl(String node, List<String> terms) {
            this.node = node;
            this.terms = terms;
        }

        public ImplicitFunExprImpl(String node, String descriptionWord) { // e.g. descriptionWord = "contradiction"
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

    public static class NameExprImpl implements NameExpr{
        private final String node;
        private final String name;

        public NameExprImpl(String node, String name) {
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
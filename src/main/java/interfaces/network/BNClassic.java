package interfaces.network;

import network.ContextImpl;
import utility.Files;

import java.io.StringWriter;
import java.util.List;

public interface BNClassic<K, V, N extends NodeDeterministic<K,V>> extends BooleanNetwork<N>{

    default Context<K,V> context(){
        ContextImpl.ContextBuilder<K,V> builder = ContextImpl.<K,V>builder();
        for (N node: getNodes()) {
            builder.add(node.getId(), node.getFunction());
        }
        return builder.build();
    }


    /* Static methods */

    /**
     * Actual Bias computed from truth tables
     * @return
     */
    default double computeActualAverageBias() {
        Double average = getNodes().stream().mapToDouble(x -> x.getFunction().getRows().stream()
                .mapToDouble(
                        y -> {
                            return (y.getOutput().equals(java.lang.Boolean.TRUE)) ? Double.valueOf(1.0) : Double.valueOf(0.0);
                        }
                ).average().getAsDouble()
        ).average().getAsDouble();
        return average;
    }



    public static <K, Boolean, N extends NodeDeterministic<K,Boolean>> String getBNFileRepresentation(BNClassic<K,Boolean,N> bn) {
        List<N> nodesList = bn.getNodes();
        List<N> incomingNodes = null;
        List<Row<K, Boolean>> rowsTruthTable = null;

        StringWriter writer = null;
        writer = new StringWriter();
        /*
         * Topology section
         */
        writer.append("Topology:");
        writer.append(Files.NEW_LINE);
        for (N node : nodesList) {
            writer.write(node.getId() + ":");
            incomingNodes = bn.getIncomingNodes(node);
            for (int i = 0; i < incomingNodes.size(); i++) {
                writer.write((i != (incomingNodes.size() - 1)) ? incomingNodes.get(i).getId() + "," : incomingNodes.get(i).getId() + "");
            }
            writer.append(Files.NEW_LINE);
        }
        /*
         * Functions section
         */
        writer.append("Functions E:");
        writer.append(Files.NEW_LINE);
        for (N node : nodesList) {
            writer.write(node.getId() + ":");
            rowsTruthTable = node.getFunction().getRows();
            Boolean outputValue;
            for (int i = 0; i < rowsTruthTable.size(); i++) {
                outputValue = rowsTruthTable.get(i).getOutput();
                //if (outputValue instanceof Boolean) {
                writer.write((outputValue.equals(true)) ? "1" : "0");
                //} else {
                //	writer.write(outputValue.toString());
                //}
            }
            writer.append(Files.NEW_LINE);

        }

        /*
         * Names section
         */
        writer.append("Names:");
        writer.append(Files.NEW_LINE);
        for (N node : nodesList) {
            writer.write(node.getId() + ":");
            writer.write(node.getName());
            writer.append(Files.NEW_LINE);
        }

        return writer.toString();
    }
}

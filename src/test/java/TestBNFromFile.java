import interfaces.network.BNClassic;
import interfaces.network.Node;
import interfaces.network.NodeDeterministic;
import network.BooleanNetworkFactory;
import org.junit.Test;
import utility.Files;

import java.io.File;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestBNFromFile {


    /**
     * Retrieves the path
     * @param path
     * @return
     */
    private String path(String path) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(path).getFile());
        return file.getAbsolutePath();
    }

    private String rootDirectory = "testing"
            + Files.FILE_SEPARATOR
            + "atm";

    @Test
    public void TestNetworkFromFile() {
        /** BN from file "sync_bn" **/
        String bnFilename = path(rootDirectory
                + Files.FILE_SEPARATOR
                + "self_loop_bn_1");

        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn = BooleanNetworkFactory.newNetworkFromFile(bnFilename);

        assertTrue("Number of nodes must be 3", 3 == bn.getNodesNumber());

        List<NodeDeterministic<BitSet, Boolean>> nodes = bn.getNodes();

        for (int i = 0; i <  bn.getNodesNumber(); i++) {
            //List of nodes sorted by their Id
            assertTrue("List of nodes must be sorted", bn.getNodes().get(i).getId() == i);

            //Names
            assertTrue("Name is \"Gene_i\"", bn.getNodes().get(i).getName().equals("Gene_" + i));

        }

        //Topology
        assertTrue("Incoming nodes of node with id 0", bn.getIncomingNodes(bn.getNodeById(0))
                 .stream()
                 .map(Node::getId)
                 .collect(Collectors.toList()).equals(List.of(1,2)));

        assertTrue("Incoming nodes of node with id 1", bn.getIncomingNodes(bn.getNodeById(1))
                .stream()
                .map(Node::getId)
                .collect(Collectors.toList()).equals(List.of(0,1,2)));

        assertTrue("Incoming nodes of node with id 2", bn.getIncomingNodes(bn.getNodeById(2))
                .stream()
                .map(Node::getId)
                .collect(Collectors.toList()).equals(List.of(1,2)));

        //Functions
        assertTrue("Function of node with id 0",
                        bn.getNodeById(0).getFunction().getRows().stream().map(x -> x.getOutput()).collect(Collectors.toList()).equals(List.of(false,true,false,true)));

        //Functions
        assertTrue("Function of node with id 1",
                bn.getNodeById(1).getFunction().getRows().stream().map(x -> x.getOutput()).collect(Collectors.toList()).equals(List.of(false,true,false,true,true,false,false,true)));

        //Functions
        assertTrue("Function of node with id 2",
                bn.getNodeById(2).getFunction().getRows().stream().map(x -> x.getOutput()).collect(Collectors.toList()).equals(List.of(true,false,true,false)));

    }

}

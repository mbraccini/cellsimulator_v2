package network;

import interfaces.network.BooleanNetwork;
import interfaces.networkdescription.NetworkAST;

import java.util.BitSet;

public class BooleanNetworkFactory {
    private BooleanNetworkFactory() {}

    /**
     * BN from a file description
     * @param filename
     * @return
     */
    public static BooleanNetwork<BitSet, Boolean> newNetworkFromFile(String filename) {
        NetworkAST ast = new NaiveBNParser(filename).parse();
        return new BNFromASTDescription(ast.getTopology().size(), ast);
    }

    /**
     * BN from an AST description
     * @param ast
     * @return
     */
    public static BooleanNetwork<BitSet, Boolean> newNetworkFromAST(NetworkAST ast) {
        return new BNFromASTDescription(ast.getTopology().size(), ast);
    }
}

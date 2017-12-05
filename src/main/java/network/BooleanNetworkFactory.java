package network;

import interfaces.networkdescription.NetworkAST;

public class BooleanNetworkFactory {
    private BooleanNetworkFactory() {}

    public static BNFromASTDescription newNetworkFromFile(String filename) {
        NetworkAST ast = new NaiveBNParser(filename).parse();
        return new BNFromASTDescription(ast.getTopology().size(), ast);
    }
}

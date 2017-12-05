package interfaces.networkdescription;

import java.util.Collection;

public interface NetworkAST {

    Collection<TopologyExpr> getTopology();

    Collection<ImplicitFunExpr> getImplicit();

    Collection<ExplicitFunExpr> getExplicit();

    Collection<NameExpr> getNames();
}

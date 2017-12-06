package interfaces.networkdescription;

import java.util.List;

public interface ImplicitFunExpr {

    enum DescriptiveFunction{
        CONTRADICTION, OR, AND, ERROR
    }

    DescriptiveFunction getDescriptiveFunction();

    /**
     * Node index
     * @return
     */
    String getNode();

    List<String> getTerms();
}

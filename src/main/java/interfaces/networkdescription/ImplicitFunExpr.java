package interfaces.networkdescription;

import java.util.List;

public interface ImplicitFunExpr {

    enum DescriptiveFunction{
        CONTRADICTION, OR, AND, ERROR
    }

    DescriptiveFunction getDescriptiveFunction();

    String getNode();

    List<String> getTerms();
}
